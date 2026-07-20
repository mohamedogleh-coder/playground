//package com.hammi.playground.modules.fields;
//
//import com.hammi.playground.config.SupabaseStorageService;
//import com.hammi.playground.exceptions.NotFoundException;
//import com.hammi.playground.modules.events.*;
//import com.hammi.playground.modules.stadium.StadiumRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//import tools.jackson.core.type.TypeReference;
//import tools.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.time.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class FieldsService {
//    private final FieldRepository fieldRepository;
//    private final StadiumRepository stadiumRepository;
//    private final SupabaseStorageService supabaseStorageService;
//    private final JdbcTemplate jdbcTemplate;
//    private final ObjectMapper objectMapper;
//
//
//    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
//        String query = """
//                 SELECT f.id, f.cost, f.capacity,
//                        CASE
//                            WHEN count(fi.id) = 0 THEN null
//                            ELSE json_agg(fi.image_path)
//                            END as image_paths
//                 FROM fields f
//                          LEFT JOIN public.field_images fi ON f.id = fi.field_id
//                 WHERE f.stadium_id = ?
//                 GROUP BY f.id, f.cost, f.capacity;
//                """;
//
//        return jdbcTemplate.query(query, (rs, _) -> {
//
//            String jsonImages = rs.getString("image_paths");
//            List<String> imagePaths = new ArrayList<>();
//
//            if (jsonImages != null) {
//                imagePaths = objectMapper.readValue(jsonImages, new TypeReference<>() {
//                });
//            }
//
//            return new FieldResponse(rs.getShort("id"),
//                    rs.getShort("capacity"), rs.getBigDecimal("cost"),
//                    imagePaths.stream().map(supabaseStorageService::getPublicUrl).toList());
//        }, stadiumId);
//    }
//
//    @Transactional
//    public FieldResponse addField(UUID stadiumId, FieldRequest request, List<MultipartFile> imageFiles) {
//
//        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//        var newField = Field.builder()
//                .cost(request.cost())
//                .capacity(request.capacity())
//                .stadium(stadium)
//                .build();
//        var savedField = fieldRepository.save(newField);
//
//        List<String> imageUrls = new ArrayList<>();
//
//        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.getFirst().isEmpty()) {
//            try {
//                String folderPrefix = "stadiums/" + stadiumId + "/fields/" + savedField.getId();
//
//                imageUrls = supabaseStorageService.uploadFiles(imageFiles, folderPrefix);
//                System.out.println("Generated urls " + imageUrls);
//                List<FieldImage> fieldImagesToSave = new ArrayList<>();
//
//                for (String path : imageUrls) {
//                    FieldImage fieldImage = FieldImage.builder()
//                            .imagePath(path)
//                            .field(savedField)
//                            .build();
//                    fieldImagesToSave.add(fieldImage);
//                }
//
//                if (!fieldImagesToSave.isEmpty()) {
//                    savedField.getFieldImages().addAll(fieldImagesToSave);
//                    fieldRepository.save(savedField);
//                }
//
//            } catch (IOException e) {
//                throw new RuntimeException("Upload-kii sawirrada waa fashilmay: " + e.getMessage());
//            }
//        }
//
//        return new FieldResponse(
//                savedField.getId(),
//                savedField.getCapacity(),
//                savedField.getCost(),
//                imageUrls.stream().map(supabaseStorageService::getPublicUrl).toList()
//        );
//    }
//
//
//    @Transactional
//    public FieldResponse updateField(UUID stadiumId, Short fieldId, FieldRequest request, List<MultipartFile> imageFiles) {
//
//        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//        var field = stadium.getFields().stream().findFirst().filter(f -> f.getId().equals(fieldId))
//                .orElseThrow(() -> new NotFoundException("Field not exists"));
//
//        field.setCapacity(request.capacity());
//        field.setCost(request.cost());
//
//        if (!field.getFieldImages().isEmpty() && (!imageFiles.isEmpty() && !imageFiles.getFirst().isEmpty())){
//
//        }
//
//        var updatedField = fieldRepository.save(field);
//        return new FieldResponse(
//                updatedField.getId(),
//                updatedField.getCapacity(),
//                updatedField.getCost(),
//                imageUrls.stream().map(supabaseStorageService::getPublicUrl).toList()
//    }
//
//
//}
//


