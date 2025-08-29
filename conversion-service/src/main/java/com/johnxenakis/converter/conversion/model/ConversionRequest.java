package com.johnxenakis.converter.conversion.model;

import java.util.Map;

public class ConversionRequest {
    private String inputPath;
    private String outputPath;
    private String format;
    private Map<String, String> codecs;

    public ConversionRequest() {
    }

    public ConversionRequest(String inputPath, String outputPath, String format, Map<String, String> codecs) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.format = format;
        this.codecs = codecs;
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

    public Map<String, String> getCodecs() {
        return codecs;
    }

    public void setCodecs(Map<String, String> codecs) {
        this.codecs = codecs;
    }
}
