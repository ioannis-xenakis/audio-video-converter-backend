package com.johnxenakis.converter.conversion.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcsStorageConfig {
    @Bean
    public Storage storage() {
        // Uses GOOGLE_APPLICATION_CREDENTIALS env var or default credentials
        return StorageOptions.getDefaultInstance().getService();
    }
}
