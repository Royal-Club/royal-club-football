package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.storage.LocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.PresignedUploadResponse;
import com.bjit.royalclub.royalclubfootball.storage.StorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class StorageController {

    private final StorageProvider storageProvider;

    @PostMapping("/presign")
    public ResponseEntity<Object> presign(
            @RequestParam String folder,
            @RequestParam String fileName,
            @RequestParam String contentType
    ) {
        PresignedUploadResponse response = storageProvider.generateUploadUrl(folder, fileName, contentType);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @GetMapping("/view-url")
    public ResponseEntity<Object> viewUrl(@RequestParam String key) {
        String url = storageProvider.generateViewUrl(key);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, Map.of("url", url, "expiresInSeconds", 3600));
    }

    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestParam String key) {
        storageProvider.delete(key);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

    @PutMapping("/local/**")
    public ResponseEntity<Void> localUpload(HttpServletRequest request) throws IOException {
        if (!(storageProvider instanceof LocalStorageProvider localStorageProvider)) {
            return ResponseEntity.notFound().build();
        }
        String key = extractKeyFromRequest(request);
        try (InputStream in = request.getInputStream()) {
            localStorageProvider.save(key, in);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/local/**")
    public void localServe(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!(storageProvider instanceof LocalStorageProvider localStorageProvider)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String key = extractKeyFromRequest(request);
        response.setContentType(localStorageProvider.detectContentType(key));
        response.setHeader("Content-Disposition", "inline; filename=\"" + key.substring(key.lastIndexOf('/') + 1) + "\"");
        response.setHeader("Cache-Control", "private, max-age=3600");

        try (InputStream in = localStorageProvider.load(key)) {
            StreamUtils.copy(in, response.getOutputStream());
        }
    }

    private String extractKeyFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/files/local/";
        int idx = uri.indexOf(prefix);
        String encoded = idx >= 0 ? uri.substring(idx + prefix.length()) : uri;
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
