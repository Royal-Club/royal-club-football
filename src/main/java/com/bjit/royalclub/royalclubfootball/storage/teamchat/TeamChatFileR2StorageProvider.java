package com.bjit.royalclub.royalclubfootball.storage.teamchat;

import com.bjit.royalclub.royalclubfootball.exception.TeamChatStorageException;
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
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Slf4j
public class TeamChatFileR2StorageProvider implements TeamChatFileStorageProvider {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final long presignMinutes;

    public TeamChatFileR2StorageProvider(String endpoint, String bucket,
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
    public TeamLogoUploadResponse generateUploadUrl(Long teamId, String fileName, String contentType) {
        String key = TeamChatFileStorageProvider.keyFor(teamId, fileName);

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
                // No public read URL, for the same reason as the local provider.
                .url(null)
                .uploadUrl(uploadUrl)
                .expiresInSeconds(presignMinutes * 60)
                .build();
    }

    @Override
    public InputStream load(String key) throws IOException {
        validateKey(key);
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception ex) {
            throw new IOException("Failed to load team chat file", ex);
        }
    }

    @Override
    public String detectContentType(String key) {
        validateKey(key);
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return response.contentType() != null ? response.contentType() : "application/octet-stream";
        } catch (Exception ex) {
            return "application/octet-stream";
        }
    }

    /** Never throws, for the same reason as the local provider does not. */
    @Override
    public void delete(String key) {
        try {
            validateKey(key);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception ex) {
            log.warn("Failed to delete team chat file key={}: {}", key, ex.getMessage());
        }
    }

    @Override
    public int deleteAllForTeam(Long teamId) {
        String prefix = TeamChatFileStorageProvider.teamPrefix(teamId);
        int removed = 0;
        int failed = 0;

        try {
            // Paginated: one room stays small, but a listing that silently stopped at the first page
            // would leave files behind while reporting success, which is the one outcome this whole
            // sweep exists to prevent.
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            for (ListObjectsV2Response page : s3Client.listObjectsV2Paginator(request)) {
                for (S3Object object : page.contents()) {
                    try {
                        s3Client.deleteObject(DeleteObjectRequest.builder()
                                .bucket(bucket).key(object.key()).build());
                        removed++;
                    } catch (Exception ex) {
                        // One unreachable object must not abandon the rest; the throw below still
                        // makes sure the room comes back around for another attempt.
                        failed++;
                        log.warn("Failed to delete team chat object {}: {}", object.key(), ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            throw new TeamChatStorageException(
                    "Could not list team chat objects for team " + teamId, ex);
        }

        if (failed > 0) {
            throw new TeamChatStorageException(
                    "Left " + failed + " team chat object(s) undeleted for team " + teamId);
        }
        return removed;
    }

    private static void validateKey(String key) {
        if (!TeamChatFileStorageProvider.isTeamChatKey(key)) {
            throw new IllegalArgumentException("Invalid team chat file key");
        }
    }
}
