package com.johnxenakis.converter.upload.service;

import com.johnxenakis.converter.upload.exception.FileValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "upload.bucketName=audio-video-converter-files",
        "upload.allowedExtensions=mp4",
        "upload.allowedMimetypes=video/mp4"
})
public class FileStorageServiceTest {
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testUploadToGCS() throws Exception {
        // Create a mock MP4 file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "dummy video content".getBytes()
        );

        // Attempt to store the file
        String fileId = fileStorageService.store(mockFile);

        // Assert that a file ID is returned
        assertNotNull(fileId);
        assertTrue(fileId.endsWith(".mp4"));

        System.out.println("Uploaded file ID: " + fileId);
    }

    @Test
    public void testPreventOverwriteWithFixedID() throws Exception {
        // Create a mock MP4 file
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "dummy video content".getBytes()
        );

        String fixedId = "test-fixed-id.mp4";

        // First upload should succeed
        String fileId1 = fileStorageService.storeWithFixedId(mockFile, fixedId);
        assertNotNull(fileId1);

        // Second upload with same ID should fail
        assertThrows(FileValidationException.class, () -> {
            fileStorageService.storeWithFixedId(mockFile, fixedId);
        });
    }

    @Test
    public void testVersioningWhenFileAlreadyExists() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "versioned-video.mp4", "video/mp4", "dummy video content".getBytes()
        );

        String firstBlobName = fileStorageService.store(mockFile);
        assertEquals("versioned-video.mp4", firstBlobName);

        String secondBlobName = fileStorageService.store(mockFile);
        assertTrue(secondBlobName.startsWith("versioned-video_v"));
        assertTrue(secondBlobName.endsWith(".mp4"));
        assertNotEquals(firstBlobName, secondBlobName);

        System.out.println("First blob: " + firstBlobName);
        System.out.println("Second blob: " + secondBlobName);
    }

    @Test
    public void testOverwriteBlockedWhenFlagIsFalse() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "overwrite-test.mp4", "video/mp4", "dummy video content".getBytes()
        );

        String blobName = fileStorageService.storeWithFixedId(mockFile, "overwrite-test.mp4");
        assertEquals("overwrite-test.mp4", blobName);

        assertThrows(FileValidationException.class, () -> fileStorageService.storeWithFixedId(mockFile, "overwrite-test.mp4"));
    }
}
