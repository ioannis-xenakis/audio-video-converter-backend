package com.johnxenakis.converter.conversion.service;

import com.johnxenakis.converter.dto.StoredFileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;

@Service
public class StorageServiceClient {

    private final WebClient webClient;

    private final RestClient restClient;

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceClient.class);

    @Value("${storage-service.url}")
    private String storageServiceUrl;

    public StorageServiceClient(WebClient webClient, RestClient restClient) {
        this.webClient = webClient;
        this.restClient = restClient;
    }

    public InputStream downloadStream(String fileId) {

        logger.info("Starting downloading file via streaming: {}", fileId);

        try {
            InputStream stream = restClient.get()
                    .uri(
                            storageServiceUrl
                                    + "/api/storage/files/"
                                    + fileId
                    )
                    .exchange((request, response) ->
                            response.getBody()
                    );

            logger.info("Completed downloading file via streaming: {}", fileId);

            return stream;
        } catch (Exception e) {

            logger.error("Failed downloading file via streaming: {}  \n Reason: {}",
                    fileId, e.getMessage());

            throw e;
        }
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
        logger.info("Starting upload: {}", fileName);

        webClient.post()
                .uri(
                        storageServiceUrl
                                + "/api/storage/files/stream"
                                + "?fileName=" + fileName
                                + "&contentType=" + contentType
                                + "&bucketType=" + bucketType
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(
                        BodyInserters.fromResource(
                                new InputStreamResource(inputStream)
                        )
                )
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        
        logger.info("Upload finished: {}", fileName);
    }
}
