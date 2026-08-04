package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileStorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * Binary endpoints for the resource library — formation diagrams, cover images
 * and documents.
 * <p>
 * Only managers can mint an upload key or delete one. The local {@code PUT}
 * that follows a presign is intentionally unauthenticated, mirroring the
 * player-photo flow: the browser uploads straight to the presigned target
 * without the API's bearer token, and the key is an unguessable UUID.
 */
@RestController
@RequestMapping("/files/resources")
@RequiredArgsConstructor
public class ResourceFileController {

    private static final String MANAGE_ROLES = "hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')";
    private static final long MAX_SIZE_BYTES = 25L * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml",
            "application/pdf");

    private final ResourceFileStorageProvider resourceFileStorageProvider;

    @PreAuthorize(MANAGE_ROLES)
    @PostMapping("/presign")
    public ResponseEntity<Object> presignUpload(@RequestParam String fileName,
                                                @RequestParam String contentType) {
        validateMeta(fileName, contentType);
        TeamLogoUploadResponse response = resourceFileStorageProvider.generateUploadUrl(fileName, contentType);
        return buildSuccessResponse(HttpStatus.OK, "Presigned URL generated", response);
    }

    @PutMapping("/local/{key}")
    public ResponseEntity<Object> localUpload(@PathVariable String key, HttpServletRequest request) throws IOException {
        if (!(resourceFileStorageProvider instanceof ResourceFileLocalStorageProvider)) {
            return ResponseEntity.notFound().build();
        }
        validateKey(key);
        if (request.getContentLengthLong() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the 25MB limit");
        }
        try (InputStream inputStream = request.getInputStream()) {
            resourceFileStorageProvider.save(key, inputStream);
        }
        return buildSuccessResponse(HttpStatus.OK, "Upload successful", Map.of("key", key));
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getResourceFile(@PathVariable String key) throws IOException {
        validateKey(key);
        try (InputStream inputStream = resourceFileStorageProvider.load(key)) {
            byte[] content = StreamUtils.copyToByteArray(inputStream);
            String contentType = resourceFileStorageProvider.detectContentType(key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize(MANAGE_ROLES)
    @DeleteMapping
    public ResponseEntity<Object> deleteResourceFile(@RequestParam String key) {
        validateKey(key);
        resourceFileStorageProvider.delete(key);
        return buildSuccessResponse(HttpStatus.OK, "Deleted", Map.of("key", key));
    }

    private void validateMeta(String fileName, String contentType) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only images and PDF documents can be uploaded");
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key");
        }
    }
}
