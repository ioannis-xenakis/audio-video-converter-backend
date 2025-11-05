package com.johnxenakis.converter.upload.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.upload.config.UploadProperties;
import com.johnxenakis.converter.upload.exception.FileValidationException;
import com.johnxenakis.converter.upload.exception.UploadFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {
    private final UploadProperties properties;
    private final Storage storage;
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private StorageUploader uploader;

    public FileStorageService(UploadProperties properties, Storage storage) {
        this.properties = properties;
        this.storage = storage;
    }

    public String store(MultipartFile file) {
        return storeInternal(file, null); // default behavior
    }

    public String storeWithFixedId(MultipartFile file, String fileIdOverride) {
        return storeInternal(file, fileIdOverride); // used only in tests
    }

    public String storeInternal(MultipartFile file, String fileIdOverride) {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename).toLowerCase();
        String mimeType = file.getContentType();

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

        try {
            String blobName = fileIdOverride != null ? fileIdOverride : resolveBlobNameWithVersioning(originalFilename);
            BlobId blobId = BlobId.of(properties.getBucketName(), blobName);

            if (!properties.isAllowOverwrite() && storage.get(blobId) != null) {
                logger.warn("File {} already exists in bucket {}", blobName, properties.getBucketName());
                throw new FileValidationException(originalFilename, "File " + blobName + "already exists in storage");
            }

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();

            uploader.uploadToStorage(file, blobInfo, storage);

            return blobName;
        } catch (IOException e) {
            throw new UploadFailureException(originalFilename, "Upload failed");
        }
    }

    private String getExtension(String fileName) {
        return fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";
    }

    private String resolveBlobNameWithVersioning(String originalName) {
        String ext = getExtension(originalName);
        String baseName = originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : originalName;

        String blobName = originalName;
        BlobId blobId = BlobId.of(properties.getBucketName(), blobName);
        int version = 1;

        while (storage.get(blobId) != null) {
            version++;
            blobName = baseName + "_v" + version + (ext.isEmpty() ? "" : "." + ext);
            blobId = BlobId.of(properties.getBucketName(), blobName);
        }

        return blobName;
    }
}
