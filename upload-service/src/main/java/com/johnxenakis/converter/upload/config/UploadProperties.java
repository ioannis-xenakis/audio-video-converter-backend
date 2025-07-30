package com.johnxenakis.converter.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {
    private String dir;
    private String allowedExtensions;
    private String allowedMimetypes;

    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }

    public List<String> getAllowedExtensions() {
        return Arrays.asList(allowedExtensions.split(","));
    }
    public void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedMimetypes() {
        return Arrays.asList(allowedMimetypes.split(","));
    }
    public void setAllowedMimetypes(String allowedMimetypes) {
        this.allowedMimetypes = allowedMimetypes;
    }

}
