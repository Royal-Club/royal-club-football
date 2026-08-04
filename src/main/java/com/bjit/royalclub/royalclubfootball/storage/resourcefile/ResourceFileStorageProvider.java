package com.bjit.royalclub.royalclubfootball.storage.resourcefile;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Storage for resource-library binaries — formation diagrams, cover images and
 * downloadable documents.
 */
public interface ResourceFileStorageProvider {

    TeamLogoUploadResponse generateUploadUrl(String fileName, String contentType);

    InputStream load(String key) throws IOException;

    String detectContentType(String key) throws IOException;

    void delete(String key);

    default void save(String key, InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Direct save is only supported by local storage provider");
    }
}
