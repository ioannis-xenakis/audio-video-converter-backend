package com.johnxenakis.converter.conversion.service;

import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

@Component
public class GcsHelper {

    @Autowired
    private Storage storage;

    public InputStream fetchFromGCS(String bucketName, String blobName) {
        Blob blob = storage.get(BlobId.of(bucketName, blobName));

        if (blob == null) {
            throw new IllegalArgumentException("Blob not found: " + blobName);
        }

        ReadChannel reader = blob.reader();
        return Channels.newInputStream(reader);
    }

    public OutputStream prepareOutputStream(String bucketName, String blobName, String mimeType) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();

        WriteChannel writer = storage.writer(blobInfo);
        return Channels.newOutputStream(writer);
    }
}
