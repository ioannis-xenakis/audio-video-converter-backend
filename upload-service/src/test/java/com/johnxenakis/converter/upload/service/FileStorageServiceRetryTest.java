package com.johnxenakis.converter.upload.service;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.johnxenakis.converter.upload.config.UploadProperties;
import com.johnxenakis.converter.upload.exception.UploadFailureException;
import com.johnxenakis.converter.upload.websocket.UploadProgressHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FileStorageServiceRetryTest {

    @MockBean
    private Storage storage;

    @MockBean
    private UploadProgressHandler progressHandler;

    @MockBean
    private UploadProperties properties;

    @Autowired
    private FileStorageService fileStorageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(properties.getBucketName()).thenReturn("audio-video-converter-test-files");
        when(properties.getAllowedExtensions()).thenReturn(java.util.List.of("mp4"));
        when(properties.getAllowedMimetypes()).thenReturn(java.util.List.of("video/mp4"));
        when(properties.isAllowOverwrite()).thenReturn(false);
    }

    @Test
    void testUploadRetriesOnIOException() throws Exception {
        byte[] content = "dummy video content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", content);

        WriteChannel mockChannel = mock(WriteChannel.class);

        // Simulate IOException during write
        doThrow(new IOException("Simulated failure"))
                .when(mockChannel)
                .write(any());

        when(storage.get(any(BlobId.class))).thenReturn(null);
        when(storage.writer(any(BlobInfo.class))).thenReturn(mockChannel);

        assertThrows(UploadFailureException.class, () -> fileStorageService.store(file));
        verify(progressHandler).broadcastFailure(
                eq("video.mp4"),
                argThat((String msg) -> msg.contains("Upload failed after multiple"))
        );
        verify(storage, times(3)).writer(any(BlobInfo.class));
    }
}
