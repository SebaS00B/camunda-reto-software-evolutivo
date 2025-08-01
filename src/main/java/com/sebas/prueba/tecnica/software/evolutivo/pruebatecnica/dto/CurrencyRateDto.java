package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para respuesta de monedas (Exchange Rate API)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRateDto {

    private String base;
    private String date;
    private Long timestamp;
    private Object rates; // ← Este será casteado a Map<String, Double>

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("error")
    private ErrorDto error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDto {
        private Integer code;
        private String type;
        private String info;
    }
    
}