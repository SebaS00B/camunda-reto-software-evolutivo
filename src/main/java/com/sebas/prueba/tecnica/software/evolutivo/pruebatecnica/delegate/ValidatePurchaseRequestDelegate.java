package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.common.util.StringUtils;

@Component("validatePurchaseRequestDelegate")
public class ValidatePurchaseRequestDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidatePurchaseRequestDelegate.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Validando solicitud de compra - Process Instance: {}", execution.getProcessInstanceId());
        
        // Obtener variables del proceso
        Double amount = (Double) execution.getVariable("amount");
        String description = (String) execution.getVariable("description");
        String category = (String) execution.getVariable("category");
        
        // Validaciones básicas
        if (amount == null || amount <= 0) {
            throw new BpmnError("VALIDATION_ERROR", "El monto debe ser mayor a 0");
        }
        
        if (StringUtils.isEmpty(description)) {
            throw new BpmnError("VALIDATION_ERROR", "La descripción es requerida");
        }
        
        // Establecer variables para el DMN
        execution.setVariable("Monto", amount);
        execution.setVariable("Categoría", category);
        execution.setVariable("validationStatus", "APPROVED");
        
        logger.info("Solicitud validada exitosamente. Monto: {}, Categoría: {}", amount, category);
    }
}