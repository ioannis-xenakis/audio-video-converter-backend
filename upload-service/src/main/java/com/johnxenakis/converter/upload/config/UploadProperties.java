package com.johnxenakis.converter.upload.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private List<String> allowedExtensions;
    private List<String> allowedMimetypes;
    private String bucketName;

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }
    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedMimetypes() {
        return allowedMimetypes;
    }
    public void setAllowedMimetypes(List<String> allowedMimetypes) {
        this.allowedMimetypes = allowedMimetypes;
    }

    public String getBucketName() {
        return bucketName;
    }
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void logProps() {
        System.out.println("UploadProps loaded. Allowed extensions: " + allowedExtensions);
        System.out.println("Allowed MIME types: " + allowedMimetypes);
    }

}
