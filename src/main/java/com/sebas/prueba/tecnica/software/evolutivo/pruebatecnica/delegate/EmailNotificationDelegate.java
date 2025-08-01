package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("emailNotificationDelegate")
public class EmailNotificationDelegate implements JavaDelegate {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            String to = (String) execution.getVariable("email");

            if (to == null || to.isEmpty()) {
                throw new IllegalArgumentException("La variable de correo 'email' no está definida o está vacía.");
            }

            String requestorName = (String) execution.getVariable("requestorName");
            String processInstanceId = execution.getProcessInstanceId();
            String subject = "Confirmación de solicitud recibida";
            String body = String.format(
                "Hola %s,\n\nTu solicitud ha sido registrada correctamente en el sistema.\n\n" +
                "Número de proceso: %s\nEstado actual: En revisión\n\n" +
                "Gracias por usar nuestro sistema.\n\nSaludos,\nEquipo de Procesos",
                requestorName != null ? requestorName : "Usuario",
                processInstanceId
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            System.out.println("Correo enviado a " + to + " con asunto: " + subject);

        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
            // Si deseas lanzar el error para que el flujo de Camunda falle explícitamente:
            throw e;
        }
    }
      public void sendGeneralNotification(DelegateExecution execution) {
    String to = (String) execution.getVariable("email");
    String subject = "Notificación de proceso";
    String body = "El proceso " + execution.getProcessInstanceId() + " ha avanzado.";

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(body);
    mailSender.send(msg);
  }
    // ✅ Implementación concreta para auto aprobación
  public void sendAutoApprovalNotification(DelegateExecution execution) {
    String to = (String) execution.getVariable("requesterEmail");
    if (to == null || to.isEmpty()) {
      return;
    }

    String subject = "Solicitud aprobada automáticamente";
    String body = String.format("Hola,\n\nTu solicitud con ID %s fue aprobada automáticamente porque el monto está por debajo del límite establecido.\n\nSaludos,\nEquipo de Compras",
            execution.getProcessInstanceId());

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(body);
    mailSender.send(msg);
  }
}
