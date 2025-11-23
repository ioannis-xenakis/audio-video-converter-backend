package com.johnxenakis.converter.upload.model;

public class ConversionJob {
    private String blobName;
    private String mimeType;
    private String outputFormat;

    public ConversionJob() {}

    public ConversionJob(String blobName, String mimeType, String outputFormat) {
        this.blobName = blobName;
        this.mimeType = mimeType;
        this.outputFormat = outputFormat;
    }

    public String getBlobName() { return blobName; }
    public String getMimeType() { return mimeType; }
    public String getOutputFormat() { return outputFormat; }

    public void setBlobName(String blobName) { this.blobName = blobName; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
}

