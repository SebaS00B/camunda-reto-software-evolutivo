package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.CurrencyRateDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.suppliers.url}")
    private String suppliersUrl;

    @Value("${app.services.currencies.url}")
    private String currenciesUrl;

    public List<SupplierDto> getSuppliers() {
        try {
            log.info("🌐 Consultando proveedores externos desde: {}", suppliersUrl);
            
            ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
                suppliersUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SupplierDto>>() {}
            );

            List<SupplierDto> suppliers = response.getBody();
            if (suppliers != null && !suppliers.isEmpty()) {
                log.info("✅ Obtenidos {} proveedores externos", suppliers.size());
                return suppliers;
            } else {
                log.warn("⚠️ No se obtuvieron proveedores, usando datos de prueba");
                return getMockSuppliers();
            }

        } catch (Exception e) {
            log.error("❌ Error consultando proveedores externos: {}", e.getMessage());
            log.info("🔄 Usando proveedores de prueba como fallback");
            return getMockSuppliers();
        }
    }

    public List<String> getCurrencies() {
        try {
            log.info("💱 Consultando monedas disponibles desde: {}", currenciesUrl);
            
            ResponseEntity<CurrencyRateDto> response = restTemplate.getForEntity(
                currenciesUrl, CurrencyRateDto.class
            );

            CurrencyRateDto currencyData = response.getBody();
            if (currencyData != null && currencyData.getSuccess() != null && currencyData.getSuccess()) {
                // Retornar monedas principales
                return Arrays.asList("USD", "EUR", "COP", "ECU");
            } else {
                return getDefaultCurrencies();
            }

        } catch (Exception e) {
            log.error("❌ Error consultando monedas: {}", e.getMessage());
            return getDefaultCurrencies();
        }
    }

    private List<SupplierDto> getMockSuppliers() {
        return Arrays.asList(
            SupplierDto.builder()
                .id(1L)
                .name("TechSolutions S.A.")
                .email("ventas@techsolutions.com")
                .phone("+593-2-123-4567")
                .build(),
            SupplierDto.builder()
                .id(2L)
                .name("Oficina Total Ltda.")
                .email("pedidos@oficinatotal.com")
                .phone("+593-4-987-6543")
                .build(),
            SupplierDto.builder()
                .id(3L)
                .name("Equipos Industriales CIA.")
                .email("info@equiposindustriales.com")
                .phone("+593-7-555-0123")
                .build(),
            SupplierDto.builder()
                .id(4L)
                .name("Software & Consulting")
                .email("contacto@softwareconsulting.com")
                .phone("+593-2-444-5678")
                .build()
        );
    }

    private List<String> getDefaultCurrencies() {
        return Arrays.asList("USD", "EUR", "COP", "ECU");
    }
}
