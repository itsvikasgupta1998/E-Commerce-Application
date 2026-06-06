package com.app.controllers;

import com.app.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;


    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String fileName
    ) throws IOException {

        InputStream inputStream =
                fileService.getResource(fileName);

        byte[] data =
                inputStream.readAllBytes();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.IMAGE_JPEG_VALUE
                )
                .body(data);
    }
}