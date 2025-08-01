package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ReportService;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ReportService reportService;
    private final PurchaseRequestService purchaseRequestService;
    private final TaskService taskService;
    
    @GetMapping({"/dashboard", "/dashboard/", "/process/dashboard"})
    public String dashboard(Model model) {
        log.info("🎯 Mostrando dashboard principal");
        
        try {
            // ✅ Obtener métricas reales del dashboard
            var metrics = reportService.getDashboardMetrics();
            model.addAttribute("metrics", metrics);
            
            // ✅ Obtener datos específicos para el dashboard
            Long totalRequests = purchaseRequestService.getTotalRequests();
            Long pendingRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.PENDING);
            Long approvedRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.APPROVED);
            Long rejectedRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.REJECTED);
            
            // ✅ Obtener solicitudes recientes con más información
            List<PurchaseRequest> recentRequests = purchaseRequestService.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();
            
            // ✅ Calcular estadísticas adicionales
            List<PurchaseRequest> overdueRequests = purchaseRequestService.findOverdueRequests();
            
            // ✅ Obtener tareas pendientes del usuario actual
            List<org.camunda.bpm.engine.task.Task> userTasks = taskService.createTaskQuery()
                .processDefinitionKey("purchase-request-process")
                .active()
                .list();
            
            // ✅ Análisis de aprobaciones automáticas vs manuales
            long autoApprovedCount = recentRequests.stream()
                .filter(r -> r.getApprovedBy() != null && r.getApprovedBy().contains("SYSTEM_AUTO"))
                .count();
            
            long manualApprovedCount = approvedRequests - autoApprovedCount;
            
            // ✅ Calcular porcentaje de aprobación
            double approvalRate = totalRequests > 0 ? 
                (double) approvedRequests / totalRequests * 100 : 0;
            
            // ✅ Encontrar solicitudes que requieren escalamiento
            List<PurchaseRequest> needsEscalation = recentRequests.stream()
                .filter(r -> r.getTotalAmount().compareTo(new BigDecimal("10000")) > 0)
                .filter(r -> r.getStatus() == PurchaseRequest.RequestStatus.PENDING)
                .toList();
            
            // ✅ Agregar todos los datos necesarios al modelo
            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("approvedRequests", approvedRequests);
            model.addAttribute("rejectedRequests", rejectedRequests);
            model.addAttribute("recentRequests", recentRequests);
            model.addAttribute("overdueRequests", overdueRequests);
            model.addAttribute("overdueCount", overdueRequests.size());
            model.addAttribute("userTasks", userTasks);
            model.addAttribute("userTasksCount", userTasks.size());
            model.addAttribute("autoApprovedCount", autoApprovedCount);
            model.addAttribute("manualApprovedCount", manualApprovedCount);
            model.addAttribute("approvalRate", String.format("%.1f", approvalRate));
            model.addAttribute("needsEscalation", needsEscalation);
            model.addAttribute("escalationCount", needsEscalation.size());
            
            // ✅ Datos para gráficos
            Map<String, Object> chartData = prepareChartData(recentRequests);
            model.addAttribute("chartData", chartData);
            
            log.info("📊 Dashboard cargado - Total: {}, Pendientes: {}, Aprobadas: {}", 
                totalRequests, pendingRequests, approvedRequests);
            
            return "process/dashboard";
            
        } catch (Exception e) {
            log.error("❌ Error cargando dashboard: {}", e.getMessage(), e);
            model.addAttribute("message", "Error cargando datos del dashboard: " + e.getMessage());
            model.addAttribute("messageType", "error");
            
            // ✅ Datos por defecto en caso de error
            model.addAttribute("totalRequests", 0L);
            model.addAttribute("pendingRequests", 0L);
            model.addAttribute("approvedRequests", 0L);
            model.addAttribute("rejectedRequests", 0L);
            model.addAttribute("recentRequests", Collections.emptyList());
            model.addAttribute("overdueCount", 0);
            model.addAttribute("userTasksCount", 0);
            model.addAttribute("approvalRate", "0.0");
            model.addAttribute("escalationCount", 0);
            
            return "process/dashboard";
        }
    }
    
    /**
     * ✅ Preparar datos para los gráficos del dashboard
     */
    private Map<String, Object> prepareChartData(List<PurchaseRequest> requests) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Datos por categoría
        Map<PurchaseRequest.PurchaseCategory, Long> categoryData = requests.stream()
            .collect(Collectors.groupingBy(
                PurchaseRequest::getCategory,
                Collectors.counting()
            ));
        
        // Datos por departamento  
        Map<String, Long> departmentData = requests.stream()
            .collect(Collectors.groupingBy(
                PurchaseRequest::getDepartment,
                Collectors.counting()
            ));
            
        // Datos por estado
        Map<PurchaseRequest.RequestStatus, Long> statusData = requests.stream()
            .collect(Collectors.groupingBy(
                PurchaseRequest::getStatus,
                Collectors.counting()
            ));
            
        // Datos de montos por rango
        Map<String, Long> amountRanges = new HashMap<>();
        amountRanges.put("$0 - $500", requests.stream()
            .filter(r -> r.getTotalAmount().compareTo(new BigDecimal("500")) <= 0)
            .count());
        amountRanges.put("$501 - $2000", requests.stream()
            .filter(r -> r.getTotalAmount().compareTo(new BigDecimal("500")) > 0 
                && r.getTotalAmount().compareTo(new BigDecimal("2000")) <= 0)
            .count());
        amountRanges.put("$2001 - $10000", requests.stream()
            .filter(r -> r.getTotalAmount().compareTo(new BigDecimal("2000")) > 0 
                && r.getTotalAmount().compareTo(new BigDecimal("10000")) <= 0)
            .count());
        amountRanges.put("$10000+", requests.stream()
            .filter(r -> r.getTotalAmount().compareTo(new BigDecimal("10000")) > 0)
            .count());
        
        chartData.put("categories", categoryData);
        chartData.put("departments", departmentData);
        chartData.put("statuses", statusData);
        chartData.put("amountRanges", amountRanges);
        
        return chartData;
    }

    /**
     * 📊 API endpoint para obtener datos del dashboard en JSON
     */
    @GetMapping("/api/dashboard/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            data.put("totalRequests", purchaseRequestService.getTotalRequests());
            data.put("pendingRequests", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.PENDING));
            data.put("approvedRequests", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.APPROVED));
            data.put("rejectedRequests", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.REJECTED));
            data.put("overdueCount", purchaseRequestService.findOverdueRequests().size());
            
            // Tareas pendientes
            long pendingTasks = taskService.createTaskQuery()
                .processDefinitionKey("purchase-request-process")
                .active()
                .count();
            data.put("pendingTasks", pendingTasks);
            
            data.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo datos del dashboard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error obteniendo datos: " + e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        log.info("📊 Mostrando página de reportes");
        
        try {
            var auditReport = reportService.getAuditReport();
            var metrics = reportService.getDashboardMetrics();
            
            model.addAttribute("auditReport", auditReport);
            model.addAttribute("metrics", metrics);
            
            return "reports/dashboard";
            
        } catch (Exception e) {
            log.error("❌ Error cargando reportes: {}", e.getMessage(), e);
            model.addAttribute("message", "Error cargando reportes");
            model.addAttribute("messageType", "error");
            return "reports/dashboard";
        }
    }
}