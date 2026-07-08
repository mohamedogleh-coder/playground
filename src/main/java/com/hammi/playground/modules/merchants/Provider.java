package com.hammi.playground.modules.merchants;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant_provider")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "provider_name", nullable = false, unique = true, length = 20)
    private String providerName;

    @Column(name = "provider_service", nullable = false, length = 50)
    private String providerService;

}