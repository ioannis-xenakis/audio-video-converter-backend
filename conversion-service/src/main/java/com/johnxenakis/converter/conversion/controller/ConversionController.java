package com.johnxenakis.converter.conversion.controller;

import com.johnxenakis.converter.conversion.service.MediaConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/convert")
public class ConversionController {
    @Autowired
    private MediaConversionService conversionService;

    @PostMapping
    public ResponseEntity<String> convert(@RequestBody ConversionRequest request) {
        Path input = Paths.get(request.getInputPath());
        Path output = Paths.get(request.getOutputPath());
        conversionService.convertMedia(input, output, request.getFormat());
        return ResponseEntity.ok("Conversion Started");
    }
}
