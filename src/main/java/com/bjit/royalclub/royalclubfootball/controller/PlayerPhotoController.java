package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.playerphoto.PlayerPhotoStorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequestMapping("/files/player-photos")
@RequiredArgsConstructor
public class PlayerPhotoController {

    private final PlayerPhotoStorageProvider playerPhotoStorageProvider;

    @PostMapping("/presign")
    public ResponseEntity<Object> presignUpload(@RequestParam String fileName,
                                                @RequestParam String contentType) {
        validateMeta(fileName, contentType);
        TeamLogoUploadResponse response = playerPhotoStorageProvider.generateUploadUrl(fileName, contentType);
        return buildSuccessResponse(HttpStatus.OK, "Presigned URL generated", response);
    }

    @PutMapping("/local/{key}")
    public ResponseEntity<Object> localUpload(@PathVariable String key, HttpServletRequest request) throws IOException {
        if (!(playerPhotoStorageProvider instanceof PlayerPhotoLocalStorageProvider)) {
            return ResponseEntity.notFound().build();
        }
        validateKey(key);
        try (InputStream inputStream = request.getInputStream()) {
            playerPhotoStorageProvider.save(key, inputStream);
        }
        return buildSuccessResponse(HttpStatus.OK, "Upload successful", Map.of("key", key));
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getPlayerPhoto(@PathVariable String key) throws IOException {
        validateKey(key);
        try (InputStream inputStream = playerPhotoStorageProvider.load(key)) {
            byte[] content = StreamUtils.copyToByteArray(inputStream);
            String contentType = playerPhotoStorageProvider.detectContentType(key);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> deletePlayerPhoto(@RequestParam String key) {
        validateKey(key);
        playerPhotoStorageProvider.delete(key);
        return buildSuccessResponse(HttpStatus.OK, "Deleted", Map.of("key", key));
    }

    private void validateMeta(String fileName, String contentType) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid key");
        }
    }
}
