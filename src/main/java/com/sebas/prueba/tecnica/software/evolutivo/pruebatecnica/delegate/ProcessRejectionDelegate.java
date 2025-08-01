package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

@Component("processRejectionDelegate")
public class ProcessRejectionDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessRejectionDelegate.class);
    
    @Autowired
    private PurchaseRequestService purchaseService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Procesando rechazo - Process Instance: {}", execution.getProcessInstanceId());
        
        String requestId = (String) execution.getVariable("requestId");
        String rejectedBy = (String) execution.getVariable("rejectedBy");
        String comments = (String) execution.getVariable("rejectionComments");
        
        purchaseService.updateRequestStatus(requestId, "REJECTED", rejectedBy, comments);
        
        execution.setVariable("finalStatus", "REJECTED");
        execution.setVariable("processEndDate", new Date());
        
        logger.info("Solicitud {} rechazada por {}", requestId, rejectedBy);
    }
}