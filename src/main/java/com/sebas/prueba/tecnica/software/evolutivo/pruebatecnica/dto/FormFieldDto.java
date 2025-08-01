package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormFieldDto {
    
    private String id;
    private String label;
    private String type;
    private String defaultValue;
    private Boolean required;
    private List<ValidationDto> validations;
    private List<OptionDto> options; // Para selects
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationDto {
        private String type;
        private String value;
        private String message;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private String value;
        private String label;
    }
}