package com.johnxenakis.converter.conversion.service;

import com.johnxenakis.converter.dto.StoredFileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class StorageServiceClient {

    private final WebClient webClient;

    @Value("${storage-service.url}")
    private String storageServiceUrl;

    public StorageServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public InputStream download(String fileId) {
        byte[] bytes = webClient.get()
                .uri(storageServiceUrl + "/api/storage/files/" + fileId)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        return new ByteArrayInputStream(bytes);
    }

    public StoredFileDto getMeta(String fileId) {
        return webClient.get()
                .uri(storageServiceUrl + "/api/storage/files/" + fileId + "/meta")
                .retrieve()
                .bodyToMono(StoredFileDto.class)
                .block();
    }

    public void uploadStream(
            InputStream inputStream,
            String fileName,
            String contentType,
            String bucketType
    ) {

        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(storageServiceUrl + "/api/storage/files/stream")
                        .queryParam("fileName", fileName)
                        .queryParam("contentType", contentType)
                        .queryParam("bucketType", bucketType)
                        .build())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(
                        BodyInserters.fromResource(
                                new InputStreamResource(inputStream)
                        )
                )
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
