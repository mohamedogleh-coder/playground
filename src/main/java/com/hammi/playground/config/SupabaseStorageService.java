//package com.hammi.playground.config;
//
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class SupabaseStorageService {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final SupabaseProperties supabaseProperties;
//
//    public SupabaseStorageService(SupabaseProperties supabaseProperties) {
//        this.supabaseProperties = supabaseProperties;
//    }
//
//    private HttpHeaders baseHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("apikey", supabaseProperties.getSecretKey());
//        return headers;
//    }
//
//    public String uploadFile(MultipartFile file, String bucket, String path) throws IOException {
//        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);
//
//        HttpHeaders headers = baseHeaders();
//        MediaType contentType = file.getContentType() != null
//                ? MediaType.parseMediaType(file.getContentType())
//                : MediaType.APPLICATION_OCTET_STREAM;
//        headers.setContentType(contentType);
//
//        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new IllegalStateException("Upload wuu fashilmay: " + response.getBody());
//        }
//
//        return "%s/storage/v1/object/public/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);
//    }
//
//    public void deleteFile(String bucket, String path) {
//        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);
//        HttpEntity<Void> request = new HttpEntity<>(baseHeaders());
//        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
//    }
//
//    public void deleteFiles(String bucket, List<String> paths) {
//        String url = "%s/storage/v1/object/%s".formatted(supabaseProperties.getUrl(), bucket);
//
//        HttpHeaders headers = baseHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("prefixes", paths), headers);
//        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
//    }
//}

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

    public SupabaseStorageService(SupabaseProperties supabaseProperties) {
        this.supabaseProperties = supabaseProperties;
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseProperties.getSecretKey());
        return headers;
    }

    public String uploadFile(MultipartFile file, String bucket, String path) throws IOException {
        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);

        HttpHeaders headers = baseHeaders();
        MediaType contentType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        headers.setContentType(contentType);

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Upload wuu fashilmay: " + response.getBody());
        }

        return "%s/storage/v1/object/public/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);
    }

    public List<String> uploadFiles(List<MultipartFile> files, String bucket, String folderPrefix) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();
        List<String> uploadedPaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String path = buildPath(folderPrefix, file.getOriginalFilename());
                String url = uploadFile(file, bucket, path);
                uploadedPaths.add(path);
                uploadedUrls.add(url);
            }
            return uploadedUrls;
        } catch (Exception ex) {
            if (!uploadedPaths.isEmpty()) {
                deleteFiles(bucket, uploadedPaths);
            }
            throw ex;
        }
    }

    private String buildPath(String folderPrefix, String originalFilename) {
        String safeName = (originalFilename == null || originalFilename.isBlank())
                ? "file"
                : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        String prefix = folderPrefix.endsWith("/") ? folderPrefix : folderPrefix + "/";
        return "%s%s-%s".formatted(prefix, UUID.randomUUID(), safeName);
    }

    public void deleteFile(String bucket, String path) {
        String url = "%s/storage/v1/object/%s/%s".formatted(supabaseProperties.getUrl(), bucket, path);
        HttpEntity<Void> request = new HttpEntity<>(baseHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

    public void deleteFiles(String bucket, List<String> paths) {
        String url = "%s/storage/v1/object/%s".formatted(supabaseProperties.getUrl(), bucket);

        HttpHeaders headers = baseHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(Map.of("prefixes", paths), headers);
        restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }
}