package com.hammi.playground.modules.fields;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;
}