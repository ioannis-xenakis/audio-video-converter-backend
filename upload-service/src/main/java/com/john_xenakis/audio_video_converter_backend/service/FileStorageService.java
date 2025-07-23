package com.john_xenakis.audio_video_converter_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final String UPLOAD_DIR = "uploads/";

    public String store(MultipartFile file) {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String ext = getExtension(file.getOriginalFilename());
            String fileId = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
            Path path = Paths.get(UPLOAD_DIR, fileId);

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
