package com.hammi.playground.modules.merchants;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends CrudRepository<StadiumMerchant, Short> {
}
