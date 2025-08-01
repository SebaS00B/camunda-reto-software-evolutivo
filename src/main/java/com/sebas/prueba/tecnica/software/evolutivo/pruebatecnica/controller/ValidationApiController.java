package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.BusinessRulesService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ API REST para validaciones de reglas de negocio en tiempo real
 */
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@Slf4j
public class ValidationApiController {

    private final BusinessRulesService businessRulesService;
    private final PurchaseRequestService purchaseRequestService;

    /**
     * ✅ Validar una solicitud antes de enviarla (validación previa)
     */
    @PostMapping("/validate-request")
    public ResponseEntity<Map<String, Object>> validateRequest(@RequestBody ValidationRequest request) {
        log.info("🔍 Validando solicitud previa - Monto: {}, Categoría: {}", 
            request.getTotalAmount(), request.getCategory());
        
        try {
            // Crear objeto temporal para validación
            PurchaseRequest tempRequest = createTempRequest(request);
            
            // Ejecutar validaciones
            BusinessRulesService.ValidationResult validation = businessRulesService.validatePurchaseRequest(tempRequest);
            BusinessRulesService.ApprovalTimeEstimate timeEstimate = businessRulesService.estimateApprovalTime(
                validation.getApprovalRoute().getRoute(), 
                tempRequest.getPriority()
            );
            
            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("valid", validation.isValid());
            response.put("errors", validation.getErrors());
            response.put("warnings", validation.getWarnings());
            
            // Información de la ruta de aprobación
            Map<String, Object> approvalInfo = new HashMap<>();
            approvalInfo.put("route", validation.getApprovalRoute().getRoute().name());
            approvalInfo.put("routeDisplayName", validation.getApprovalRoute().getRoute().getDisplayName());
            approvalInfo.put("reason", validation.getApprovalRoute().getReason());
            approvalInfo.put("autoApproval", validation.getApprovalRoute().isAutoApprovalEligible());
            approvalInfo.put("estimatedDays", timeEstimate.getEstimatedDays());
            approvalInfo.put("maxDays", timeEstimate.getMaxDays());
            approvalInfo.put("approverEmail", businessRulesService.getApproverEmail(validation.getApprovalRoute().getRoute()));
            
            response.put("approvalInfo", approvalInfo);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ Validación completada - Ruta: {}, Válida: {}", 
                validation.getApprovalRoute().getRoute(), validation.isValid());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error durante validación: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("errors", java.util.List.of("Error interno durante validación: " + e.getMessage()));
            errorResponse.put("warnings", java.util.List.of());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * ✅ Obtener información de límites y reglas para el frontend
     */
    @GetMapping("/business-rules")
    public ResponseEntity<Map<String, Object>> getBusinessRules() {
        log.info("📋 Obteniendo reglas de negocio para frontend");
        
        Map<String, Object> rules = new HashMap<>();
        
        // Límites de montos
        Map<String, Object> amountLimits = new HashMap<>();
        amountLimits.put("autoApproval", BusinessRulesService.AUTO_APPROVAL_LIMIT);
        amountLimits.put("supervisor", BusinessRulesService.SUPERVISOR_LIMIT);
        amountLimits.put("manager", BusinessRulesService.MANAGER_LIMIT);
        amountLimits.put("officeSuppliesAuto", BusinessRulesService.OFFICE_SUPPLIES_AUTO_LIMIT);
        amountLimits.put("strategicCeo", BusinessRulesService.STRATEGIC_CEO_LIMIT);
        amountLimits.put("urgentEscalation", BusinessRulesService.URGENT_ESCALATION_LIMIT);
        
        // Rutas de aprobación
        Map<String, String> approvalRoutes = new HashMap<>();
        for (BusinessRulesService.ApprovalRoute route : BusinessRulesService.ApprovalRoute.values()) {
            approvalRoutes.put(route.name(), route.getDisplayName());
        }
        
        // Categorías y sus reglas especiales
        Map<String, Object> categoryRules = new HashMap<>();
        categoryRules.put("OFFICE_SUPPLIES", Map.of(
            "autoApprovalLimit", BusinessRulesService.OFFICE_SUPPLIES_AUTO_LIMIT,
            "description", "Suministros de oficina hasta $500 se aprueban automáticamente"
        ));
        categoryRules.put("STRATEGIC", Map.of(
            "ceoRequiredAbove", BusinessRulesService.STRATEGIC_CEO_LIMIT,
            "description", "Categoría estratégica > $5,000 requiere aprobación CEO"
        ));
        categoryRules.put("CONSULTING", Map.of(
            "ceoRequiredAbove", BusinessRulesService.STRATEGIC_CEO_LIMIT,
            "description", "Consultoría > $5,000 requiere aprobación CEO"
        ));
        
        rules.put("amountLimits", amountLimits);
        rules.put("approvalRoutes", approvalRoutes);
        rules.put("categoryRules", categoryRules);
        rules.put("lastUpdated", System.currentTimeMillis());
        
        return ResponseEntity.ok(rules);
    }

    /**
     * ✅ Simular el proceso DMN para una solicitud específica
     */
    @PostMapping("/simulate-dmn")
    public ResponseEntity<Map<String, Object>> simulateDmn(@RequestBody ValidationRequest request) {
        log.info("🎯 Simulando evaluación DMN - Monto: {}, Categoría: {}, Prioridad: {}", 
            request.getTotalAmount(), request.getCategory(), request.getPriority());
        
        try {
            PurchaseRequest tempRequest = createTempRequest(request);
            BusinessRulesService.ApprovalRouteResult routeResult = businessRulesService.determineApprovalRoute(tempRequest);
            
            Map<String, Object> simulation = new HashMap<>();
            simulation.put("inputVariables", Map.of(
                "Monto", request.getTotalAmount(),
                "Categoría", request.getCategory(),
                "priority", request.getPriority()
            ));
            
            simulation.put("dmnResult", Map.of(
                "approvalRoute", routeResult.getRoute().name(),
                "reason", routeResult.getReason(),
                "autoApprovalEligible", routeResult.isAutoApprovalEligible(),
                "urgentEscalation", routeResult.isUrgentEscalation(),
                "highValueRequest", routeResult.isHighValueRequest(),
                "strategicCategory", routeResult.isStrategicCategory()
            ));
            
            // Información adicional
            BusinessRulesService.ApprovalTimeEstimate timeEstimate = businessRulesService.estimateApprovalTime(
                routeResult.getRoute(), tempRequest.getPriority()
            );
            
            simulation.put("processingEstimate", Map.of(
                "estimatedDays", timeEstimate.getEstimatedDays(),
                "maxDays", timeEstimate.getMaxDays(),
                "approverEmail", businessRulesService.getApproverEmail(routeResult.getRoute())
            ));
            
            simulation.put("timestamp", System.currentTimeMillis());
            simulation.put("success", true);
            
            return ResponseEntity.ok(simulation);
            
        } catch (Exception e) {
            log.error("❌ Error simulando DMN: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Error simulando evaluación DMN: " + e.getMessage()
            ));
        }
    }

