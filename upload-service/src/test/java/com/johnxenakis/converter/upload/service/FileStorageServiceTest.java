package com.johnxenakis.converter.upload.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
