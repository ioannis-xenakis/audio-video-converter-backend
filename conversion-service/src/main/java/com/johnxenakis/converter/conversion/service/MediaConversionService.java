package com.johnxenakis.converter.conversion.service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;

import java.nio.file.Path;

public class MediaConversionService {
    public void convertMedia(Path inputPath, Path outputPath, String outputFormat) {
        FFmpeg.atPath() // assumes ffmpeg is at system PATH
                .addInput(UrlInput.fromPath(inputPath))
                .addOutput(UrlOutput.toPath(outputPath)
                        .setFormat(outputFormat)) // e.g. to ".mp3", ".wav", ".mp4", ".avi"
                .execute();
    }
}
