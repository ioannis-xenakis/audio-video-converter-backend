package com.johnxenakis.converter.dto;

public class JobPayload {
    private String fileId;
    private String mimeType;
    private String outputFormat;

    // Default constructor (needed by Jackson)
    public JobPayload() {}

    public JobPayload(String fileId, String mimeType, String outputFormat) {
        this.fileId = fileId;
        this.mimeType = mimeType;
        this.outputFormat = outputFormat;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override
    public String toString() {
        return "JobPayload:{" +
                "fileId='" + fileId + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", outputFormat='" + outputFormat + '\'' +
                '}';
    }
}
