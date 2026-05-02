package com.bjit.royalclub.royalclubfootball.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class R2StorageProvider implements StorageProvider {

    private final String bucket;
    private final long presignDurationMinutes;
    private final S3Presigner presigner;
    private final S3Client s3Client;

    public R2StorageProvider(String endpoint, String bucket, String accessKey, String secretKey, long presignDurationMinutes) {
        this.bucket = bucket;
        this.presignDurationMinutes = presignDurationMinutes;

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public PresignedUploadResponse generateUploadUrl(String folder, String originalName, String contentType) {
        String key = folder + "/" + UUID.randomUUID() + extractExtension(originalName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        String uploadUrl = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                        .putObjectRequest(putObjectRequest)
                        .build()
        ).url().toString();

        return new PresignedUploadResponse(uploadUrl, key, presignDurationMinutes * 60);
    }

    @Override
    public String generateViewUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        return presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignDurationMinutes))
                        .getObjectRequest(getObjectRequest)
                        .build()
        ).url().toString();
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("Could not delete R2 object key={}: {}", key, e.getMessage());
        }
    }

    private static String extractExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        }
        return "";
    }
}