package com.hammi.playground.modules.fields;

import com.hammi.playground.config.SupabaseStorageService;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldsService {
    private final FieldRepository fieldRepository;
    private final FieldImageRepository fieldImageRepository;
    private final StadiumRepository stadiumRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        String query = """
                 SELECT f.id,
                        f.cost,
                        f.capacity,
                        CASE
                            WHEN count(fi.id) = 0 THEN null
                            ELSE json_agg(json_build_object('image_id', fi.id, 'image_path', fi.image_path))
                        END AS image_paths
                 FROM fields f
                 LEFT JOIN public.field_images fi ON f.id = fi.field_id
                 WHERE f.stadium_id = ?
                 GROUP BY f.id, f.cost, f.capacity;
                """;

        return jdbcTemplate.query(query, (rs, rowNum) -> {
            String jsonImages = rs.getString("image_paths");
            List<FieldImageResponse> fieldImages = new ArrayList<>();

            if (jsonImages != null) {
                try {
                    List<Map<String, Object>> imageList = objectMapper.readValue(jsonImages, new TypeReference<>() {
                    });

                    for (Map<String, Object> img : imageList) {
                        Number imgIdNum = (Number) img.get("image_id");
                        Short imageId = imgIdNum != null ? imgIdNum.shortValue() : null;

                        String rawPath = (String) img.get("image_path");
                        String publicUrl = null;

                        if (rawPath != null) {
                            publicUrl = supabaseStorageService.getPublicUrl(rawPath);
                        }
                        fieldImages.add(new FieldImageResponse(imageId, publicUrl));
                    }
                } catch (Exception e) {
                    System.err.println("JSON Parsing error: " + e.getMessage());
                }
            }
            return new FieldResponse(rs.getShort("id"), rs.getShort("capacity"), rs.getBigDecimal("cost"), fieldImages);
        }, stadiumId);
    }

    @Transactional
    public FieldResponse addField(UUID stadiumId, FieldRequest request, List<MultipartFile> imageFiles) {

        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var newField = Field.builder().cost(request.cost()).capacity(request.capacity()).stadium(stadium).build();
        var savedField = fieldRepository.save(newField);


        List<FieldImageResponse> fieldImages = new ArrayList<>();

        if (hasImages(imageFiles)) {
            fieldImages = addNewImages(stadiumId, savedField, imageFiles);
        }
        return new FieldResponse(savedField.getId(), savedField.getCapacity(), savedField.getCost(), fieldImages);
    }

    @Transactional
    public FieldResponse updateField(UUID stadiumId, Short fieldId, FieldRequest request, List<MultipartFile> imageFiles) {

        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));


        var field = stadium.getFields().stream().filter(f -> f.getId().equals(fieldId)).findFirst().orElseThrow(() -> new NotFoundException("Field not exists"));


        field.setCapacity(request.capacity());
        field.setCost(request.cost());

        List<FieldImageResponse> fieldImages = new ArrayList<>(field.getFieldImages().stream().map(image -> new FieldImageResponse(image.getId(), image.getImagePath())).toList());

        if (hasImages(imageFiles)) {
            fieldImages.addAll(addNewImages(stadiumId, field, imageFiles));
        }


        Field savedField = fieldRepository.save(field);


        return new FieldResponse(savedField.getId(), savedField.getCapacity(), savedField.getCost(), fieldImages);
    }

    private List<FieldImageResponse> addNewImages(UUID stadiumId, Field field, List<MultipartFile> imageFiles) {
        try {
            String folderPrefix = "stadiums/" + stadiumId + "/fields/" + field.getId();

            List<String> imagePaths = supabaseStorageService.uploadFiles(imageFiles, folderPrefix);


            List<FieldImage> images = imagePaths.stream().map(path -> FieldImage.builder().imagePath(path).field(field).build()).toList();


            var savedImages = fieldImageRepository.saveAll(images);

            return savedImages.stream().map(image -> new FieldImageResponse(image.getId(), image.getImagePath())).toList();

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    private boolean hasImages(List<MultipartFile> imageFiles) {
        return imageFiles != null && !imageFiles.isEmpty() && imageFiles.stream().anyMatch(file -> !file.isEmpty());
    }
}