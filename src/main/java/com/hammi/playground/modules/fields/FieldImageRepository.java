package com.hammi.playground.modules.fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldImageRepository extends JpaRepository<FieldImage, Short> {

    Optional<FieldImage> findFieldImageByImagePath(String imagePath);
}
