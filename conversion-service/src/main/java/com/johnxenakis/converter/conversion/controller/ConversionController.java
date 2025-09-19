package com.johnxenakis.converter.conversion.controller;

import com.johnxenakis.converter.conversion.config.ConvertConfig;
import com.johnxenakis.converter.conversion.service.MediaConversionService;
import com.johnxenakis.converter.conversion.util.SmartOutputStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/convert")
public class ConversionController {
    @Autowired
    private MediaConversionService conversionService;
    @Autowired
    private ConvertConfig convertConfig;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> convert(
            @RequestPart("file") MultipartFile file,
            @RequestPart("format") String format,
            @RequestPart(value = "codecs", required = false) Map<String, String> codecs,
            @RequestPart(value = "arguments", required = false) Map<String, String> arguments
    ) {
        String outputFilename = "converted." + format;
        Path tempFilePath = convertConfig.getConvertTempPath().resolve(outputFilename);

        StreamingResponseBody responseBody = outputStream -> {
            conversionService.convertMedia(
                    file.getSize(),
                    file.getInputStream(),
                    outputStream,
                    format,
                    codecs,
                    arguments
            );

            // If SmartOutputStrategy used ChannelOutput, stream the temp file
            if (SmartOutputStrategy.FORMATS_REQUIRING_SEEK.contains(format)) {
                try (InputStream resultStream = Files.newInputStream(tempFilePath)) {
                    resultStream.transferTo(outputStream);
                    outputStream.flush();
                }
                Files.deleteIfExists(tempFilePath); // Clean the temp file
            }
        };
        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"" + outputFilename + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
}
