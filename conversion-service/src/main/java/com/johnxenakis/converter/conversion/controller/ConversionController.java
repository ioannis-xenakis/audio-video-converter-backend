package com.johnxenakis.converter.conversion.controller;

import com.johnxenakis.converter.conversion.config.ConvertConfig;
import com.johnxenakis.converter.conversion.config.GCSConfig;
import com.johnxenakis.converter.conversion.service.GcsHelper;
import com.johnxenakis.converter.conversion.service.MediaConversionService;
import com.johnxenakis.converter.conversion.util.MimeTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/convert")
public class ConversionController {
    @Autowired
    private MediaConversionService conversionService;
    @Autowired
    private ConvertConfig convertConfig;
    @Autowired
    private GcsHelper gcsHelper;
    @Autowired
    private GCSConfig gcsConfig;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> convertFromGCS(
            @RequestPart("blobName") String blobName,
            @RequestPart("format") String format,
            @RequestPart(value = "mimeType", required = false) String mimeType,
            @RequestPart(value = "codecs", required = false) Map<String, String> codecs,
            @RequestPart(value = "arguments", required = false) Map<String, String> arguments
    ) {
        String convertedBlobName = blobName + "_converted." + format;

        // Automatically resolve MIME type from format
        String mimeTypeResult = MimeTypeUtil.resolveMimeType(format, mimeType);

        StreamingResponseBody responseBody = outputStream -> {
            conversionService.convertMedia(
                    1L,
                    gcsConfig.getInputBucket(),
                    gcsConfig.getOutputBucket(),
                    blobName,
                    mimeTypeResult,
                    format,
                    codecs,
                    arguments
            );

            // Always read the final converted file from GCS
            try (InputStream gcsStream =
                         gcsHelper.fetchFromGCS(gcsConfig.getOutputBucket(), convertedBlobName)) {
                gcsStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"" + convertedBlobName + "\""
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
}
