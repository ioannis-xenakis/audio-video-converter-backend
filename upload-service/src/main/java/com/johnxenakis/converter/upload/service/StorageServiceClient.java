package com.johnxenakis.converter.upload.service;

import com.johnxenakis.converter.upload.model.StoredFileResponse;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StorageServiceClient {

    private final WebClient webClient;

    public StorageServiceClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://storage-service:8083/api/storage/files").build();
    }

    public StoredFileResponse upload(MultipartFile file, String type) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", file.getResource());
        bodyBuilder.part("type", type);

        return webClient.post()
                .bodyValue(bodyBuilder.build())
                .retrieve()
                .bodyToMono(StoredFileResponse.class)
                .block();
    }

    public StoredFileResponse getMeta(String id) {
        return webClient.get()
                .uri("/{id}/meta", id)
                .retrieve()
                .bodyToMono(StoredFileResponse.class)
                .block();
    }

    public byte[] download(String id) {
        return webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
