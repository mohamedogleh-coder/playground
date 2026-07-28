package com.hammi.playground.modules.expanses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpansesRepository extends JpaRepository<Expense, Integer> {
}
