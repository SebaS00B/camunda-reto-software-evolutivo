package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * 🚀 CAMUNDA BPM CONSULTANT DEMO - SOFTWARE EVOLUTIVO
 * 
 * Aplicación completa que demuestra capacidades de BPM:
 * - ✅ Procesos BPMN con diferentes rutas de aprobación  
 * - ✅ Reglas de negocio DMN
 * - ✅ Formularios con validaciones y servicios REST
 * - ✅ Timers y recordatorios automáticos
 * - ✅ Notificaciones por email
 * - ✅ Dashboard personalizado y bandeja de tareas
 * - ✅ Reportes y auditoría completa
 * - ✅ Logs técnicos y monitoreo
 * 
 * @author Sebastian Burgos
 * @version 1.0.0
 */

@Slf4j
@SpringBootApplication
@EnableProcessApplication
@EnableAsync
@EnableScheduling
public class PruebatecnicaApplication  {

    public static void main(String[] args) {
        log.info("🚀 Iniciando Camunda BPM Consultant Demo...");
        log.info("📋 Características implementadas:");
        log.info("   ✅ Proceso Purchase Request con rutas de aprobación");
        log.info("   ✅ Reglas DMN para determinar rutas automáticamente");
        log.info("   ✅ Formularios con validaciones y servicios REST");
        log.info("   ✅ Timers para recordatorios automáticos");
        log.info("   ✅ Sistema de notificaciones por email");
        log.info("   ✅ Dashboard personalizado con métricas");
        log.info("   ✅ Bandeja de tareas para usuarios");
        log.info("   ✅ Reportes y auditoría completa");
        log.info("   ✅ Logs técnicos y monitoreo avanzado");
        log.info("   ✅ Look & Feel personalizado");
        
        SpringApplication.run(PruebatecnicaApplication.class, args);
        
        log.info("🎯 Aplicación iniciada exitosamente!");
        log.info("📊 Dashboard disponible en: http://localhost:8080");
        log.info("🔧 Camunda Cockpit en: http://localhost:8080/camunda");
        log.info("📝 Tasklist en: http://localhost:8080/camunda/app/tasklist");
        log.info("🗄️ H2 Console en: http://localhost:8080/h2-console");
        log.info("👤 Usuario admin: admin / admin");
    }

    /**
     * Bean para realizar llamadas REST a servicios externos
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}