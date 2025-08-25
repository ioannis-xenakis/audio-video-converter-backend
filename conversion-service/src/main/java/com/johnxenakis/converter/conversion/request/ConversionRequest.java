package com.johnxenakis.converter.conversion.request;

public class ConversionRequest {
    private String inputPath;
    private String outputPath;
    private String format;

    public ConversionRequest() {
    }

    public ConversionRequest(String inputPath, String outputPath, String format) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.format = format;
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
}
