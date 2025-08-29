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

    private String resolveAudioCodec(String format, Map<String, String> codecs) {
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
}
