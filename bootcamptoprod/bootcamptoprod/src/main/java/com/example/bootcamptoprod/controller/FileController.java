package com.example.bootcamptoprod.controller;

import com.example.bootcamptoprod.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String url = fileStorageService.store(file);
        return ResponseEntity.ok(new UploadResponse(url));
    }

    public record UploadResponse(String url) {}
}
