package com.johnxenakis.converter.conversion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GCSConfig {
    @Value("${buckets.input-bucket}")
    private String inputBucket;

    @Value("${buckets.output-bucket}")
    private String outputBucket;

    public String getInputBucket() {
        return inputBucket;
    }

    public String getOutputBucket() {
        return outputBucket;
    }
}
