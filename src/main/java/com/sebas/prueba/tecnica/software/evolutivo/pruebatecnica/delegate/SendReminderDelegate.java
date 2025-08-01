package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.NotificationService;

@Component("sendReminderDelegate")
public class SendReminderDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(SendReminderDelegate.class);
    
    @Autowired
    private NotificationService emailService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Enviando recordatorio - Process Instance: {}", execution.getProcessInstanceId());
        
        String currentTask = getCurrentTaskName(execution);
        String assigneeEmail = getAssigneeEmail(execution);
        
        if (assigneeEmail != null) {
            emailService.sendReminderEmail(
                assigneeEmail, 
                (String) execution.getVariable("requestId"),
                currentTask,
                (Double) execution.getVariable("amount"),
                (String) execution.getVariable("description")
            );
            
            // Incrementar contador de recordatorios
            Integer reminderCount = (Integer) execution.getVariable("reminderCount");
            execution.setVariable("reminderCount", (reminderCount != null ? reminderCount : 0) + 1);
        }
        
        logger.info("Recordatorio enviado para tarea: {}", currentTask);
    }
    
    private String getCurrentTaskName(DelegateExecution execution) {
        // Lógica para determinar la tarea actual
        return "Approval Task";
    }
    
    private String getAssigneeEmail(DelegateExecution execution) {
        // Lógica para obtener el email del asignado
        return "approver@company.com";
    }
}

