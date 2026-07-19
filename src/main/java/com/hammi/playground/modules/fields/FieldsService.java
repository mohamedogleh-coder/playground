//@Service
//@RequiredArgsConstructor
//public class FieldsService {
//
//    private final FieldRepository fieldRepository;
//    private final StadiumRepository stadiumRepository;
//    private final FieldImageRepository fieldImageRepository;
//    private final SupabaseStorageService supabaseStorageService;
//
//
//    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
//
//        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//
//        return stadium.getFields()
//                .stream()
//                .map(field -> {
//
//                    List<String> images = field.getImages()
//                            .stream()
//                            .map(FieldImage::getImagePath)
//                            .toList();
//
//                    return new FieldResponse(
//                            field.getId(),
//                            field.getCapacity(),
//                            field.getCost(),
//                            images
//                    );
//
//                })
//                .toList();
//    }
//
//
//    public FieldResponse addField(UUID stadiumId, FieldRequest request) {
//
//        var stadium = stadiumRepository.findStadiumWithFields(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//
//        Field field = Field.builder()
//                .capacity(request.capacity())
//                .cost(request.cost())
//                .stadium(stadium)
//                .build();
//
//
//        Field savedField = fieldRepository.save(field);
//
//
//        List<FieldImage> images = request.images()
//                .stream()
//                .map(file -> {
//
//                    try {
//
//                        String path = supabaseStorageService.uploadFile(
//                                file,
//                                "fields"
//                        );
//
//
//                        return FieldImage.builder()
//                                .imagePath(path)
//                                .field(savedField)
//                                .build();
//
//
//                    } catch (Exception e) {
//                        throw new RuntimeException("Image upload failed", e);
//                    }
//
//                })
//                .toList();
//
//
//        fieldImageRepository.saveAll(images);
//
//
//        return new FieldResponse(
//                savedField.getId(),
//                savedField.getCapacity(),
//                savedField.getCost(),
//                images.stream()
//                        .map(FieldImage::getImagePath)
//                        .toList()
//        );
//    }
//
//}

package com.hammi.playground.modules.fields;

import com.hammi.playground.config.SupabaseStorageService;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.*;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
//        List<String> imagePaths = stadium.getFields().
        return stadium.getFields().stream().map((field -> new FieldResponse(field.getId(), field.getCapacity(), field.getCost(), List.of()
        ))).toList();
    }


    public FieldResponse addField(UUID stadiumId, FieldRequest request) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var newField = Field.builder().cost(request.cost()).capacity(request.capacity()).stadium(stadium).build();
        var savedField = fieldRepository.save(newField);

        List<String> imagePaths = new ArrayList<>();


//        supabaseStorageService.uploadFile();

        return new FieldResponse(
                savedField.getId(), savedField.getCapacity(), savedField.getCost(),
                List.of()
        );
    }

}

