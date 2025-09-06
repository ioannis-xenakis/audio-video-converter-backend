package com.johnxenakis.converter.conversion.util;

import com.github.kokorin.jaffree.ffmpeg.ChannelOutput;
import com.github.kokorin.jaffree.ffmpeg.Output;
import com.github.kokorin.jaffree.ffmpeg.PipeOutput;

import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SmartOutputStrategy {
    private static final List<String> FORMATS_REQUIRING_SEEK = List.of("wmv", "asf", "mkv", "matroska");

    public static Output chooseOutput(
            String format,
            OutputStream stream,
            Path tempFilePath,
            long estimatedSizeInBytes
    ) {
        boolean requiresSeek = FORMATS_REQUIRING_SEEK.contains(format.toLowerCase());
        boolean isLarge = estimatedSizeInBytes > 1_000_000_000L; // 1GB threshold

        if (requiresSeek || isLarge) {
            try {
                SeekableByteChannel channel = Files.newByteChannel(tempFilePath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                System.out.println("Returning output with ChannelOutput");
                return ChannelOutput.toChannel(null, channel).setFormat(format);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create ChannelOutput", e);
            }
        } else {
            System.out.println("Returning output with PipeOutput");
            return PipeOutput.pumpTo(stream).setFormat(format);
        }
    }
}