package com.bjit.royalclub.royalclubfootball.storage;

public interface StorageProvider {

    PresignedUploadResponse generateUploadUrl(String folder, String originalName, String contentType);

    String generateViewUrl(String key);

    void delete(String key);
}
