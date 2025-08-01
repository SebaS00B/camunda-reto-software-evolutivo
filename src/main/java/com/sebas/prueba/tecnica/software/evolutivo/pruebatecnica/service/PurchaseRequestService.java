package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PurchaseRequestService {

    private final PurchaseRequestRepository repository;

    public PurchaseRequest save(PurchaseRequest request) {
        log.debug("💾 Guardando solicitud: {}", request.getBusinessKey());
        return repository.save(request);
    }

    public Optional<PurchaseRequest> findById(Long id) {
        return repository.findById(id);
    }

    public PurchaseRequest findByProcessInstanceId(String processInstanceId) {
        return repository.findByProcessInstanceId(processInstanceId).orElse(null);
    }

    public List<PurchaseRequest> findByRequesterEmail(String email) {
        return repository.findByRequesterEmail(email);
    }

    public List<PurchaseRequest> findByStatus(PurchaseRequest.RequestStatus status) {
        return repository.findByStatus(status);
    }

    public List<PurchaseRequest> findAll() {
        return repository.findAll();
    }

    public List<PurchaseRequest> findOverdueRequests() {
        return repository.findOverdueRequests(LocalDateTime.now());
    }

    public Long getTotalRequests() {
        return repository.count();
    }

    public Long getRequestsByStatus(PurchaseRequest.RequestStatus status) {
        return repository.countByStatus(status);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<PurchaseRequest> findByDepartment(String department) {
    return repository.findByDepartment(department);
}
}
