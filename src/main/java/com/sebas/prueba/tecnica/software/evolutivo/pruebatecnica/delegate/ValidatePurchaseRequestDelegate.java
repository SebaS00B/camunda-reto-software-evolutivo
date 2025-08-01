package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import java.math.BigDecimal;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;

import io.micrometer.common.util.StringUtils;

@Component("validatePurchaseRequestDelegate")
public class ValidatePurchaseRequestDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidatePurchaseRequestDelegate.class);
    
    // ✅ Configurar límites de negocio
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000"); // $1M máximo
    private static final BigDecimal AUTO_APPROVAL_LIMIT = new BigDecimal("200");
    private static final BigDecimal URGENT_ESCALATION_LIMIT = new BigDecimal("50000");
    
    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("🧐 Antes de invocar la tabla DMN recibo: {}", execution.getVariables());

        logger.info("🔍 Validando solicitud de compra - Process Instance: {}", execution.getProcessInstanceId());
        
        try {
            // ✅ 1. Obtener y validar datos básicos
            ValidationContext context = extractAndValidateBasicData(execution);
            
            // ✅ 2. Validaciones de negocio específicas
            validateBusinessRules(context, execution);
            
            // ✅ 3. Validaciones de montos y límites
            validateAmountLimits(context, execution);
            
            // ✅ 4. Validar proveedor y datos externos
            validateSupplierData(context, execution);
            
            // ✅ 5. Establecer variables calculadas para el DMN
            setDmnVariables(context, execution);
            
            // ✅ 6. Actualizar estado en BD si existe
            updateRequestInDatabase(context, execution);
            
            execution.setVariable("validationStatus", "APPROVED");
            execution.setVariable("validationComments", "Solicitud validada exitosamente");
            
            logger.info("✅ Solicitud validada exitosamente. Monto: {}, Categoría: {}, Ruta sugerida: {}", 
                context.amount, context.category, suggestApprovalRoute(context));
                
        } catch (BpmnError e) {
            logger.error("❌ Error de validación BPMN: {}", e.getMessage());
            throw e; // Re-lanzar errores BPMN
        } catch (Exception e) {
            logger.error("❌ Error inesperado durante validación: {}", e.getMessage(), e);
            throw new BpmnError("VALIDATION_ERROR", "Error inesperado durante validación: " + e.getMessage());
        }
    }
    
    /**
     * ✅ Extraer y validar datos básicos del proceso
     */
    private ValidationContext extractAndValidateBasicData(DelegateExecution execution) throws BpmnError {
        ValidationContext context = new ValidationContext();
        
        // Extraer variables principales
        context.businessKey = execution.getProcessBusinessKey();
        context.requesterName = (String) execution.getVariable("requesterName");
        context.requesterEmail = (String) execution.getVariable("requesterEmail");
        context.description = (String) execution.getVariable("description");
        context.department = (String) execution.getVariable("department");
        context.category = (String) execution.getVariable("category");
        context.priority = (String) execution.getVariable("priority");
        context.supplierName = (String) execution.getVariable("supplierName");
        context.supplierEmail = (String) execution.getVariable("supplierEmail");
        
        // ✅ Manejo robusto del monto (puede venir como Double o BigDecimal)
        Object amountVar = execution.getVariable("totalAmount");
        if (amountVar instanceof BigDecimal) {
            context.amount = (BigDecimal) amountVar;
        } else if (amountVar instanceof Double) {
            context.amount = BigDecimal.valueOf((Double) amountVar);
        } else if (amountVar instanceof String) {
            try {
                context.amount = new BigDecimal((String) amountVar);
            } catch (NumberFormatException e) {
                throw new BpmnError("VALIDATION_ERROR", "Formato de monto inválido: " + amountVar);
            }
        } else {
            throw new BpmnError("VALIDATION_ERROR", "Monto no proporcionado o en formato incorrecto");
        }
        
        // Validaciones básicas de nulos y vacíos
        if (StringUtils.isBlank(context.requesterName)) {
            throw new BpmnError("VALIDATION_ERROR", "El nombre del solicitante es requerido");
        }
        
        if (StringUtils.isBlank(context.requesterEmail) || !isValidEmail(context.requesterEmail)) {
            throw new BpmnError("VALIDATION_ERROR", "Email del solicitante inválido");
        }
        
        if (StringUtils.isBlank(context.description) || context.description.length() < 10) {
            throw new BpmnError("VALIDATION_ERROR", "La descripción debe tener al menos 10 caracteres");
        }
        
        if (StringUtils.isBlank(context.department)) {
            throw new BpmnError("VALIDATION_ERROR", "El departamento es requerido");
        }
        
        if (StringUtils.isBlank(context.category)) {
            throw new BpmnError("VALIDATION_ERROR", "La categoría es requerida");
        }
        
        return context;
    }
    
    /**
     * ✅ Validaciones específicas de reglas de negocio
     */
    private void validateBusinessRules(ValidationContext context, DelegateExecution execution) throws BpmnError {
        
        // Validar categorías permitidas
        try {
            PurchaseRequest.PurchaseCategory.valueOf(context.category);
        } catch (IllegalArgumentException e) {
            throw new BpmnError("VALIDATION_ERROR", "Categoría inválida: " + context.category);
        }
        
        // Validar prioridades
        if (context.priority != null) {
            try {
                PurchaseRequest.Priority.valueOf(context.priority);
            } catch (IllegalArgumentException e) {
                throw new BpmnError("VALIDATION_ERROR", "Prioridad inválida: " + context.priority);
            }
        }
        
        // ✅ Reglas específicas por categoría
        if ("STRATEGIC".equals(context.category) || "CONSULTING".equals(context.category)) {
            if (context.amount.compareTo(new BigDecimal("1000")) < 0) {
                logger.warn("⚠️ Solicitud estratégica por monto bajo: {}", context.amount);
            }
        }
        
        // ✅ Validar coherencia de datos
        if ("URGENT".equals(context.priority) && context.amount.compareTo(URGENT_ESCALATION_LIMIT) > 0) {
            logger.warn("⚠️ Solicitud URGENTE por monto alto requiere atención especial: {}", context.amount);
            execution.setVariable("requiresSpecialAttention", true);
        }
    }
    
    /**
     * ✅ Validar límites de montos
     */
    private void validateAmountLimits(ValidationContext context, DelegateExecution execution) throws BpmnError {
        
        if (context.amount.compareTo(MIN_AMOUNT) < 0) {
            throw new BpmnError("VALIDATION_ERROR", 
                String.format("El monto debe ser mayor a $%.2f", MIN_AMOUNT));
        }
        
        if (context.amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BpmnError("VALIDATION_ERROR", 
                String.format("El monto excede el límite máximo de $%.2f", MAX_AMOUNT));
        }
        
        // ✅ Establecer flags para el proceso
        execution.setVariable("isAutoApprovalEligible", 
            context.amount.compareTo(AUTO_APPROVAL_LIMIT) <= 0);
        execution.setVariable("requiresCeoApproval", 
            context.amount.compareTo(new BigDecimal("10000")) > 0);
        execution.setVariable("isHighValue", 
            context.amount.compareTo(new BigDecimal("5000")) > 0);
    }
    
    /**
     * ✅ Validar datos del proveedor
     */
    private void validateSupplierData(ValidationContext context, DelegateExecution execution) throws BpmnError {
        
        if (StringUtils.isBlank(context.supplierName)) {
            throw new BpmnError("VALIDATION_ERROR", "El nombre del proveedor es requerido");
        }
        
        if (StringUtils.isNotBlank(context.supplierEmail) && !isValidEmail(context.supplierEmail)) {
            throw new BpmnError("VALIDATION_ERROR", "Email del proveedor inválido: " + context.supplierEmail);
        }
        
        // TODO: Aquí podrías validar contra una lista de proveedores aprobados
        // validateApprovedSupplier(context.supplierName);
    }
    
    /**
     * ✅ Establecer variables para el DMN Engine
     */
    private void setDmnVariables(ValidationContext context, DelegateExecution execution) {
        // Variables exactas que espera el DMN
        execution.setVariable("Monto", context.amount);
        execution.setVariable("Categoría", context.category);
        execution.setVariable("priority", context.priority != null ? context.priority : "NORMAL");
        
        // Variables adicionales para el proceso
        execution.setVariable("validatedAmount", context.amount);
        execution.setVariable("validatedCategory", context.category);
        execution.setVariable("validatedPriority", context.priority);
        
        logger.debug("🎯 Variables DMN establecidas - Monto: {}, Categoría: {}, Prioridad: {}", 
            context.amount, context.category, context.priority);
    }
    
    /**
     * ✅ Actualizar solicitud en base de datos
     */
    private void updateRequestInDatabase(ValidationContext context, DelegateExecution execution) {
        if (context.businessKey != null) {
            Optional<PurchaseRequest> optionalRequest = purchaseRequestRepository.findByBusinessKey(context.businessKey);
            if (optionalRequest.isPresent()) {
                PurchaseRequest request = optionalRequest.get();
                request.setStatus(PurchaseRequest.RequestStatus.IN_APPROVAL);
                request.setComments("Solicitud validada y en proceso de aprobación");
                purchaseRequestRepository.save(request);
                logger.debug("📝 Estado actualizado en BD para solicitud: {}", context.businessKey);
            }
        }
    }
    
    /**
     * ✅ Sugerir ruta de aprobación (para logging)
     */
    private String suggestApprovalRoute(ValidationContext context) {
        if (context.amount.compareTo(AUTO_APPROVAL_LIMIT) <= 0) {
            return "AUTO";
        } else if (context.amount.compareTo(new BigDecimal("10000")) > 0) {
            return "CEO";
        } else if (context.amount.compareTo(new BigDecimal("2000")) > 0) {
            return "MANAGER";
        } else {
            return "SUPERVISOR";
        }
    }
    
    /**
     * ✅ Validar formato de email
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    /**
     * ✅ Clase interna para el contexto de validación
     */
    private static class ValidationContext {
        String businessKey;
        String requesterName;
        String requesterEmail;
        String description;
        String department;
        String category;
        String priority;
        String supplierName;
        String supplierEmail;
        BigDecimal amount;
    }
}