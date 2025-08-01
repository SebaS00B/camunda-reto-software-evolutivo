package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.NotificationService;

import java.math.BigDecimal;
import java.util.List;

@Component("sendReminderDelegate")
public class SendReminderDelegate implements JavaDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(SendReminderDelegate.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private TaskService taskService;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("⏰ Enviando recordatorio - Process Instance: {}", execution.getProcessInstanceId());
        
        String currentTask = getCurrentTaskName(execution);
        String assigneeEmail = getAssigneeEmail(execution);
        String requestId = (String) execution.getVariable("requestId");
        
        // ✅ CORREGIDO: Usar las variables correctas
        BigDecimal monto = null;
        Object montoVar = execution.getVariable("Monto");  // Con M mayúscula
        if (montoVar != null) {
            if (montoVar instanceof BigDecimal) {
                monto = (BigDecimal) montoVar;
            } else if (montoVar instanceof Double) {
                monto = BigDecimal.valueOf((Double) montoVar);
            } else if (montoVar instanceof Integer) {
                monto = BigDecimal.valueOf((Integer) montoVar);
            }
        }
        
        String categoria = (String) execution.getVariable("Categoría");  // Con tilde
        String priority = (String) execution.getVariable("priority");    // Sin espacio extra
        
        logger.info("📊 Variables del proceso: Monto={}, Categoría={}, Priority={}", monto, categoria, priority);
        
        if (assigneeEmail != null && requestId != null) {
            String description = String.format("%s - %s", 
                categoria != null ? categoria : "N/A", 
                priority != null ? priority : "N/A"
            );
            
            notificationService.sendReminderEmail(
                assigneeEmail, 
                requestId,
                currentTask,
                monto != null ? monto : BigDecimal.ZERO,
                description
            );
            
            // Incrementar contador de recordatorios
            Integer reminderCount = (Integer) execution.getVariable("reminderCount");
            execution.setVariable("reminderCount", (reminderCount != null ? reminderCount : 0) + 1);
            
            logger.info("✅ Recordatorio enviado a: {} para tarea: {}", assigneeEmail, currentTask);
        } else {
            logger.warn("❌ No se pudo enviar recordatorio - Email: {}, RequestId: {}", assigneeEmail, requestId);
        }
    }
    
    private String getCurrentTaskName(DelegateExecution execution) {
        try {
            // Buscar la tarea activa actual en el proceso
            List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(execution.getProcessInstanceId())
                .active()
                .list();
            
            if (!activeTasks.isEmpty()) {
                return activeTasks.get(0).getName();
            }
            
            // Fallback: determinar por approvalRoute
            String approvalRoute = (String) execution.getVariable("approvalRoute");
            if (approvalRoute != null) {
                switch (approvalRoute) {
                    case "SUPERVISOR": return "APROBACION SUPERVISOR";
                    case "MANAGER": return "APROBACION GERENTE"; 
                    case "CEO": return "APROBACION CEO";
                    default: return "Tarea de Aprobación";
                }
            }
            
            return "Tarea de Aprobación";
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo nombre de tarea: {}", e.getMessage());
            return "Tarea de Aprobación";
        }
    }
    
    private String getAssigneeEmail(DelegateExecution execution) {
        try {
            String approvalRoute = (String) execution.getVariable("approvalRoute");
            
            if (approvalRoute == null) {
                logger.warn("⚠️ approvalRoute es null, usando admin por defecto");
                return "admin@softwareevolutivo.com";
            }
            
            // Mapear según las rutas definidas en tu DMN
            switch (approvalRoute) {
                case "SUPERVISOR": 
                    return "supervisor@softwareevolutivo.com";
                case "MANAGER": 
                    return "manager@softwareevolutivo.com";
                case "CEO": 
                    return "ceo@softwareevolutivo.com";
                default:
                    logger.warn("⚠️ Ruta de aprobación desconocida: {}", approvalRoute);
                    return "admin@softwareevolutivo.com";
            }
        } catch (Exception e) {
            logger.error("❌ Error obteniendo email del asignado: {}", e.getMessage());
            return "admin@softwareevolutivo.com"; // Email por defecto
        }
    }
}