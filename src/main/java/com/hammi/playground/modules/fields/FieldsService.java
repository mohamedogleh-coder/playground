package com.hammi.playground.modules.fields;

import com.hammi.playground.config.SupabaseStorageService;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.*;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldsService {
    private final FieldRepository fieldRepository;
    private final StadiumRepository stadiumRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        String query = """
                 SELECT f.id, f.cost, f.capacity,
                        CASE
                            WHEN count(fi.id) = 0 THEN null
                            ELSE json_agg(fi.image_path)
                            END as image_paths
                 FROM fields f
                          LEFT JOIN public.field_images fi ON f.id = fi.field_id
                 WHERE f.stadium_id = ?
                 GROUP BY f.id, f.cost, f.capacity;
                """;

        return jdbcTemplate.query(query, (rs, _) -> {

            String jsonImages = rs.getString("image_paths");
            List<String> imagePaths = new ArrayList<>();

            if (jsonImages != null) {
                imagePaths = objectMapper.readValue(jsonImages, new TypeReference<>() {
                });
            }

            return new FieldResponse(rs.getShort("id"),
                    rs.getShort("capacity"), rs.getBigDecimal("cost"),
                    imagePaths.stream().map(supabaseStorageService::getPublicUrl).toList());
        }, stadiumId);
    }

    @Transactional
    public FieldResponse addField(UUID stadiumId, FieldRequest request, List<MultipartFile> imageFiles) {

        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var newField = Field.builder()
                .cost(request.cost())
                .capacity(request.capacity())
                .stadium(stadium)
                .build();
        var savedField = fieldRepository.save(newField);

        List<String> imageUrls = new ArrayList<>();

        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
            try {
                String folderPrefix = "stadiums/" + stadiumId + "/fields/" + savedField.getId();

                imageUrls = supabaseStorageService.uploadFiles(imageFiles, folderPrefix);
                System.out.println("Generated urls " + imageUrls);
                List<FieldImage> fieldImagesToSave = new ArrayList<>();

                for (String path : imageUrls) {
                    FieldImage fieldImage = FieldImage.builder()
                            .imagePath(path)
                            .field(savedField)
                            .build();
                    fieldImagesToSave.add(fieldImage);
                }

                if (!fieldImagesToSave.isEmpty()) {
                    savedField.getFieldImages().addAll(fieldImagesToSave);
                    fieldRepository.save(savedField);
                }

            } catch (IOException e) {
                throw new RuntimeException("Upload-kii sawirrada waa fashilmay: " + e.getMessage());
            }
        }

        return new FieldResponse(
                savedField.getId(),
                savedField.getCapacity(),
                savedField.getCost(),
                imageUrls.stream().map(supabaseStorageService::getPublicUrl).toList()
        );
    }

//
//    @Transactional
//    public FieldResponse addField(UUID stadiumId, FieldRequest request, List<MultipartFile> imageFiles) {
//        // 1. Soo hel Stadium-ka
//        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//        // 2. Samee oo Save-garee Field-ka cusub
//        var newField = Field.builder()
//                .cost(request.cost())
//                .capacity(request.capacity())
//                .stadium(stadium)
//                .build();
//        var savedField = fieldRepository.save(newField);
//
//        List<String> imageUrls = new ArrayList<>();
//
//        // 3. Upload-garee sawirrada haddii Flutter laga soo diray
//        if (imageFiles != null && !imageFiles.isEmpty()) {
//            try {
//                // Halkan ku baas (List-ka files-ka, magaca bucket-kaaga, iyo folder-ka lagu dhex ridayo)
//
//                String folderPrefix = "stadiums/" + stadiumId + "/fields/" + savedField.getId();
//
//                imageUrls = supabaseStorageService.uploadFiles(imageFiles, folderPrefix);
//                System.out.println(imageUrls);
//                // 4. URL-yada soo baxay mid mid ugu samee FieldImage oo la xidhiidha Field-ka rasmiga ah
//                for (String url : imageUrls) {
//                    FieldImage fieldImage = FieldImage.builder()
//                            .imagePath(url)
//                            .field(savedField)
//                            .build();
//                    // Ku dar liiska sawirrada ee field-ka (Maadaama uu CascadeType.ALL leeyahay si toos ah ayuu u save-garmayaa)
//                    savedField.getFieldImages().add(fieldImage);
//                }
//
//                fieldRepository.save(savedField);
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
//                imageUrls
//        );
//    }


}

