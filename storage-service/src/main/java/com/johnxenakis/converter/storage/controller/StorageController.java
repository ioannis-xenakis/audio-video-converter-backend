package com.johnxenakis.converter.storage.controller;

import com.johnxenakis.converter.storage.model.ResourceWithMeta;
import com.johnxenakis.converter.storage.model.StoredFile;
import com.johnxenakis.converter.storage.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/storage/files")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<StoredFile> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "ownerId", required = false) String ownerId,
            @RequestParam(value = "tags", required = false) String tags
    ) throws IOException {
        StoredFile stored = storageService.store(file, type, ownerId, tags);
        return ResponseEntity.status(HttpStatus.CREATED).body(stored);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) throws IOException {
        ResourceWithMeta resource = storageService.load(id);
        StoredFile meta = resource.getMeta();
        byte[] bytes = resource.getInputStream().readAllBytes();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .contentLength(meta.getSize())
                .body(bytes);
    }

    @GetMapping("/{id}/meta")
    public ResponseEntity<StoredFile> meta(@PathVariable String id) {
        return ResponseEntity.ok(storageService.load(id).getMeta());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
