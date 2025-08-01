package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:demo.bpm.consultant@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public void sendApprovalNotification(PurchaseRequest request, String approverEmail) {
        try {
            log.info("📧 Enviando notificación de aprobación a: {}", approverEmail);

            String subject = String.format("Nueva solicitud de compra para aprobar - %s", request.getBusinessKey());
            String body = buildApprovalNotificationBody(request);

            sendEmail(approverEmail, subject, body);

        } catch (Exception e) {
            log.error("❌ Error enviando notificación de aprobación: {}", e.getMessage(), e);
        }
    }

    public void sendReminderNotification(PurchaseRequest request, String approverEmail, String taskName) {
        try {
            log.info("⏰ Enviando recordatorio a: {}", approverEmail);

            String subject = String.format("Recordatorio: Solicitud pendiente - %s", request.getBusinessKey());
            String body = buildReminderNotificationBody(request, taskName);

            sendEmail(approverEmail, subject, body);

        } catch (Exception e) {
            log.error("❌ Error enviando recordatorio: {}", e.getMessage(), e);
        }
    }

    public void sendFinalNotification(PurchaseRequest request, String status, String message) {
        try {
            log.info("📬 Enviando notificación final a: {}", request.getRequesterEmail());

            String subject = String.format("Estado de su solicitud - %s", request.getBusinessKey());
            String body = buildFinalNotificationBody(request, status, message);

            sendEmail(request.getRequesterEmail(), subject, body);

        } catch (Exception e) {
            log.error("❌ Error enviando notificación final: {}", e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("📧 [SIMULADO] Email a: {} | Asunto: {}", to, subject);
            log.debug("📧 [SIMULADO] Cuerpo:\n{}", body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a: {}", to);

        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", to, e.getMessage());
        }
    }

    private String buildApprovalNotificationBody(PurchaseRequest request) {
        return String.format("""
            Estimado aprobador,
            
            Tiene una nueva solicitud de compra pendiente de aprobación:
            
            📋 DETALLES DE LA SOLICITUD:
            • Número: %s
            • Solicitante: %s (%s)
            • Departamento: %s
            • Descripción: %s
            • Monto: %s %s
            • Categoría: %s
            • Prioridad: %s
            • Proveedor: %s
            
            🔗 Para revisar y aprobar la solicitud, acceda a:
            %s/camunda/app/tasklist
            
            ⏰ Fecha límite: %s
            
            Saludos,
            Sistema BPM - Software Evolutivo
            """,
            request.getBusinessKey(),
            request.getRequesterName(),
            request.getRequesterEmail(),
            request.getDepartment(),
            request.getDescription(),
            request.getCurrency(),
            request.getTotalAmount(),
            request.getCategory().getDisplayName(),
            request.getPriority().getDisplayName(),
            request.getSupplierName(),
            baseUrl,
            request.getDueDate()
        );
    }

    private String buildReminderNotificationBody(PurchaseRequest request, String taskName) {
        return String.format("""
            Estimado aprobador,
            
            ⏰ RECORDATORIO: Tiene una solicitud pendiente de aprobación
            
            📋 DETALLES:
            • Número: %s
            • Tarea: %s
            • Solicitante: %s
            • Monto: %s %s
            • Días pendiente: %d
            
            🔗 Acceda al sistema para completar la aprobación:
            %s/camunda/app/tasklist
            
            Este es el recordatorio #%d para esta solicitud.
            
            Saludos,
            Sistema BPM - Software Evolutivo
            """,
            request.getBusinessKey(),
            taskName,
            request.getRequesterName(),
            request.getCurrency(),
            request.getTotalAmount(),
            java.time.Duration.between(request.getCreatedAt(), java.time.LocalDateTime.now()).toDays(),
            baseUrl,
            request.getReminderCount()
        );
    }

    private String buildFinalNotificationBody(PurchaseRequest request, String status, String message) {
        String statusIcon = "APPROVED".equals(status) ? "✅" : "❌";
        
        return String.format("""
            Estimado %s,
            
            %s ESTADO DE SU SOLICITUD: %s
            
            📋 DETALLES:
            • Número: %s
            • Descripción: %s
            • Monto: %s %s
            • Estado: %s
            
            💬 Mensaje: %s
            
            🕐 Tiempo de procesamiento: %d horas
            
            🔗 Para más detalles, consulte el sistema:
            %s/dashboard
            
            Gracias por usar nuestro sistema BPM.
            
            Saludos,
            Software Evolutivo
            """,
            request.getRequesterName(),
            statusIcon,
            status,
            request.getBusinessKey(),
            request.getDescription(),
            request.getCurrency(),
            request.getTotalAmount(),
            request.getStatus().getDisplayName(),
            message,
            request.getProcessingTimeHours() != null ? request.getProcessingTimeHours() : 0,
            baseUrl
        );
    }
}
