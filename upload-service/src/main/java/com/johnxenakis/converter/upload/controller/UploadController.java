package com.johnxenakis.converter.upload.controller;

import com.johnxenakis.converter.upload.exception.FileValidationException;
import com.johnxenakis.converter.upload.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final FileStorageService storageService;

    public UploadController(FileStorageService storageService){
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file){
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("uploaded", storageService.store(file));
        } catch (FileValidationException e) {
            Map<String, Object> failedDetails = new HashMap<>();
            failedDetails.put("fileName", e.getFileName());
            failedDetails.put("reason", e.getReason());
            response.put("failed", failedDetails);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        List<String> uploadedFileIds = new ArrayList<>();
        List<Map<String, String>> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                uploadedFileIds.add(storageService.store(file));
            } catch (FileValidationException e) {
                Map<String, String> errorDetails = new HashMap<>();
                errorDetails.put("file", e.getFileName());
                errorDetails.put("error", e.getReason());
                failedFiles.add(errorDetails);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uploaded", uploadedFileIds);
        response.put("failed", failedFiles);

        return ResponseEntity.ok(response);
    }
}