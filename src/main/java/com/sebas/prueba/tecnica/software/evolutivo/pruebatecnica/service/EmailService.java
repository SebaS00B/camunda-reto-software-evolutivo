package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.contact.email}")
    private String fromEmail;
    
    public void sendReminderEmail(String to, String requestId, String taskName, Double amount, String description) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("🔔 Recordatorio: Solicitud de Compra Pendiente - " + requestId);
            
            String content = buildReminderEmailContent(requestId, taskName, amount, description);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("Recordatorio enviado a: {} para solicitud: {}", to, requestId);
            
        } catch (Exception e) {
            logger.error("Error enviando recordatorio: {}", e.getMessage(), e);
        }
    }
    
    public void sendApprovalNotification(String to, String requestId, DelegateExecution execution) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("✅ Solicitud Aprobada - " + requestId);
            
            String content = buildApprovalEmailContent(requestId, execution);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("Notificación de aprobación enviada a: {}", to);
            
        } catch (Exception e) {
            logger.error("Error enviando notificación de aprobación: {}", e.getMessage(), e);
        }
    }
    
    public void sendRejectionNotification(String to, String requestId, DelegateExecution execution) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("❌ Solicitud Rechazada - " + requestId);
            
            String content = buildRejectionEmailContent(requestId, execution);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("Notificación de rechazo enviada a: {}", to);
            
        } catch (Exception e) {
            logger.error("Error enviando notificación de rechazo: {}", e.getMessage(), e);
        }
    }
    
    private String buildReminderEmailContent(String requestId, String taskName, Double amount, String description) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #f39c12;">🔔 Recordatorio - Solicitud Pendiente</h2>
                    <p>Tienes una solicitud de compra pendiente de aprobación:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #f39c12;">
                        <strong>ID:</strong> %s<br/>
                        <strong>Tarea:</strong> %s<br/>
                        <strong>Monto:</strong> $%.2f<br/>
                        <strong>Descripción:</strong> %s
                    </div>
                    
                    <p style="margin-top: 20px;">
                        <a href="http://localhost:8080/camunda/app/tasklist/" 
                           style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                            Ver en Camunda Tasklist
                        </a>
                    </p>
                    
                    <p style="color: #6c757d; font-size: 12px; margin-top: 30px;">
                        Este es un mensaje automático del sistema BPM - Software Evolutivo
                    </p>
                </div>
            </body>
            </html>
            """, requestId, taskName, amount, description);
    }
    
    private String buildApprovalEmailContent(String requestId, DelegateExecution execution) {
        Double amount = (Double) execution.getVariable("amount");
        String description = (String) execution.getVariable("description");
        String approvedBy = (String) execution.getVariable("approvedBy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #28a745;">✅ Solicitud Aprobada</h2>
                    <p>Tu solicitud de compra ha sido <strong>APROBADA</strong>:</p>
                    
                    <div style="background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745;">
                        <strong>ID:</strong> %s<br/>
                        <strong>Monto:</strong> $%.2f<br/>
                        <strong>Descripción:</strong> %s<br/>
                        <strong>Aprobado por:</strong> %s
                    </div>
                    
                    <p style="color: #155724; margin-top: 20px;">
                        Puedes proceder con la compra según los procedimientos establecidos.
                    </p>
                </div>
            </body>
            </html>
            """, requestId, amount, description, approvedBy);
    }
    
    private String buildRejectionEmailContent(String requestId, DelegateExecution execution) {
        Double amount = (Double) execution.getVariable("amount");
        String description = (String) execution.getVariable("description");
        String rejectedBy = (String) execution.getVariable("rejectedBy");
        String comments = (String) execution.getVariable("rejectionComments");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc3545;">❌ Solicitud Rechazada</h2>
                    <p>Tu solicitud de compra ha sido <strong>RECHAZADA</strong>:</p>
                    
                    <div style="background-color: #f8d7da; padding: 15px; border-left: 4px solid #dc3545;">
                        <strong>ID:</strong> %s<br/>
                        <strong>Monto:</strong> $%.2f<br/>
                        <strong>Descripción:</strong> %s<br/>
                        <strong>Rechazado por:</strong> %s<br/>
                        %s
                    </div>
                    
                    <p style="color: #721c24; margin-top: 20px;">
                        Si tienes preguntas, contacta al aprobador para más detalles.
                    </p>
                </div>
            </body>
            </html>
            """, requestId, amount, description, rejectedBy, 
            comments != null ? "<strong>Comentarios:</strong> " + comments : "");
    }
}