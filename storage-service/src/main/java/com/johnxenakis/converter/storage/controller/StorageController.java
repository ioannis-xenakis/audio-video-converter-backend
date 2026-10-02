package com.johnxenakis.converter.storage.controller;

import com.johnxenakis.converter.dto.StoredFileDto;
import com.johnxenakis.converter.storage.exception.DuplicateFileException;
import com.johnxenakis.converter.storage.model.ResourceWithMeta;
import com.johnxenakis.converter.storage.model.StoredFile;
import com.johnxenakis.converter.storage.service.StorageService;
import com.johnxenakis.converter.storage.util.StoredFileMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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
            @RequestParam("bucketType") String bucketType,
            @RequestParam(value = "ownerId", required = false) String ownerId,
            @RequestParam(value = "tags", required = false) String tags
    ) throws IOException {
        try {
            StoredFile stored = storageService.store(file, bucketType, ownerId, tags);
            return ResponseEntity.status(HttpStatus.CREATED).body(stored);
        } catch (DuplicateFileException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<StoredFileDto> uploadStream(
            HttpServletRequest request,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam String bucketType,
            @RequestParam(value = "ownerId", required = false) String ownerId,
            @RequestParam(value = "tags", required = false) String tags
    ) throws IOException {

        InputStream inputStream = request.getInputStream();

        StoredFile entity = storageService.store(
                inputStream,
                fileName,
                contentType,
                bucketType,
                ownerId,
                tags
        );

        return ResponseEntity.ok(StoredFileMapper.toDto(entity));
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
    public ResponseEntity<StoredFileDto> meta(@PathVariable String id) {
        StoredFile entity = storageService.load(id).getMeta();

        return ResponseEntity.ok(StoredFileMapper.toDto(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        storageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
