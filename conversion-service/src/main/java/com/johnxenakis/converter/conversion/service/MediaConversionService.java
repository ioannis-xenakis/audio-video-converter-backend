package com.johnxenakis.converter.conversion.service;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import com.johnxenakis.converter.conversion.config.ConvertConfig;
import com.johnxenakis.converter.conversion.config.FFmpegConfig;
import com.johnxenakis.converter.conversion.model.ConversionContext;
import com.johnxenakis.converter.conversion.util.MediaConversionUtil;
import com.johnxenakis.converter.conversion.util.SmartOutputStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class MediaConversionService {
    @Autowired
    private FFmpegConfig ffmpegConfig;
    @Autowired
    private ConvertConfig convertConfig;
    @Autowired
    private StorageServiceClient storageClient;
    private static final Logger logger = LoggerFactory.getLogger(MediaConversionService.class);

    public void convertMedia(long estimatedSize, String bucketType,
                             String fileId, String mimeType, String outputFormat,
                             Map<String, String> codecs, Map<String, String> arguments) throws IOException {

        Path ffmpegExecutable = ffmpegConfig.getFFmpegPath().resolve("ffmpeg.exe");
        Path ffprobeExecutable = ffmpegConfig.getFFmpegPath().resolve("ffprobe.exe");
        MediaConversionUtil mediaConversionUtil = new MediaConversionUtil();
        double durationSeconds = 0;
        long bitrateKbps = 0;
        InputStream probeStream = storageClient.download(fileId);
        InputStream ffmpegStream = storageClient.download(fileId);
        if (ffmpegStream == null) {
            throw new IllegalArgumentException("InputStream ffmpegStream not found");
        }

        if (Objects.equals(outputFormat, "wmv")) {
            outputFormat = "asf";
        }

        Path tempFilePath =
                convertConfig.getConvertTempPath()
                        .resolve(
                                fileId
                                        + "_"
                                        + System.currentTimeMillis()
                                        + "."
                                        + outputFormat
                        );

        FFprobeResult result = FFprobe.atPath(ffprobeExecutable.getParent())
                .setShowStreams(true)
                .setInput(probeStream)
                .execute();

        for (Stream stream : result.getStreams()) {
            if (stream.getDuration() != null && stream.getDuration() > durationSeconds) {
                durationSeconds = stream.getDuration(); // longest stream duration
            }

            if (stream.getBitRate() != null && stream.getBitRate() > bitrateKbps) {
                bitrateKbps = stream.getBitRate() / 1000; // convert to kbps
            }
        }

        //Prepare GCS output
        String convertedBlobName = fileId + "_converted." + outputFormat;

        logger.info("Duration: {} seconds", durationSeconds);
        logger.info("Bitrate: {} kbps", bitrateKbps);

        long estimatedSizeBytes =
                mediaConversionUtil.getEstimatedSize(
                        estimatedSize,
                        bitrateKbps,
                        durationSeconds
                );

        ConversionContext context =
                new ConversionContext(
                        outputFormat,
                        ffmpegExecutable,
                        ffmpegStream,
                        codecs,
                        arguments,
                        convertedBlobName,
                        mimeType,
                        bucketType,
                        estimatedSizeBytes
                );

        // If format requiring seeking, or the estimated size is beyond the 1GB threshold, do stream the file,
        // else, use the temp file path to convert.
        boolean useTempFile = mediaConversionUtil.getUseTempFile(estimatedSize, bitrateKbps, durationSeconds, outputFormat);

        if (!useTempFile) {
            // Converting via streaming
            logger.info(
                    "Conversion via streaming has started"
            );

            mediaConversionUtil.convertViaStreaming(
                    context,
                    storageClient
            );

            logger.info(
                    "Conversion via streaming completed"
            );
        } else {
            // Converting via temp file
            logger.info(
                    "Conversion via temp file started"
            );

            mediaConversionUtil.convertViaTempFile(
                    outputFormat,
                    tempFilePath,
                    ffmpegExecutable,
                    ffmpegStream,
                    codecs,
                    arguments,
                    storageClient,
                    convertedBlobName,
                    mimeType,
                    bucketType);

            logger.info("Conversion via temp file completed");
        }
    }
}
