package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.delegate;

import java.time.LocalDateTime;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;

public class UpdatePurchaseRequestDelegate implements JavaDelegate {
    @Autowired PurchaseRequestRepository repo;
    public void execute(DelegateExecution ctx) {
        String businessKey = ctx.getProcessBusinessKey();
        PurchaseRequest pr = repo.findByBusinessKey(businessKey).orElseThrow();
        pr.setStatus(PurchaseRequest.RequestStatus.APPROVED);
        pr.setApprovedBy(businessKey);
        pr.setApprovedAt(LocalDateTime.now());
        repo.save(pr);
    }
}
