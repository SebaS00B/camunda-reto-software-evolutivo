package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 🔧 CONFIGURACIÓN DE PROPIEDADES DE SERVICIOS
 * 
 * Maneja las URLs de servicios externos utilizados por la aplicación.
 * 
 * @author Sebastian Burgos
 * @version 1.0.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.services")
public class AppServiceProperties {
    private String suppliersUrl;
    private String currenciesUrl;
    private String categoriesUrl;  // ← Agregar esta línea

}