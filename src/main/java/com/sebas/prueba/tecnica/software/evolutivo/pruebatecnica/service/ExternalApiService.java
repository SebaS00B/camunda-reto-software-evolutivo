package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;

@Service
public class ExternalApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    
    @Value("${app.services.suppliers.url}")
    private String suppliersUrl;
    
    @Value("${app.services.categories.url}")
    private String categoriesUrl;
    
    private final RestTemplate restTemplate;

    
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Cacheable("suppliers")
    public List<SupplierDto> getSuppliers() {
        try {
            logger.info("Consultando proveedores desde: {}", suppliersUrl);
            
            // Usando JSONPlaceholder users como proveedores
            JsonNode[] users = restTemplate.getForObject(suppliersUrl, JsonNode[].class);
            
            return Arrays.stream(users)
                    .map(user -> new SupplierDto(
                        null, user.get("id").asText(),
                        user.get("name").asText(),
                        user.get("company").get("name").asText(),
                        user.get("email").asText(), categoriesUrl, null, null
                    ))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("Error consultando proveedores: {}", e.getMessage());
            return getDefaultSuppliers();
        }
    }
    
    private List<SupplierDto> getDefaultSuppliers() {
        return Arrays.asList(
            new SupplierDto(null, "1", "Acme Corp", "Acme Corporation", "sales@acme.com", categoriesUrl, null, null),
            new SupplierDto(null, "2", "Tech Solutions", "Tech Solutions Inc", "contact@techsol.com", categoriesUrl, null, null),
            new SupplierDto(null, "3", "Office World", "Office World Ltd", "orders@officeworld.com", categoriesUrl, null, null)
        );
    }
}
