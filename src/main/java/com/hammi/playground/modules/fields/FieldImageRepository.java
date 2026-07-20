package com.hammi.playground.modules.fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldImageRepository extends JpaRepository<FieldImage, Short> {
}
