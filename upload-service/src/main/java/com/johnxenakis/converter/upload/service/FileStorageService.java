package com.johnxenakis.converter.upload.service;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.upload.config.UploadProperties;
import com.johnxenakis.converter.upload.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

@Service
public class FileStorageService {
    private final UploadProperties properties;
    private final Storage storage;
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    public FileStorageService(UploadProperties properties, Storage storage) {
        this.properties = properties;
        this.storage = storage;
    }

    public String store(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String ext = getExtension(fileName).toLowerCase();
        String mimeType = file.getContentType();

        // Validate extension
        if(!properties.getAllowedExtensions().contains(ext)) {
            logger.warn("Rejected file: {} with unsupported extension: {} ", fileName, ext);
            throw new FileValidationException(fileName, "Unsupported file extension: " + ext);
        }

        // Validate MIME type
        if(!properties.getAllowedMimetypes().contains(mimeType)) {
            logger.warn("Rejected file: {} with unsupported MIME type: {} ", fileName, mimeType);
            throw new FileValidationException(fileName, "Unsupported MIME type: " + mimeType);
        }

        try {
            String fileId = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
            BlobId blobId = BlobId.of(properties.getBucketName(), fileId);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();

            try (InputStream inputStream = file.getInputStream();
                 WriteChannel writer = storage.writer(blobInfo)) {
                long totalBytesLoaded = 0;
                long fileSize = file.getSize(); // Total file size in bytes

                int chunkSize;
                if (fileSize > 5L * 1024 * 1024 * 1024) { // >5GB
                    chunkSize = 128 * 1024 * 1024;
                } else if (fileSize > 2L * 1024 * 1024 * 1024) {
                    chunkSize = 64 * 1024 * 1024;
                } else if (fileSize > 500L * 1024 * 1024) {
                    chunkSize = 32 * 1024 * 1024;
                } else if (fileSize > 100L * 1024 * 1024) {
                    chunkSize = 16 * 1024 * 1024;
                } else {
                    chunkSize = 8 * 1024 * 1024;
                }
                byte[] buffer = new byte[chunkSize];
                int limit;
                while ((limit = inputStream.read(buffer)) >= 0) {
                    writer.write(ByteBuffer.wrap(buffer, 0, limit));
                    totalBytesLoaded += limit;

                    double progress = (double) totalBytesLoaded / fileSize * 100;
                    logger.info("Uploading {}: {} bytes uploaded ({})%", fileName, totalBytesLoaded, String.format("%.2f", progress));
                }
            }

            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file in Google Cloud Storage", e);
        }
    }

    private String getExtension(String fileName) {
        return fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";
    }
}
