package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.config.AppServiceProperties;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.CurrencyRateDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate;
    private final AppServiceProperties properties;

    public List<SupplierDto> getSuppliers() {
        String url = properties.getSuppliersUrl();
        log.info("🌐 Consultando proveedores externos desde: {}", url);
        try {
            ResponseEntity<List<SupplierDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
            );
            List<SupplierDto> suppliers = response.getBody();
            return (suppliers != null && !suppliers.isEmpty()) ? suppliers : getMockSuppliers();
        } catch (Exception e) {
            log.error("❌ Error consultando proveedores externos: {}", e.getMessage());
            return getMockSuppliers();
        }
    }

    public List<String> getCurrencies() {
        String url = properties.getCurrenciesUrl();
        log.info("💱 Consultando monedas disponibles desde: {}", url);
        try {
            ResponseEntity<CurrencyRateDto> response = restTemplate.getForEntity(
                url, CurrencyRateDto.class
            );
            CurrencyRateDto currencyData = response.getBody();
            return (currencyData != null && Boolean.TRUE.equals(currencyData.getSuccess()))
                    ? Arrays.asList("USD", "EUR", "COP", "ECU")
                    : getDefaultCurrencies();
        } catch (Exception e) {
            log.error("❌ Error consultando monedas: {}", e.getMessage());
            return getDefaultCurrencies();
        }
    }

    private List<SupplierDto> getMockSuppliers() {
        return Arrays.asList(
            SupplierDto.builder().id(1L).name("TechSolutions S.A.").email("ventas@techsolutions.com").phone("+593-2-123-4567").build(),
            SupplierDto.builder().id(2L).name("Oficina Total Ltda.").email("pedidos@oficinatotal.com").phone("+593-4-987-6543").build(),
            SupplierDto.builder().id(3L).name("Equipos Industriales CIA.").email("info@equiposindustriales.com").phone("+593-7-555-0123").build(),
            SupplierDto.builder().id(4L).name("Software & Consulting").email("contacto@softwareconsulting.com").phone("+593-2-444-5678").build()
        );
    }

    private List<String> getDefaultCurrencies() {
        return Arrays.asList("USD", "EUR", "COP", "ECU");
    }
}
