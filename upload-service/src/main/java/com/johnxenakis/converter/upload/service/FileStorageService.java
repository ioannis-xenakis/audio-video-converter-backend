package com.johnxenakis.converter.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${upload.dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String ext = getExtension(file.getOriginalFilename());
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
