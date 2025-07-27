package com.johnxenakis.converter.upload.controller;

import com.johnxenakis.converter.upload.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final FileStorageService storageService;

    public UploadController(FileStorageService storageService){
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
        String fileId = storageService.store(file);
        return ResponseEntity.ok(fileId);
    }
}