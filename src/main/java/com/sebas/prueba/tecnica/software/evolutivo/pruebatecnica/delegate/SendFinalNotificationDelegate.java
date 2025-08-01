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
    private NotificationService notificationService; // ✅ Cambio aquí
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Enviando notificación final - Process Instance: {}", execution.getProcessInstanceId());
        
        Boolean approved = (Boolean) execution.getVariable("approved");
        String requesterEmail = (String) execution.getVariable("requesterEmail");
        String requestId = (String) execution.getVariable("requestId");
        
        if (requesterEmail == null || requestId == null) {
            logger.error("Faltan datos para enviar notificación - Email: {}, RequestId: {}", 
                requesterEmail, requestId);
            return;
        }
        
        if (approved != null && approved) {
            logger.info("Enviando notificación de APROBACIÓN");
            notificationService.sendApprovalNotification(requesterEmail, requestId, execution);
            execution.setVariable("finalStatus", "APPROVED");
            
        } else {
            logger.info("Enviando notificación de RECHAZO");
            notificationService.sendRejectionNotification(requesterEmail, requestId, execution);
            execution.setVariable("finalStatus", "REJECTED");
        }
        
        logger.info("Notificación final enviada a: {} - Estado: {}", 
            requesterEmail, approved ? "APROBADA" : "RECHAZADA");
        
        execution.setVariable("processCompletedAt", java.time.LocalDateTime.now().toString());
    }
}
