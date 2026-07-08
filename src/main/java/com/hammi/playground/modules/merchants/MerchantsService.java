package com.hammi.playground.modules.merchants;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantsService {
    private final StadiumRepository stadiumRepository;
    private final ProviderRepository providerRepository;
    private final MerchantRepository merchantRepository;

    @Transactional
    public List<MerchantResponse> addMerchants(UUID stadiumId, MerchantsListRequest request) {

        var stadium = stadiumRepository.findStadiumWithMerchants(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium not found"));

        return request.merchants().stream()
                .map(merchantRequest -> {

                    var provider = providerRepository.findById(merchantRequest.providerId())
                            .orElseThrow(() -> new NotFoundException("Provider not found"));

                    return stadium.getMerchants().stream()
                            .filter(merchant ->
                                    merchant.getMerchantNumber().equals(merchantRequest.merchantNumber())
                                            && merchant.getProvider().getId().equals(merchantRequest.providerId()))
                            .findFirst()
                            .map(merchant -> new MerchantResponse(
                                    merchant.getId(),
                                    merchant.getMerchantNumber(),
                                    merchant.getProvider().getProviderName(),
                                    merchant.getProvider().getProviderService()
                            ))
                            .orElseGet(() -> {

                                var stadiumMerchant = StadiumMerchant.builder()
                                        .merchantNumber(merchantRequest.merchantNumber())
                                        .provider(provider)
                                        .stadium(stadium)
                                        .build();

                                var savedMerchant = merchantRepository.save(stadiumMerchant);

                                return new MerchantResponse(
                                        savedMerchant.getId(),
                                        savedMerchant.getMerchantNumber(),
                                        savedMerchant.getProvider().getProviderName(),
                                        savedMerchant.getProvider().getProviderService()
                                );
                            });

                })
                .toList();
    }


    public List<MerchantResponse> getStadiumMerchants(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithMerchants(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium not found"));
        return stadium.getMerchants().stream().map(merchant -> new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantNumber(),
                merchant.getProvider().getProviderName(),
                merchant.getProvider().getProviderService()
        )).toList();
    }
}
