package com.hammi.playground.modules.stadium.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FieldRepository extends CrudRepository<Field, Short> {
//     List<Field> findFieldByStadiumId(UUID stadiumId);
}
