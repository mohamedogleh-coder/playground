package com.hammi.playground.modules.global;

import com.hammi.playground.modules.merchants.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public  interface ProvidersRepository extends JpaRepository<Provider, Short> {
}
