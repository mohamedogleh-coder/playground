package com.hammi.playground.config;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SupabaseProperties supabaseProperties;

    public static final String BUCKET = "playground";

    public SupabaseStorageService(SupabaseProperties supabaseProperties) {
        this.supabaseProperties = supabaseProperties;
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseProperties.getSecretKey());
        return headers;
    }

    public String uploadFile(MultipartFile file, String path) throws IOException {
        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), BUCKET, path);

        HttpHeaders headers = baseHeaders();
        MediaType contentType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        headers.setContentType(contentType);

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Can't upload image" + response.getBody());
        }

        return path;
    }

    public List<String> uploadFiles(List<MultipartFile> files, String folderPrefix) throws IOException {
        List<String> uploadedPaths = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String path = buildPath(folderPrefix, file);
                String savedPath = uploadFile(file, path);
                uploadedPaths.add(savedPath);
            }
            return uploadedPaths;
        } catch (Exception ex) {
            if (!uploadedPaths.isEmpty()) {
                deleteFiles(uploadedPaths);
            }
            throw ex;
        }
    }


    public void deleteFile(String path) {
        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), BUCKET, path);
        HttpEntity<Void> request = new HttpEntity<>(baseHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

    public void deleteFiles(List<String> paths) {
        String url = "%s/storage/v1/object/%s".formatted(supabaseProperties.getUrl(), BUCKET);

        HttpHeaders headers = baseHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("prefixes", paths), headers);
        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

    public String getPublicUrl(String path) {
        return "%s/storage/v1/object/public/%s/%s".formatted(supabaseProperties.getUrl(), BUCKET, path);
    }

    private String buildPath(String folderPrefix, MultipartFile file) {
        String extension = ".jpg";

        String contentType = file.getContentType();
        if (contentType != null) {
            extension = switch (contentType) {
                case "image/png" -> ".png";
                case "image/gif" -> ".gif";
                case "image/webp" -> ".webp";
                case "image/jpeg", "image/jpg" -> ".jpg";
                default -> extension;
            };
        } else if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            String origName = file.getOriginalFilename();
            extension = origName.substring(origName.lastIndexOf(".")).toLowerCase();
        }

        String randomFileName = UUID.randomUUID().toString() + extension;

        String prefix = folderPrefix.endsWith("/") ? folderPrefix : folderPrefix + "/";
        return "%s%s".formatted(prefix, randomFileName);
    }

}