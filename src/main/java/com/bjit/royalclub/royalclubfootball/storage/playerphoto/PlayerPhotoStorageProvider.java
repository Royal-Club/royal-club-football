package com.bjit.royalclub.royalclubfootball.storage.playerphoto;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;

import java.io.IOException;
import java.io.InputStream;

public interface PlayerPhotoStorageProvider {

    TeamLogoUploadResponse generateUploadUrl(String fileName, String contentType);

    InputStream load(String key) throws IOException;

    String detectContentType(String key) throws IOException;

    void delete(String key);

    default void save(String key, InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Direct save is only supported by local storage provider");
    }
}
