package com.bjit.royalclub.royalclubfootball.storage.playerphoto;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class PlayerPhotoLocalStorageProvider implements PlayerPhotoStorageProvider {

    private final Path uploadPath;
    private final String baseUrl;

    public PlayerPhotoLocalStorageProvider(String uploadDir, String baseUrl) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @Override
    public TeamLogoUploadResponse generateUploadUrl(String fileName, String contentType) {
        String key = "player-photo-" + UUID.randomUUID() + getSafeExtension(fileName);
        return TeamLogoUploadResponse.builder()
                .key(key)
                .url("/files/player-photos/" + key)
                .uploadUrl(baseUrl + "/files/player-photos/local/" + key)
                .expiresInSeconds(3600L)
                .build();
    }

    @Override
    public void save(String key, InputStream inputStream) throws IOException {
        validateKey(key);
        Files.createDirectories(uploadPath);
        Path destination = uploadPath.resolve(key).normalize();
        if (!destination.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream load(String key) throws IOException {
        validateKey(key);
        Path filePath = uploadPath.resolve(key).normalize();
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new IOException("File not found");
        }
        return Files.newInputStream(filePath);
    }

    @Override
    public String detectContentType(String key) throws IOException {
        validateKey(key);
        Path filePath = uploadPath.resolve(key).normalize();
        String contentType = Files.probeContentType(filePath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    @Override
    public void delete(String key) {
        try {
            validateKey(key);
            Path filePath = uploadPath.resolve(key).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception ignored) {
        }
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    private static String getSafeExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) return ".png";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) return ".png";
        String extension = originalFileName.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : ".png";
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) return "http://localhost:9191";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
