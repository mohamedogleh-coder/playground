package com.hammi.playground.modules.merchants;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends CrudRepository<Provider, Short> {
}
