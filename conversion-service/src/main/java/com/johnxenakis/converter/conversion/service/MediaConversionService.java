package com.johnxenakis.converter.conversion.service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.johnxenakis.converter.conversion.config.FFmpegConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

@Service
public class MediaConversionService {
    @Autowired
    private FFmpegConfig ffmpegConfig;

    public void convertMedia(Path inputPath, Path outputPath, String outputFormat,
                             Map<String, String> codecs, Map<String, String> arguments) {
        Path ffmpegExecutable = ffmpegConfig.getFFmpegPath().resolve("ffmpeg.exe");
        UrlOutput urlOutput = UrlOutput.toPath(outputPath);

        // Video and audio codec names.
        String videoCodec = resolveVideoCodec(outputFormat, codecs);
        String audioCodec = resolveAudioCodec(outputFormat, codecs);

        if (videoCodec != null) {
            urlOutput.setCodec(StreamType.VIDEO, videoCodec);
        }
        if (audioCodec != null) {
            urlOutput.setCodec(StreamType.AUDIO, audioCodec);
        }

        // Add extra arguments.
        if (arguments != null && !arguments.isEmpty()) {
            arguments.forEach(urlOutput::addArguments);
        }

        if (Objects.equals(outputFormat, "wmv")) {
            outputFormat = "asf";
        }

        FFmpeg.atPath(ffmpegExecutable.getParent())
                .addInput(UrlInput.fromPath(inputPath))
                .addOutput(urlOutput.setFormat(outputFormat))
                .execute();
    }

    private String resolveVideoCodec(String format, Map<String, String> codecs) {
        if (codecs != null && codecs.containsKey("video")) {
            return codecs.get("video");
        }

        return switch (format.toLowerCase()) {
            case "wmv" -> "msmpeg4";
            case "mp4" -> "libx264";
            case "avi" -> "mpeg4";
            default -> null;
        };
    }

    private String resolveAudioCodec(String format, Map<String, String> codecs) {
        if (codecs != null && codecs.containsKey("audio")) {
            return codecs.get("audio");
        }

        return switch (format.toLowerCase()) {
            case "wmv" -> "wmav2";
            case "mp4" -> "aac";
            case "avi" -> "mp3";
            case "mp3" -> "libmp3lame";
            case "wav" -> "pcm_s16le";
            default -> null;
        };
    }
}
