package com.bjit.royalclub.royalclubfootball.storage.playerphoto;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class PlayerPhotoR2StorageProvider implements PlayerPhotoStorageProvider {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final long presignMinutes;

    public PlayerPhotoR2StorageProvider(String endpoint, String bucket,
                                        String accessKey, String secretKey,
                                        long presignMinutes) {
        this.bucket = bucket;
        this.presignMinutes = presignMinutes;

        StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(creds)
                .region(Region.of("auto"))
                .build();

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(creds)
                .region(Region.of("auto"))
                .build();
    }

    @Override
    public TeamLogoUploadResponse generateUploadUrl(String fileName, String contentType) {
        String key = "player-photo-" + UUID.randomUUID() + getSafeExtension(fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        String uploadUrl = presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignMinutes))
                        .putObjectRequest(putObjectRequest)
                        .build())
                .url()
                .toString();

        return TeamLogoUploadResponse.builder()
                .key(key)
                .url("/files/player-photos/" + key)
                .uploadUrl(uploadUrl)
                .expiresInSeconds(presignMinutes * 60)
                .build();
    }

    @Override
    public InputStream load(String key) throws IOException {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception ex) {
            throw new IOException("Failed to load player photo", ex);
        }
    }

    @Override
    public String detectContentType(String key) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return response.contentType() != null ? response.contentType() : "application/octet-stream";
        } catch (Exception ex) {
            return "application/octet-stream";
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception ex) {
            log.warn("Failed to delete player photo key={}: {}", key, ex.getMessage());
        }
    }

    private static String getSafeExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) return ".png";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) return ".png";
        String extension = originalFileName.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : ".png";
    }
}
