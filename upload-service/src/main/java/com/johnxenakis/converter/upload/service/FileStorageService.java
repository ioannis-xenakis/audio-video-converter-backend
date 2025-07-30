package com.johnxenakis.converter.upload.service;

import com.johnxenakis.converter.upload.config.UploadProperties;
import com.johnxenakis.converter.upload.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private final UploadProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    public FileStorageService(UploadProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String ext = getExtension(fileName).toLowerCase();
        String mimeType = file.getContentType();

        // Validate extension
        if(!properties.getAllowedExtensions().contains(ext)) {
            logger.warn("Rejected file: {} with unsupported extension: {} ", fileName, ext);
            throw new FileValidationException("Unsupported file extension: " + ext);
        }

        // Validate MIME type
        if(!properties.getAllowedMimetypes().contains(mimeType)) {
            logger.warn("Rejected file: {} with unsupported MIME type: {} ", fileName, mimeType);
            throw new FileValidationException("Unsupported MIME type: " + mimeType);
        }

        try {
            Path uploadPath = Paths.get(properties.getDir());
            Files.createDirectories(uploadPath);

            String fileId = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
            Path path = uploadPath.resolve(fileId);

            file.transferTo(path.toFile());

            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String getExtension(String fileName) {
        return fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";
    }
}
