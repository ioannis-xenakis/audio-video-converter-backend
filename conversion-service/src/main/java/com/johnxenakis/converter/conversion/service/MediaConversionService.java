package com.johnxenakis.converter.conversion.service;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;
import com.johnxenakis.converter.conversion.config.ConvertConfig;
import com.johnxenakis.converter.conversion.config.FFmpegConfig;
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

        long estimatedSizeBytes = getEstimatedSize(estimatedSize, bitrateKbps, durationSeconds);

        //Prepare GCS output
        String convertedBlobName = fileId + "_converted." + outputFormat;

        logger.info("Duration: {} seconds", durationSeconds);
        logger.info("Bitrate: {} kbps", bitrateKbps);

        // If format requiring seeking, or the estimated size is beyond the 1GB threshold, do stream the file,
        // else, use the temp file path to convert.
        boolean useTempFile = SmartOutputStrategy.FORMATS_REQUIRING_SEEK.contains(outputFormat.toLowerCase()) || estimatedSizeBytes > 1_000_000_000L;

        if (!useTempFile) {

            PipedOutputStream pipedOutputStream =
                    new PipedOutputStream();

            PipedInputStream pipedInputStream =
                    new PipedInputStream(
                            pipedOutputStream,
                            1024 * 1024
                    );

            CompletableFuture<Void> uploadTask =
                    CompletableFuture.runAsync(() -> {

                        storageClient.uploadStream(
                                pipedInputStream,
                                convertedBlobName,
                                mimeType,
                                bucketType
                        );

                    });

            mediaConversionUtil.convert(
                    outputFormat,
                    null,
                    pipedOutputStream,
                    estimatedSizeBytes,
                    ffmpegExecutable,
                    ffmpegStream,
                    codecs,
                    arguments);

            pipedOutputStream.close();

            uploadTask.join();

            logger.info(
                    "Conversion via PipeOutput completed"
            );
        } else {
            // Converting via temp file
            logger.info(
                    "Conversion via temp file started"
            );

            mediaConversionUtil.convert(
                    outputFormat,
                    tempFilePath,
                    null,
                    estimatedSizeBytes,
                    ffmpegExecutable,
                    ffmpegStream,
                    codecs,
                    arguments);

            try (InputStream resultStream =
                         Files.newInputStream(tempFilePath)) {

                storageClient.uploadStream(
                        resultStream,
                        convertedBlobName,
                        mimeType,
                        bucketType
                );
            }

            Files.deleteIfExists(tempFilePath); // Clean the temp file

            logger.info("Conversion via temp file completed");
        }
    }

    private long getEstimatedSize(long tempEstimatedSize, long bitrateKbps, double durationSeconds) {
        // fiveGB's(5GB) real value in "long" variable type, is 5_368_709_120L
        long fiveGB = 5L * 1024 * 1024 * 1024;
        if (tempEstimatedSize > fiveGB) {
            tempEstimatedSize = (bitrateKbps * 1000 / 8) * (long) durationSeconds;
        }
        return tempEstimatedSize;
    }
}
