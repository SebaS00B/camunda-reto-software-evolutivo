package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO para proveedores externos (JSON Placeholder API)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto {
    
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String website;
    private AddressDto address;
    private CompanyDto company;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDto {
        private String street;
        private String suite;
        private String city;
        private String zipcode;
        private GeoDto geo;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeoDto {
        private String lat;
        private String lng;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyDto {
        private String name;
        private String catchPhrase;
        private String bs;
    }
}