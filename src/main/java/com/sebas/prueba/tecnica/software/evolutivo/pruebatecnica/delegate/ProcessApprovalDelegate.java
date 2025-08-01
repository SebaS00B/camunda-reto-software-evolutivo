package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

@Component("processApprovalDelegate")
public class ProcessApprovalDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessApprovalDelegate.class);
    
    @Autowired
    private PurchaseRequestService purchaseService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Procesando aprobación - Process Instance: {}", execution.getProcessInstanceId());
        
        // Actualizar estado en base de datos
        String requestId = (String) execution.getVariable("requestId");
        String approvedBy = (String) execution.getVariable("approvedBy");
        String comments = (String) execution.getVariable("approvalComments");
        
        purchaseService.updateRequestStatus(requestId, "APPROVED", approvedBy, comments);
        
        execution.setVariable("finalStatus", "APPROVED");
        execution.setVariable("processEndDate", new Date());
        
        logger.info("Solicitud {} aprobada por {}", requestId, approvedBy);
    }
}
