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
  public void execute(DelegateExecution execution) {
    String to = (String) execution.getVariable("email");
    String subject = "Notificación de proceso";
    String body = "El proceso " + execution.getProcessInstanceId() + " ha avanzado.";

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(body);
    mailSender.send(msg);
  }

  public void sendAutoApprovalNotification(DelegateExecution execution) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sendAutoApprovalNotification'");
  }
}