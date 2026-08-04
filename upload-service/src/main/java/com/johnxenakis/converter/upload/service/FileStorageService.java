package com.johnxenakis.converter.upload.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.dto.JobPayload;
import com.johnxenakis.converter.upload.config.UploadProperties;
import com.johnxenakis.converter.upload.exception.FileValidationException;
import com.johnxenakis.converter.upload.exception.UploadFailureException;
import com.johnxenakis.converter.upload.model.StoredFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

@Service
public class FileStorageService {
    @Autowired
    private StorageServiceClient storageClient;

    private final UploadProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public FileStorageService(UploadProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file, String outputFormat) {
        return storeInternal(file, null, outputFormat); // default behavior
    }

    public String storeWithFixedId(MultipartFile file, String fileIdOverride, String outputFormat) {
        return storeInternal(file, fileIdOverride, outputFormat); // used only in tests
    }

    public String storeInternal(MultipartFile file, String fileIdOverride, String outputFormat) {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename).toLowerCase();
        String mimeType = file.getContentType();

        // Validate extension and MIME type
        validate(ext, mimeType, originalFilename);

        try {
            // Upload to Storage Service
            StoredFileResponse stored = storageClient.upload(file, "original");

            logger.info("Uploaded file {} to Storage Service with ID {}", originalFilename, stored.getId());

            // Publish Kafka conversion job
            if (outputFormat != null) {
                logger.info("originalFilename: {}, mimeType: {}, outputFormat: {}", originalFilename, mimeType, outputFormat);
                JobPayload job = new JobPayload(
                        stored.getId(),
                        stored.getContentType(),
                        outputFormat
                );

                kafkaTemplate.send("conversion-jobs", stored.getId(), job);
                logger.info("Published conversion job for {}", stored.getId());
            }

            return stored.getId();

        } catch (WebClientResponseException.Conflict e) {
            // Storage Service says: file already exists
            throw new FileValidationException(originalFilename, "File already exists");
        } catch (Exception e) {
            logger.error("Upload failed for {}: {}", originalFilename, e.getMessage());
            throw new UploadFailureException(originalFilename, "Upload failed via Storage Service");
        }
    }

    private void validate(String ext, String mimeType, String originalFilename) {
        // Validate extension
        if(!properties.getAllowedExtensions().contains(ext)) {
            logger.warn("Rejected file: {} with unsupported extension: {} ", originalFilename, ext);
            throw new FileValidationException(originalFilename, "Unsupported file extension: " + ext);
        }

        // Validate MIME type
        if(!properties.getAllowedMimetypes().contains(mimeType)) {
            logger.warn("Rejected file: {} with unsupported MIME type: {} ", originalFilename, mimeType);
            throw new FileValidationException(originalFilename, "Unsupported MIME type: " + mimeType);
        }
    }

    private String getExtension(String fileName) {
        return fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";
    }
}
