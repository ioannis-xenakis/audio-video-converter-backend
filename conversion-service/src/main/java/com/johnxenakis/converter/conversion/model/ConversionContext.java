package com.johnxenakis.converter.conversion.model;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public record ConversionContext(
        String outputFormat,
        Path ffmpegExecutable,
        InputStream ffmpegStream,
        Map<String, String> codecs,
        Map<String, String> arguments,

        String convertedBlobName,
        String mimeType,
        String bucketType,

        long estimatedSizeBytes
) {
}
