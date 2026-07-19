package com.hammi.playground.config;

import com.hammi.playground.config.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final SupabaseStorageService storageService;


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bucket") String bucket,
            @RequestParam("path") String path) throws IOException {

        String publicUrl = storageService.uploadFile(file, bucket, path);
        return ResponseEntity.ok(Map.of("url", publicUrl));
    }

    @DeleteMapping("/object")
    public ResponseEntity<Void> delete(
            @RequestParam String bucket,
            @RequestParam String path) {

        storageService.deleteFile(bucket, path);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/objects")
    public ResponseEntity<Void> deleteMany(
            @RequestParam String bucket,
            @RequestBody List<String> paths) {

        storageService.deleteFiles(bucket, paths);
        return ResponseEntity.noContent().build();
    }
}