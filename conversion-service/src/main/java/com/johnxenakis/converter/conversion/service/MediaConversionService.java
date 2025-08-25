package com.johnxenakis.converter.conversion.service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.johnxenakis.converter.conversion.config.FFmpegConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class MediaConversionService {
    @Autowired
    private FFmpegConfig ffmpegConfig;

    public void convertMedia(Path inputPath, Path outputPath, String outputFormat) {
        Path ffmpegExecutable = ffmpegConfig.getFFmpegPath().resolve("ffmpeg.exe");

        FFmpeg.atPath(ffmpegExecutable.getParent())
                .addInput(UrlInput.fromPath(inputPath))
                .addOutput(UrlOutput.toPath(outputPath)
                        .setFormat(outputFormat)) // e.g. to ".mp3", ".wav", ".mp4", ".avi"
                .execute();
    }
}
