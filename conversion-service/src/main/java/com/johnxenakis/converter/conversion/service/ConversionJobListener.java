package com.johnxenakis.converter.conversion.service;

import com.johnxenakis.converter.conversion.util.MimeTypeUtil;
import com.johnxenakis.converter.dto.JobPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConversionJobListener {
    private static final Logger logger = LoggerFactory.getLogger(ConversionJobListener.class);

    private final MediaConversionService mediaConversionService;

    @Autowired
    public ConversionJobListener(
            MediaConversionService mediaConversionService) {
        this.mediaConversionService = mediaConversionService;
    }

    @KafkaListener(
            topics = "conversion-jobs",
            groupId = "conversion-service",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(JobPayload job) throws Exception {
        logger.info("Received job: {}", job);

        // Automatically resolve MIME type from format
        String mimeTypeResult = MimeTypeUtil.resolveMimeType(job.getOutputFormat(), null);
        logger.info("Auto-resolved mime type by MimeTypeUtil: {}", mimeTypeResult);
        job.setMimeType(mimeTypeResult);

        String fileId = job.getFileId();
        String mimeType = job.getMimeType();
        String outputFormat = job.getOutputFormat();

        try {
            mediaConversionService.convertMedia(
                    0L, // estimated size placeholder
                    "converted",
                    fileId,
                    mimeType,
                    outputFormat,
                    null, // codecs
                    null  // extra args
            );
            logger.info("Conversion completed for {}", fileId);
        } catch (Exception e) {
            logger.error("Conversion failed for " + fileId + ": " + e.getMessage());
        }
    }
}