    /**
     * ✅ Validar una solicitud existente por ID
     */
    @GetMapping("/validate-existing/{id}")
    public ResponseEntity<Map<String, Object>> validateExisting(@PathVariable Long id) {
        log.info("🔍 Validando solicitud existente: {}", id);
        
        try {
            PurchaseRequest request = purchaseRequestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + id));
            
            BusinessRulesService.ValidationResult validation = businessRulesService.validatePurchaseRequest(request);
            boolean isOverdue = businessRulesService.isRequestOverdue(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", id);
            response.put("businessKey", request.getBusinessKey());
            response.put("valid", validation.isValid());
            response.put("errors", validation.getErrors());
            response.put("warnings", validation.getWarnings());
            response.put("isOverdue", isOverdue);
            response.put("currentStatus", request.getStatus().name());
            response.put("approvalRoute", validation.getApprovalRoute().getRoute().name());
            response.put("routeReason", validation.getApprovalRoute().getReason());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error validando solicitud existente {}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "error", "Solicitud no encontrada o error durante validación",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Crear objeto temporal para validaciones
     */
    private PurchaseRequest createTempRequest(ValidationRequest request) {
        PurchaseRequest tempRequest = new PurchaseRequest();
        tempRequest.setTotalAmount(request.getTotalAmount());
        tempRequest.setRequesterName(request.getRequesterName());
        tempRequest.setRequesterEmail(request.getRequesterEmail());
        tempRequest.setDescription(request.getDescription());
        tempRequest.setDepartment(request.getDepartment());
        tempRequest.setSupplierName(request.getSupplierName());
        tempRequest.setSupplierEmail(request.getSupplierEmail());
        
        // Convertir strings a enums
        try {
            tempRequest.setCategory(PurchaseRequest.PurchaseCategory.valueOf(request.getCategory()));
        } catch (Exception e) {
            tempRequest.setCategory(PurchaseRequest.PurchaseCategory.OTHER);
        }
        
        try {
            tempRequest.setPriority(PurchaseRequest.Priority.valueOf(request.getPriority()));
        } catch (Exception e) {
            tempRequest.setPriority(PurchaseRequest.Priority.NORMAL);
        }
        
        tempRequest.setCreatedAt(java.time.LocalDateTime.now());
        tempRequest.setDueDate(request.getDueDate());
        
        return tempRequest;
    }

    /**
     * ✅ DTO para validación de solicitudes
     */
    @lombok.Data
    public static class ValidationRequest {
        private BigDecimal totalAmount;
        private String requesterName;
        private String requesterEmail;
        private String description;
        private String department;
        private String category;
        private String priority;
        private String supplierName;
        private String supplierEmail;
        private java.time.LocalDateTime dueDate;
    }
}