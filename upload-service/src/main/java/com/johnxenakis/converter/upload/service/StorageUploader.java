package com.johnxenakis.converter.upload.service;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.upload.exception.UploadFailureException;
import com.johnxenakis.converter.upload.websocket.UploadProgressHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Component
public class StorageUploader {

    @Autowired
    private UploadProgressHandler progressHandler;

    private static final Logger logger = LoggerFactory.getLogger(StorageUploader.class);

    @Retryable(
            retryFor = { IOException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void uploadToStorage(MultipartFile file, BlobInfo blobInfo, Storage storage) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             WriteChannel writer = storage.writer(blobInfo)) {
            long totalBytesLoaded = 0;
            long fileSize = file.getSize(); // Total file size in bytes

            byte[] buffer = new byte[64 * 1024];
            int limit;
            while ((limit = inputStream.read(buffer)) >= 0) {
                writer.write(ByteBuffer.wrap(buffer, 0, limit));
                totalBytesLoaded += limit;

                double progress = (double) totalBytesLoaded / fileSize * 100;
                logger.info("Uploading {}: {} bytes uploaded ({})%", file.getOriginalFilename(), totalBytesLoaded, String.format("%.2f", progress));
                progressHandler.broadcastProgress(file.getOriginalFilename(), progress, totalBytesLoaded);
            }
        }
    }

    @Recover
    public void recover(IOException e, MultipartFile file, BlobInfo blobInfo, Storage storage) {
        String filename = file.getOriginalFilename();
        logger.error("Upload failed after retries for file: {}", filename, e);
        progressHandler.broadcastFailure(filename, "Upload failed after multiple attempts");
        throw new UploadFailureException(filename, "Upload failed after multiple attempts");
    }

    private static byte[] getBuffer(long fileSize) {
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
            chunkSize = 64 * 1024;
        }
        return new byte[chunkSize];
    }

}
