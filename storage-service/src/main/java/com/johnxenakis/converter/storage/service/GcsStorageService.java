package com.johnxenakis.converter.storage.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.storage.model.ResourceWithMeta;
import com.johnxenakis.converter.storage.model.StoredFile;
import com.johnxenakis.converter.storage.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class GcsStorageService implements StorageService {

    private final Storage storage;
    private final StoredFileRepository repository;

    @Value("${storage.bucket-name}")
    private String bucketName;

    @Value("${storage.base-path}")
    private String basePath;

    public GcsStorageService(Storage storage, StoredFileRepository repository) {
        this.storage = storage;
        this.repository = repository;
    }

    @Override
    public StoredFile store(MultipartFile file, String ownerId, String tags) throws IOException {
        String id = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String objectName = buildObjectName(id, extension);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        StoredFile stored = new StoredFile();
        stored.setId(id);
        stored.setBucket(bucketName);
        stored.setObjectName(objectName);
        stored.setContentType(blob.getContentType());
        stored.setSize(blob.getSize());
        stored.setCreatedAt(Instant.now());
        stored.setOwnerId(ownerId);
        stored.setTags(tags);

        return repository.save(stored);
    }

    @Override
    public ResourceWithMeta load(String id) {
        StoredFile meta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));

        Blob blob = storage.get(meta.getBucket(), meta.getObjectName());
        if (blob == null) {
            throw new RuntimeException("Blob missing in GCS: " + id);
        }

        ResourceWithMeta result = new ResourceWithMeta();
        result.setMeta(meta);
        result.setInputStream(new ByteArrayInputStream(blob.getContent()));
        return result;
    }

    @Override
    public void delete(String id) {
        StoredFile meta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));

        storage.delete(meta.getBucket(), meta.getObjectName());
        repository.delete(meta);
    }

    private String buildObjectName(String id, String extension) {
        String datePath = LocalDate.now().toString();
        return String.format("%s/%s/%s.%s", basePath, datePath, id, extension);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
