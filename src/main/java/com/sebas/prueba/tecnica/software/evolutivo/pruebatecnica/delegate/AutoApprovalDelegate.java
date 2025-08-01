package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("autoApprovalDelegate")
public class AutoApprovalDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoApprovalDelegate.class);
    
    @Autowired
    private EmailNotificationDelegate emailService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Procesando aprobación automática - Process Instance: {}", execution.getProcessInstanceId());
        
        // Marcar como aprobado automáticamente
        execution.setVariable("approved", true);
        execution.setVariable("approvedBy", "SYSTEM_AUTO_APPROVAL");
        execution.setVariable("approvalDate", new Date());
        execution.setVariable("approvalComments", "Aprobación automática por monto menor al límite establecido");
        
        // Enviar notificación
        String requesterEmail = (String) execution.getVariable("requesterEmail");
        if (requesterEmail != null) {
            emailService.sendAutoApprovalNotification(execution);
        }
        
        logger.info("Aprobación automática completada para solicitud: {}", execution.getProcessInstanceId());
    }
}
