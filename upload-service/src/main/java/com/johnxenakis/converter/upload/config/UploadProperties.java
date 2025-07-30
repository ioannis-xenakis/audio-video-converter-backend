package com.johnxenakis.converter.upload.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String dir;
    private List<String> allowedExtensions;
    private List<String> allowedMimetypes;

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }

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

    @PostConstruct
    public void logProps() {
        System.out.println("UploadProps loaded. Allowed extensions: " + allowedExtensions);
        System.out.println("Allowed MIME types: " + allowedMimetypes);
        System.out.println("Upload directory(dir): " + dir);
    }

}
