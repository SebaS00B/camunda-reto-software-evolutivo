package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Servicio especializado en validaciones de reglas de negocio
 * Implementa toda la lógica DMN en código Java para validaciones previas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessRulesService {
    
    // ✅ Constantes de límites de negocio
    public static final BigDecimal AUTO_APPROVAL_LIMIT = new BigDecimal("200");
    public static final BigDecimal SUPERVISOR_LIMIT = new BigDecimal("2000");
    public static final BigDecimal MANAGER_LIMIT = new BigDecimal("10000");
    public static final BigDecimal OFFICE_SUPPLIES_AUTO_LIMIT = new BigDecimal("500");
    public static final BigDecimal STRATEGIC_CEO_LIMIT = new BigDecimal("5000");
    public static final BigDecimal URGENT_ESCALATION_LIMIT = new BigDecimal("500");
    
    /**
     * ✅ Determinar la ruta de aprobación según las reglas DMN
     */
    public ApprovalRouteResult determineApprovalRoute(PurchaseRequest request) {
        log.debug("🎯 Determinando ruta de aprobación para: {} - Monto: {}", 
            request.getBusinessKey(), request.getTotalAmount());
        
        ApprovalRouteResult result = new ApprovalRouteResult();
        result.setAmount(request.getTotalAmount());
        result.setCategory(request.getCategory());
        result.setPriority(request.getPriority());
        
        // ✅ REGLA 1: Auto-aprobación para montos muy bajos
        if (request.getTotalAmount().compareTo(AUTO_APPROVAL_LIMIT) <= 0) {
            result.setRoute(ApprovalRoute.AUTO);
            result.setReason("Monto ≤ $200 - Aprobación automática");
            result.setAutoApprovalEligible(true);
            return result;
        }
        
        // ✅ REGLA 2: Auto-aprobación para suministros de oficina hasta $500
        if (request.getCategory() == PurchaseRequest.PurchaseCategory.OFFICE_SUPPLIES 
            && request.getTotalAmount().compareTo(OFFICE_SUPPLIES_AUTO_LIMIT) <= 0) {
            result.setRoute(ApprovalRoute.AUTO);
            result.setReason("Suministros de oficina ≤ $500 - Aprobación automática");
            result.setAutoApprovalEligible(true);
            return result;
        }
        
        // ✅ REGLA 3: Escalamiento inmediato para solicitudes urgentes con monto alto
        if ((request.getPriority() == PurchaseRequest.Priority.HIGH 
             || request.getPriority() == PurchaseRequest.Priority.URGENT)
            && request.getTotalAmount().compareTo(URGENT_ESCALATION_LIMIT) > 0) {
            
            if (request.getTotalAmount().compareTo(MANAGER_LIMIT) > 0) {
                result.setRoute(ApprovalRoute.CEO);
                result.setReason("Solicitud URGENTE con monto alto > $500 - Escalamiento a CEO");
            } else {
                result.setRoute(ApprovalRoute.MANAGER);
                result.setReason("Solicitud URGENTE con monto > $500 - Escalamiento a Manager");
            }
            result.setUrgentEscalation(true);
            return result;
        }
        
        // ✅ REGLA 4: CEO para categorías estratégicas
        if ((request.getCategory() == PurchaseRequest.PurchaseCategory.STRATEGIC 
             || request.getCategory() == PurchaseRequest.PurchaseCategory.CONSULTING)
            && request.getTotalAmount().compareTo(STRATEGIC_CEO_LIMIT) > 0) {
            result.setRoute(ApprovalRoute.CEO);
            result.setReason("Categoría estratégica/consultoría > $5,000 - Requiere aprobación CEO");
            result.setStrategicCategory(true);
            return result;
        }
        
        // ✅ REGLA 5: CEO para montos muy altos
        if (request.getTotalAmount().compareTo(MANAGER_LIMIT) > 0) {
            result.setRoute(ApprovalRoute.CEO);
            result.setReason("Monto > $10,000 - Requiere aprobación CEO");
            result.setHighValueRequest(true);
            return result;
        }
        
        // ✅ REGLA 6: Manager para montos medios-altos y equipamiento
        if (request.getTotalAmount().compareTo(SUPERVISOR_LIMIT) > 0 
            && request.getTotalAmount().compareTo(MANAGER_LIMIT) <= 0) {
            
            if (request.getCategory() == PurchaseRequest.PurchaseCategory.EQUIPMENT 
                || request.getCategory() == PurchaseRequest.PurchaseCategory.IT_HARDWARE) {
                result.setRoute(ApprovalRoute.MANAGER);
                result.setReason("Equipamiento/Hardware IT entre $2,001-$10,000 - Aprobación Manager");
                return result;
            }
        }
        
        // ✅ REGLA 7: Supervisor para montos medios
        if (request.getTotalAmount().compareTo(AUTO_APPROVAL_LIMIT) > 0 
            && request.getTotalAmount().compareTo(SUPERVISOR_LIMIT) <= 0) {
            result.setRoute(ApprovalRoute.SUPERVISOR);
            result.setReason("Monto entre $201-$2,000 - Aprobación Supervisor");
            return result;
        }
        
        // ✅ REGLA DEFAULT: Supervisor como fallback
        result.setRoute(ApprovalRoute.SUPERVISOR);
        result.setReason("Regla por defecto - Aprobación Supervisor");
        
        log.debug("✅ Ruta determinada: {} - {}", result.getRoute(), result.getReason());
        return result;
    }
    
    /**
     * ✅ Validar si una solicitud cumple con las reglas de negocio
     */
    public ValidationResult validatePurchaseRequest(PurchaseRequest request) {
        ValidationResult validation = new ValidationResult();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validaciones básicas
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("El monto debe ser mayor a $0");
        }
        
        if (request.getTotalAmount() != null && request.getTotalAmount().compareTo(new BigDecimal("1000000")) > 0) {
            errors.add("El monto excede el límite máximo de $1,000,000");
        }
        
        // Validaciones de coherencia
        if (request.getPriority() == PurchaseRequest.Priority.URGENT 
            && request.getTotalAmount() != null 
            && request.getTotalAmount().compareTo(new BigDecimal("100000")) > 0) {
            warnings.add("Solicitud URGENTE por monto muy alto ($100K+) requiere justificación especial");
        }
        
        // Validaciones por categoría
        if (request.getCategory() == PurchaseRequest.PurchaseCategory.OFFICE_SUPPLIES 
            && request.getTotalAmount() != null 
            && request.getTotalAmount().compareTo(new BigDecimal("5000")) > 0) {
            warnings.add("Suministros de oficina por monto alto - Verificar necesidad real");
        }
        
        // Validaciones de proveedor
        if (request.getSupplierName() == null || request.getSupplierName().trim().isEmpty()) {
            errors.add("Debe especificar un proveedor");
        }
        
        // Validaciones temporales
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDateTime.now().plusDays(1))) {
            warnings.add("Fecha límite muy próxima - Puede afectar el proceso de aprobación");
        }
        
        validation.setValid(errors.isEmpty());
        validation.setErrors(errors);
        validation.setWarnings(warnings);
        validation.setApprovalRoute(determineApprovalRoute(request));
        
        return validation;
    }
    
    /**
     * ✅ Calcular el tiempo estimado de aprobación
     */
    public ApprovalTimeEstimate estimateApprovalTime(ApprovalRoute route, PurchaseRequest.Priority priority) {
        ApprovalTimeEstimate estimate = new ApprovalTimeEstimate();
        
        int baseDays = switch (route) {
            case AUTO -> 0;
            case SUPERVISOR -> 2;
            case MANAGER -> 3;
            case CEO -> 5;
        };
        
        // Ajustar por prioridad
        if (priority == PurchaseRequest.Priority.URGENT) {
            baseDays = Math.max(1, baseDays - 1);
        } else if (priority == PurchaseRequest.Priority.LOW) {
            baseDays += 1;
        }
        
        estimate.setEstimatedDays(baseDays);
        estimate.setMaxDays(baseDays + 2); // Buffer para retrasos
        estimate.setRoute(route);
        
        return estimate;
    }
    
    /**
     * ✅ Verificar si una solicitud está vencida según reglas de negocio
     */
    public boolean isRequestOverdue(PurchaseRequest request) {
        if (request.getDueDate() == null) {
            // Si no hay fecha límite, calcular basado en fecha de creación + tiempo estimado
            ApprovalRoute route = determineApprovalRoute(request).getRoute();
            ApprovalTimeEstimate estimate = estimateApprovalTime(route, request.getPriority());
            LocalDateTime expectedCompletion = request.getCreatedAt().plusDays(estimate.getMaxDays());
            return LocalDateTime.now().isAfter(expectedCompletion);
        }
        
        return LocalDateTime.now().isAfter(request.getDueDate()) 
               && request.getStatus() == PurchaseRequest.RequestStatus.PENDING;
    }
    
    /**
     * ✅ Obtener el email del aprobador según la ruta
     */
    public String getApproverEmail(ApprovalRoute route) {
        return switch (route) {
            case AUTO -> null; // No requiere aprobador humano
            case SUPERVISOR -> "supervisor@softwareevolutivo.com";
            case MANAGER -> "manager@softwareevolutivo.com";
            case CEO -> "ceo@softwareevolutivo.com";
        };
    }
    
    // ✅ Enums y clases de resultado
    public enum ApprovalRoute {
        AUTO("Automática"),
        SUPERVISOR("Supervisor"),
        MANAGER("Gerente"),
        CEO("CEO");
        
        private final String displayName;
        
        ApprovalRoute(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @lombok.Data
    public static class ApprovalRouteResult {
        private ApprovalRoute route;
        private String reason;
        private BigDecimal amount;
        private PurchaseRequest.PurchaseCategory category;
        private PurchaseRequest.Priority priority;
        private boolean autoApprovalEligible;
        private boolean urgentEscalation;
        private boolean highValueRequest;
        private boolean strategicCategory;
    }
    
    @lombok.Data
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private ApprovalRouteResult approvalRoute;
    }
    
    @lombok.Data
    public static class ApprovalTimeEstimate {
        private int estimatedDays;
        private int maxDays;
        private ApprovalRoute route;
    }
}