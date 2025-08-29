package com.johnxenakis.converter.conversion.model;

public class ConversionRequest {
    private String inputPath;
    private String outputPath;
    private String format;
    private String audioCodec;
    private String videoCodec;

    public ConversionRequest() {
    }

    public ConversionRequest(String inputPath, String outputPath, String format, String audioCodec, String videoCodec) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.format = format;
        this.audioCodec = audioCodec;
        this.videoCodec = videoCodec;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }
}
