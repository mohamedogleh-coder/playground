
package com.hammi.playground.modules.fields;

import com.hammi.playground.config.SupabaseStorageService;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.EventBookingRepository;
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
    private final EventBookingRepository eventBookingRepository;

    public List<FieldResponse> getStadiumFields(UUID stadiumId) {

        String query = """
                SELECT f.id,
                       f.cost,
                       f.capacity,
                       f.stop_booking,
                       COALESCE(
                           json_agg(fi.image_path) FILTER (WHERE fi.id IS NOT NULL),
                           '[]'::json
                       ) AS image_paths
                FROM fields f
                LEFT JOIN public.field_images fi
                    ON f.id = fi.field_id
                WHERE f.stadium_id = ?
                GROUP BY f.id, f.cost, f.capacity
                ORDER BY capacity ,cost DESC;
                """;

        return jdbcTemplate.query(query, (rs, rowNum) -> {

            List<String> imageUrls = new ArrayList<>();

            String jsonImages = rs.getString("image_paths");

            if (jsonImages != null) {
                try {
                    List<String> paths = objectMapper.readValue(
                            jsonImages,
                            new TypeReference<List<String>>() {
                            }
                    );

                    imageUrls = paths.stream()
                            .map(supabaseStorageService::getPublicUrl)
                            .toList();

                } catch (Exception e) {
                    System.err.println("JSON Parsing error: " + e.getMessage());
                }
            }

            return new FieldResponse(
                    rs.getShort("id"),
                    rs.getShort("capacity"),
                    rs.getBigDecimal("cost"),
                    rs.getBoolean("stop_booking"),
                    imageUrls
            );

        }, stadiumId);
    }


    @Transactional
    public FieldResponse addField(UUID stadiumId, FieldRequest request, List<MultipartFile> imageFiles) {

        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var newField = Field.builder().cost(request.cost()).stopBooking(request.stopBooking())
                .capacity(request.capacity()).stadium(stadium).build();
        var savedField = fieldRepository.save(newField);

        List<String> fieldImages = new ArrayList<>();

        if (hasImages(imageFiles)) {
            fieldImages = addNewImages(stadiumId, savedField, imageFiles);
        }
        return new FieldResponse(savedField.getId(), savedField.getCapacity(), savedField.getCost(), savedField.getStopBooking(), fieldImages.stream().map(supabaseStorageService::getPublicUrl).toList());
    }

    @Transactional
    public FieldResponse updateField(UUID stadiumId, Short fieldId, FieldRequest request, List<MultipartFile> imageFiles) {

        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var field = stadium.getFields().stream().filter(f -> f.getId().equals(fieldId)).findFirst().orElseThrow(() -> new NotFoundException("Field not exists"));

        field.setCapacity(request.capacity());
        field.setCost(request.cost());
        field.setStopBooking(request.stopBooking());

        List<String> fieldImages = new ArrayList<>(field.getFieldImages().stream().map(FieldImage::getImagePath).toList());

        if (hasImages(imageFiles)) {
            fieldImages.addAll(addNewImages(stadiumId, field, imageFiles));
        }

        Field savedField = fieldRepository.save(field);
        return new FieldResponse(savedField.getId(), savedField.getCapacity(), savedField.getCost(), savedField.getStopBooking(), fieldImages.stream().map(supabaseStorageService::getPublicUrl).toList());
    }


    //
//
//    @Transactional
//    public void deleteField(Short fieldId) {
//
//        boolean isExists = eventBookingRepository.existsByField_IdAndEventEndGreaterThanEqual(fieldId, LocalDateTime.now());
//
//
//        System.out.println(isExists);
//
//        //

    /// /        var field = fieldRepository.findFieldWithUpcomingEvents(fieldId).orElseThrow(() -> new NotFoundException("Field not exists"));
    /// /
    /// /
    /// /        if (!field.getEventBookings().isEmpty()) {
    /// /            throw new ApiException("Garoonkan ma masaxi kartid waayo wuxu leyahy events aan weli la ciyaarin");
    /// /        }
    /// ///        if (!field.getFieldImages().isEmpty()) {
    /// ///            List<String> paths = field.getFieldImages().stream().map(FieldImage::getImagePath).toList();
    /// ///            supabaseStorageService.deleteFiles(paths);
    /// ///        }
    /// /
    /// /        System.out.println("Good");
    /// /        fieldRepository.delete(field);
//    }
    @Transactional
    public void deleteImage(String imagePath) {
        var actualPath = supabaseStorageService.extractPath(imagePath).trim();
        var image = fieldImageRepository.findFieldImageByImagePath(actualPath)
                .orElseThrow(() -> new NotFoundException("Image not exists"));

        supabaseStorageService.deleteFile(actualPath);

        fieldImageRepository.delete(image);
    }

    private List<String> addNewImages(UUID stadiumId, Field field, List<MultipartFile> imageFiles) {
        try {
            String folderPrefix = "stadiums/" + stadiumId + "/fields/" + field.getId();

            List<String> imagePaths = supabaseStorageService.uploadFiles(imageFiles, folderPrefix);

            List<FieldImage> images = imagePaths.stream().map(path -> FieldImage.builder().imagePath(path).field(field).build()).toList();

            var savedImages = fieldImageRepository.saveAll(images);

            return savedImages.stream().map(FieldImage::getImagePath).toList();

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    private boolean hasImages(List<MultipartFile> imageFiles) {
        return imageFiles != null && !imageFiles.isEmpty() && imageFiles.stream().anyMatch(file -> !file.isEmpty());
    }
}