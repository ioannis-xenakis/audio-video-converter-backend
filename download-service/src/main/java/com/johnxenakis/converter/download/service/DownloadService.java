package com.johnxenakis.converter.download.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {
    private final Storage storage;
    private final String bucketName;

    public DownloadService(Storage storage,
                           @Value("${app.gcs.bucket-name}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    public ResponseEntity<InputStreamSource> download(String blobName,
                                                      String mimeType,
                                                      String outputFormat) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);

        // TODO Download function is not finished.
    }
}
