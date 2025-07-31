package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 🚀 APLICACIÓN PRINCIPAL - RETO TÉCNICO BPM CAMUNDA
 * 
 * Software Evolutivo - Demostración de capacidades técnicas
 * 
 * Esta aplicación implementa un sistema completo de gestión de procesos
 * de negocio utilizando Camunda BPM integrado con Spring Boot.
 * 
 * FUNCIONALIDADES IMPLEMENTADAS:
 * ✅ Modelado y ejecución de procesos BPMN
 * ✅ Formularios web con validaciones avanzadas
 * ✅ Integración con servicios REST externos
 * ✅ Sistema de notificaciones por email
 * ✅ Temporizadores y tareas programadas
 * ✅ Bandeja de tareas para usuarios
 * ✅ Auditoría completa de procesos
 * ✅ Reportes y métricas de ejecución
 * ✅ Interfaz personalizada responsive
 * 
 * @author Sebastian (Candidato Software Evolutivo)
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableAsync        // Operaciones asíncronas (emails, notificaciones)
@EnableScheduling   // Tareas programadas (temporizadores, cleanup)
public class PruebatecnicaApplication {

    public static void main(String[] args) {
        // Banner personalizado antes del inicio
        printWelcomeBanner();
        
        // Iniciar la aplicación Spring Boot
        SpringApplication.run(PruebatecnicaApplication.class, args);
        
        // Información post-inicio
        printApplicationInfo();
    }
    
    /**
     * Banner de bienvenida con información del reto
     */
    private static void printWelcomeBanner() {
        System.out.println("\n" +
            "██████╗ ███╗   ███╗██████╗      ██████╗██╗  ██╗ █████╗ ██╗     ██╗     ███████╗███╗   ██╗ ██████╗ ███████╗\n" +
            "██╔══██╗████╗ ████║██╔══██╗    ██╔════╝██║  ██║██╔══██╗██║     ██║     ██╔════╝████╗  ██║██╔════╝ ██╔════╝\n" +
            "██████╔╝██╔████╔██║██████╔╝    ██║     ███████║███████║██║     ██║     █████╗  ██╔██╗ ██║██║  ███╗█████╗  \n" +
            "██╔══██╗██║╚██╔╝██║██╔═══╝     ██║     ██╔══██║██╔══██║██║     ██║     ██╔══╝  ██║╚██╗██║██║   ██║██╔══╝  \n" +
            "██████╔╝██║ ╚═╝ ██║██║         ╚██████╗██║  ██║██║  ██║███████╗███████╗███████╗██║ ╚████║╚██████╔╝███████╗\n" +
            "╚═════╝ ╚═╝     ╚═╝╚═╝          ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝\n");
        
        System.out.println("🏢 SOFTWARE EVOLUTIVO - RETO TÉCNICO BPM");
        System.out.println("👨‍💻 Desarrollado por: Sebastian");
        System.out.println("🎯 Objetivo: Demostrar expertise en Camunda BPM + Spring Boot");
        System.out.println("⚡ Iniciando aplicación...\n");
    }
    
    /**
     * Información de acceso post-inicio
     */
    private static void printApplicationInfo() {
        System.out.println("\n" +
            "════════════════════════════════════════════════════════════════════════════════════════\n" +
            "🎉 ¡APLICACIÓN INICIADA CORRECTAMENTE!\n" +
            "════════════════════════════════════════════════════════════════════════════════════════\n" +
            "\n" +
            "🌐 INTERFACES WEB DISPONIBLES:\n" +
            "   • Aplicación Principal:    http://localhost:8080\n" +
            "   • Camunda Cockpit:         http://localhost:8080/app/cockpit\n" +
            "   • Camunda Tasklist:        http://localhost:8080/app/tasklist\n" +
            "   • Camunda Admin:           http://localhost:8080/app/admin\n" +
            "   • H2 Database Console:     http://localhost:8080/h2-console\n" +
            "\n" +
            "🔐 CREDENCIALES DE ACCESO:\n" +
            "   • Usuario Camunda:         admin / admin\n" +
            "   • Base de datos H2:        sa / (sin contraseña)\n" +
            "   • JDBC URL:                jdbc:h2:mem:camunda-db\n" +
            "\n" +
            "📚 API ENDPOINTS:\n" +
            "   • REST API:                http://localhost:8080/api\n" +
            "   • Health Check:            http://localhost:8080/actuator/health\n" +
            "   • Métricas:                http://localhost:8080/actuator/metrics\n" +
            "\n" +
            "🚀 FUNCIONALIDADES IMPLEMENTADAS:\n" +
            "   ✅ Procesos BPMN automatizados\n" +
            "   ✅ Formularios con validaciones\n" +
            "   ✅ Servicios REST integrados\n" +
            "   ✅ Notificaciones por email\n" +
            "   ✅ Temporizadores y workflows\n" +
            "   ✅ Bandeja de tareas\n" +
            "   ✅ Auditoría y reportes\n" +
            "   ✅ Interfaz personalizada\n" +
            "\n" +
            "════════════════════════════════════════════════════════════════════════════════════════\n" +
            "💡 TIP: Empieza visitando http://localhost:8080 para ver la demo completa\n" +
            "════════════════════════════════════════════════════════════════════════════════════════\n");
    }
}
