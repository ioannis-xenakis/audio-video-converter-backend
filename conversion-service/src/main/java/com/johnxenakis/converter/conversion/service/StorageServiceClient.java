package com.johnxenakis.converter.conversion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class StorageServiceClient {

    private final RestTemplate restTemplate;

    // TODO Create storage-service.url in application.properties file.
    @Value("${storage-service.url}")
    private String storageServiceUrl;

    public StorageServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public InputStream download(String fileId) {
        ResponseEntity<byte[]> response =
                restTemplate.getForEntity(
                        storageServiceUrl + "/api/storage/files/" + fileId,
                        byte[].class
                );

        return new ByteArrayInputStream(response.getBody());
    }

    // TODO Create StoredFileMeta DTO class file.
    public StoredFileMeta getMeta(String fileId) {
        return restTemplate.getForObject(
                storageServiceUrl + "/api/storage/files/" + fileId + "/meta",
                StoredFileMeta.class
        );
    }
}
