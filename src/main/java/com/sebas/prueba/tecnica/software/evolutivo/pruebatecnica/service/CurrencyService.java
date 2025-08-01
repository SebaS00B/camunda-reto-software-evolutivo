package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;


import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.CurrencyRateDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.CurrencyOptionDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    public CurrencyRateDto getRatesFromApi() {
        // MOCK temporal para pruebas (borra esto después si conectas al API real)
        Map<String, Double> mockRates = Map.of(
            "USD", 1.0,
            "EUR", 0.9,
            "GBP", 0.75,
            "JPY", 130.0
        );

        return CurrencyRateDto.builder()
            .base("USD")
            .date("2025-08-01")
            .timestamp(System.currentTimeMillis() / 1000)
            .rates(mockRates)
            .success(true)
            .build();
    }

    public List<CurrencyOptionDto> getCurrencyOptions() {
        CurrencyRateDto response = getRatesFromApi();

        // ✅ Protección contra null
        if (response == null || response.getRates() == null) {
            return List.of(); // evita error y regresa lista vacía
        }

        Map<String, Double> rates = (Map<String, Double>) response.getRates();

        return rates.keySet().stream()
            .map(code -> new CurrencyOptionDto(
                code,
                getCurrencyName(code),
                getCurrencySymbol(code)
            ))
            .collect(Collectors.toList());
    }

    private String getCurrencySymbol(String code) {
        return switch (code) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            default -> code;
        };
    }

    private String getCurrencyName(String code) {
        return switch (code) {
            case "USD" -> "Dólar estadounidense";
            case "EUR" -> "Euro";
            case "GBP" -> "Libra esterlina";
            case "JPY" -> "Yen japonés";
            default -> code;
        };
    }
}