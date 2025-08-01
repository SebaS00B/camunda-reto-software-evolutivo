package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ExternalServiceClient;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessRestController {

    private final PurchaseRequestService purchaseRequestService;
    private final ExternalServiceClient externalServiceClient;

    /**
     * 📊 API para obtener métricas
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.info("📊 API: Obteniendo métricas del proceso");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("total", purchaseRequestService.getTotalRequests());
        metrics.put("pending", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.PENDING));
        metrics.put("approved", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.APPROVED));
        metrics.put("rejected", purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.REJECTED));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * 🌐 API para obtener proveedores (para select dinámico)
     */
    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        log.info("🌐 API: Obteniendo lista de proveedores");
        
        List<SupplierDto> suppliers = externalServiceClient.getSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    /**
     * 💱 API para obtener monedas
     */
    @GetMapping("/currencies")
    public ResponseEntity<List<String>> getCurrencies() {
        log.info("💱 API: Obteniendo lista de monedas");
        
        List<String> currencies = externalServiceClient.getCurrencies();
        return ResponseEntity.ok(currencies);
    }

    /**
     * 📋 API para obtener solicitudes por departamento
     */
    @GetMapping("/requests/department/{department}")
    public ResponseEntity<List<PurchaseRequest>> getRequestsByDepartment(@PathVariable String department) {
        log.info("📋 API: Obteniendo solicitudes del departamento: {}", department);
        
        List<PurchaseRequest> requests = purchaseRequestService.findByDepartment(department);
        return ResponseEntity.ok(requests);
    }

    /**
     * 🔍 API para buscar solicitud por ID
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<PurchaseRequest> getRequest(@PathVariable Long id) {
        log.info("🔍 API: Obteniendo detalles de solicitud: {}", id);
        
        return purchaseRequestService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}