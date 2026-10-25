package com.johnxenakis.converter.conversion.util;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Output;
import com.github.kokorin.jaffree.ffmpeg.PipeInput;
import com.johnxenakis.converter.conversion.model.ConversionContext;
import com.johnxenakis.converter.conversion.service.StorageServiceClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MediaConversionUtil {

    private static long estimatedSizeBytes = 0;

    public void convertViaStreaming(
            ConversionContext context,
            StorageServiceClient storageClient
    ) throws IOException {

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
                            context.convertedBlobName(),
                            context.mimeType(),
                            context.bucketType()
                    );

                });

        convert(
                outputFormat,
                null,
                pipedOutputStream,
                ffmpegExecutable,
                ffmpegStream,
                codecs,
                arguments);

        pipedOutputStream.close();

        uploadTask.join();
    }

    public void convertViaTempFile(
            String outputFormat,
            Path tempFilePath,
            Path ffmpegExecutable,
            InputStream ffmpegStream,
            Map<String, String> codecs,
            Map<String, String> arguments,
            StorageServiceClient storageClient,
            String convertedBlobName,
            String mimeType,
            String bucketType) throws IOException {

        convert(
                outputFormat,
                tempFilePath,
                null,
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
    }

    private void convert(
            String outputFormat,
            Path tempFilePath,
            PipedOutputStream pipedOutputStream,
            Path ffmpegExecutable,
            InputStream ffmpegStream,
            Map<String, String> codecs,
            Map<String, String> arguments) {

        Output output =
                SmartOutputStrategy.chooseOutput(
                        outputFormat,
                        pipedOutputStream,
                        tempFilePath,
                        estimatedSizeBytes
                );

        FFmpeg ffmpeg = FFmpeg.atPath(
                        ffmpegExecutable.getParent()
                )
                .addInput(
                        PipeInput.pumpFrom(ffmpegStream)
                )
                .addOutput(output);

        // Video and audio codec names.
        addCodecs(ffmpeg, codecs, outputFormat);

        // Add extra arguments.
        addExtraArguments(ffmpeg, arguments);

        ffmpeg.execute();
    }

    private void addCodecs(FFmpeg ffmpeg, Map<String, String> codecs, String format) {
        String videoCodec = resolveVideoCodec(codecs, format);
        String audioCodec = resolveAudioCodec(codecs, format);

        if (videoCodec != null) {
            ffmpeg.addArguments("-c:v", videoCodec);
        }
        if (audioCodec != null) {
            ffmpeg.addArguments("-c:a", audioCodec);
        }
    }

    private String resolveVideoCodec(Map<String, String> codecs, String format) {
        if (codecs != null && codecs.containsKey("video")) {
            return codecs.get("video");
        }

        return switch (format.toLowerCase()) {
            case "3gp", "3g2"       -> "h263";         // Mobile formats
            case "amv"              -> "amv";          // AMV-specific codec
            case "apng"             -> "apng";         // Animated PNG
            case "asf", "wtv", "wmv" -> "msmpeg4";      // Windows formats
            case "avi"              -> "mpeg4";        // Legacy but safe
            case "avif"             -> "libaom-av1";   // AV1 image format
            case "dv"               -> "dvvideo";      // Digital Video
            case "f4v", "flv"       -> "flv";          // Flash Video
            case "gif"              -> "gif";          // Animated GIF
            case "h261"             -> "h261";         // Legacy
            case "h263"             -> "h263";         // Mobile
            case "h264", "mp4", "ismv", "psp", "m4v" -> "libx264"; // H.264 family
            case "hevc"             -> "libx265";      // H.265
            case "ivf", "webm"      -> "libvpx";       // VP8 for WebM
            case "mjpeg", "mpjpeg"  -> "mjpeg";        // MJPEG
            case "mkv", "matroska"  -> "libx264";      // Matroska
            case "mov", "qt"        -> "libx264";      // QuickTime
            case "mpeg", "vcd", "svcd", "vob", "dvd" -> "mpeg2video"; // MPEG family
            case "nut"              -> "libx264";      // Experimental
            case "ogv", "ogg"       -> "libtheora";    // Ogg Video
            case "rawvideo", "yuv4mpegpipe" -> "rawvideo"; // Uncompressed
            case "rm"               -> "rv10";         // RealMedia
            case "roq"              -> "roqvideo";     // RoQ format
            case "swf", "avm2"      -> "flv";          // Flash
            case "vc1", "vc1test"   -> "vc1";          // VC-1
            case "webp"             -> "libwebp";      // Animated WebP
            case "vvc"              -> "libvvc";       // H.266/VVC (if compiled)
            default                 -> null;
        };
    }

    private String resolveAudioCodec(Map<String, String> codecs, String format) {
        if (codecs != null && codecs.containsKey("audio")) {
            return codecs.get("audio");
        }

        return switch (format.toLowerCase()) {
            case "3gp", "3g2"       -> "aac";           // Mobile formats
            case "ac3"              -> "ac3";           // Dolby AC-3
            case "ac4"              -> "ac4";           // Dolby AC-4
            case "adts"             -> "aac";           // AAC in ADTS
            case "adx"              -> "adx";           // CRI ADX
            case "aea"              -> "pcm_s16le";     // Studio audio
            case "aiff"             -> "pcm_s16be";     // AIFF
            case "alaw"             -> "pcm_alaw";      // A-law
            case "amr"              -> "libopencore_amrnb"; // Narrowband AMR
            case "aptx"             -> "aptx";          // Bluetooth audio
            case "aptx_hd"          -> "aptx_hd";       // Bluetooth HD
            case "au"               -> "pcm_mulaw";     // Sun AU
            case "caf"              -> "aac";           // Apple CAF
            case "daud"             -> "pcm_s24be";     // D-Cinema audio
            case "dfpwm"            -> "dfpwm";         // Compressed waveform
            case "dts"              -> "dts";           // DTS
            case "eac3"             -> "eac3";          // Enhanced AC-3
            case "flac"             -> "flac";          // FLAC
            case "g722"             -> "g722";          // VoIP
            case "g723_1"           -> "g723_1";        // VoIP
            case "g726", "g726le"   -> "g726";          // VoIP
            case "gsm"              -> "gsm";           // GSM
            case "ircam"            -> "pcm_s16be";     // IRCAM
            case "lc3"              -> "lc3";           // Low Complexity Comm
            case "mp2"              -> "mp2";           // MPEG Layer 2
            case "mp3"              -> "libmp3lame";    // MPEG Layer 3
            case "mulaw"            -> "pcm_mulaw";     // μ-law
            case "oga", "ogg", "opus" -> "libopus";     // Ogg Opus
            case "oma"              -> "atrac3";        // Sony ATRAC
            case "sbc"              -> "sbc";           // Bluetooth
            case "sox"              -> "pcm_s16le";     // SoX
            case "spdif"            -> "ac3";           // IEC 61937
            case "spx"              -> "libspeex";      // Speex
            case "tta"              -> "tta";           // True Audio
            case "voc"              -> "pcm_u8";        // Creative Voice
            case "wav", "w64"       -> "pcm_s16le";     // WAV family
            case "wv"               -> "wavpack";       // WavPack
            case "webm", "webm_chunk" -> "libopus";     // WebM audio
            case "mp4", "m4v", "ismv", "psp" -> "aac";  // MP4 family
            case "mov"              -> "aac";           // QuickTime
            case "flv", "f4v"       -> "aac";           // Flash
            case "asf", "wtv"       -> "wmav2";         // Windows formats
            case "mxf", "mxf_d10", "mxf_opatom" -> "pcm_s16le"; // MXF
            case "vob", "dvd", "svcd", "vcd", "mpeg" -> "mp2"; // MPEG family
            default                 -> null;
        };
    }

    private void addExtraArguments(FFmpeg ffmpeg, Map<String, String> arguments) {

        if (arguments != null && !arguments.isEmpty()) {
            arguments.forEach(ffmpeg::addArguments);
        }
    }

    public long getEstimatedSize(long tempEstimatedSize, long bitrateKbps, double durationSeconds) {
        // fiveGB's(5GB) real value in "long" variable type, is 5_368_709_120L
        long fiveGB = 5L * 1024 * 1024 * 1024;
        if (tempEstimatedSize > fiveGB) {
            tempEstimatedSize = (bitrateKbps * 1000 / 8) * (long) durationSeconds;
        }
        return tempEstimatedSize;
    }

    public boolean getUseTempFile(long estimatedSize, long bitrateKbps, double durationSeconds, String outputFormat) {
        estimatedSizeBytes = getEstimatedSize(estimatedSize, bitrateKbps, durationSeconds);

        return SmartOutputStrategy.FORMATS_REQUIRING_SEEK.contains(outputFormat.toLowerCase()) || estimatedSizeBytes > 1_000_000_000L;
    }
}
