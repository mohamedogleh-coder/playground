package com.hammi.playground.modules.global;

import com.hammi.playground.modules.merchants.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppService {
    private final ProvidersRepository providersRepository;

    public List<Provider> getMerchantProviders() {
        return providersRepository.findAll();
    }
}
