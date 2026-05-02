package com.bjit.royalclub.royalclubfootball.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
public class LocalStorageProvider implements StorageProvider {

    private final Path uploadRoot;
    private final String baseUrl;

    public LocalStorageProvider(String uploadDir, String baseUrl) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
    }

    @Override
    public PresignedUploadResponse generateUploadUrl(String folder, String originalName, String contentType) {
        String ext = extractExtension(originalName);
        String baseName = originalName != null && originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : (originalName != null ? originalName : "file");

        String key = folder + "/" + baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;

        String encodedName = URLEncoder.encode(fileNameFromKey(key), StandardCharsets.UTF_8).replace("+", "%20");
        String uploadUrl = baseUrl + "/files/local/" + folder + "/" + encodedName;

        return new PresignedUploadResponse(uploadUrl, key, 3600);
    }

    @Override
    public String generateViewUrl(String key) {
        String folder = key.contains("/") ? key.substring(0, key.lastIndexOf('/')) : "";
        String encodedName = URLEncoder.encode(fileNameFromKey(key), StandardCharsets.UTF_8).replace("+", "%20");
        return baseUrl + "/files/local/" + (folder.isEmpty() ? "" : folder + "/") + encodedName;
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolvePath(key));
        } catch (Exception e) {
            log.warn("Could not delete local file key={}: {}", key, e.getMessage());
        }
    }

    public void save(String key, InputStream data) {
        try {
            Path target = resolvePath(key);
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save file");
        }
    }

    public InputStream load(String key) {
        try {
            return Files.newInputStream(resolvePath(key));
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }

    public String detectContentType(String key) {
        try {
            String contentType = Files.probeContentType(resolvePath(key));
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception ignored) {
            return "application/octet-stream";
        }
    }

    private Path resolvePath(String key) {
        String sanitized = key.replace("\\", "/").replaceFirst("^/+", "");
        Path target = uploadRoot.resolve(sanitized).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid file key");
        }
        return target;
    }

    private static String extractExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        }
        return "";
    }

    private static String fileNameFromKey(String key) {
        return key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
    }
}
