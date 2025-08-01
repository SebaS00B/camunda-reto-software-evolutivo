package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.NotificationService;

@Component("sendFinalNotificationDelegate")
public class SendFinalNotificationDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFinalNotificationDelegate.class);
    
    @Autowired
    private NotificationService emailService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Enviando notificación final - Process Instance: {}", execution.getProcessInstanceId());
        
        String finalStatus = (String) execution.getVariable("finalStatus");
        String requesterEmail = (String) execution.getVariable("requesterEmail");
        String requestId = (String) execution.getVariable("requestId");
        
        if ("APPROVED".equals(finalStatus)) {
            emailService.sendApprovalNotification(requesterEmail, requestId, execution);
        } else {
            emailService.sendRejectionNotification(requesterEmail, requestId, execution);
        }
        
        logger.info("Notificación final enviada. Estado: {}", finalStatus);
    }
}
