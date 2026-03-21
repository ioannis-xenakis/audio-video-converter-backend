package com.johnxenakis.converter.download.service;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.channels.Channels;

@Service
public class DownloadService {
    private final Storage storage;
    private final String bucketName;

    public DownloadService(Storage storage,
                           @Value("${app.gcs.bucket-name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    public ResponseEntity<InputStreamResource> download(String blobName,
                                                        String mimeType,
                                                        String outputFormat) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);

        if (blob == null || !blob.exists()) {
            return ResponseEntity.notFound().build();
        }

        ReadChannel reader = blob.reader();
        InputStream inputStream = Channels.newInputStream(reader);

        String filename = buildFilename(blobName, outputFormat);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                mimeType != null ? mimeType : "application/octet-stream"
        ));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }

    private String buildFilename(String blobName, String outputFormat) {
        if (outputFormat == null || outputFormat.isBlank()) {
            return blobName;
        }
        int dotIndex = blobName.lastIndexOf('.');
        String base = dotIndex > 0 ? blobName.substring(0, dotIndex) : blobName;
        return base + "." + outputFormat;
    }
}
