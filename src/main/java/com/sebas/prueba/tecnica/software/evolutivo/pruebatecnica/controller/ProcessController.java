package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.CurrencyService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ExternalServiceClient;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.NotificationService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/process")
@RequiredArgsConstructor
@Slf4j
public class ProcessController {

    private final RuntimeService runtimeService;
    private final PurchaseRequestService purchaseRequestService;
    private final ExternalServiceClient externalServiceClient;
    private final PurchaseRequestRepository requestRepo;
    private final NotificationService notificationService; // Para enviar mails

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.info("🔥 Mostrando formulario de creación de solicitud");

        List<SupplierDto> suppliers = externalServiceClient.getSuppliers();
        

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currencies", currencyService.getCurrencyOptions());
        model.addAttribute("purchaseRequest", new PurchaseRequest());
        model.addAttribute("categories", PurchaseRequest.PurchaseCategory.values());
        model.addAttribute("priorities", PurchaseRequest.Priority.values());

        return "process/create-purchase-request";
    }
@PostMapping("/start-api")
public ResponseEntity<?> startProcess(@RequestBody Map<String, Object> requestData) {
    try {
        log.info("🔥 Iniciando proceso con datos: {}", requestData);
        
        // ✅ Crear PurchaseRequest desde el Map
        PurchaseRequest purchaseRequest = new PurchaseRequest();
        
        // Mapear campos básicos
        purchaseRequest.setRequesterName((String) requestData.get("requesterName"));
        purchaseRequest.setRequesterEmail((String) requestData.get("requesterEmail"));
        purchaseRequest.setDepartment((String) requestData.get("department"));
        purchaseRequest.setDescription((String) requestData.get("description"));
        
        // ✅ Manejo seguro del monto
        Object amountObj = requestData.get("totalAmount");
        if (amountObj instanceof Number) {
            purchaseRequest.setTotalAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
        } else {
            throw new IllegalArgumentException("totalAmount debe ser un número");
        }
        
        purchaseRequest.setCurrency((String) requestData.get("currency"));
        purchaseRequest.setSupplierName((String) requestData.get("supplierName"));
        purchaseRequest.setSupplierEmail((String) requestData.get("supplierEmail"));
        
        // ✅ Manejo seguro de enums
        String categoryStr = (String) requestData.get("category");
        String priorityStr = (String) requestData.get("priority");
        
        try {
            purchaseRequest.setCategory(PurchaseRequest.PurchaseCategory.valueOf(categoryStr));
            purchaseRequest.setPriority(PurchaseRequest.Priority.valueOf(priorityStr));
        } catch (IllegalArgumentException e) {
            log.error("❌ Enum inválido - Category: {}, Priority: {}", categoryStr, priorityStr);
            throw new IllegalArgumentException("Categoría o prioridad inválida: " + e.getMessage());
        }
        
        // ✅ CORRECCIÓN DE FECHA: Manejo flexible
        Object dueDateObj = requestData.get("dueDate");
        if (dueDateObj != null) {
            try {
                String dueDateStr = dueDateObj.toString();
                LocalDateTime dueDateTime;
                
                if (dueDateStr.contains("T")) {
                    // Ya tiene formato LocalDateTime: "2025-08-02T23:59:59"
                    dueDateTime = LocalDateTime.parse(dueDateStr);
                } else {
                    // Solo fecha: "2025-08-02" -> convertir a "2025-08-02T23:59:59"
                    LocalDate dueDate = LocalDate.parse(dueDateStr);
                    dueDateTime = dueDate.atTime(23, 59, 59);
                }
                
                purchaseRequest.setDueDate(dueDateTime);
                log.info("✅ Fecha procesada correctamente: {}", dueDateTime);
                
            } catch (Exception e) {
                log.error("❌ Error procesando fecha: {}", dueDateObj, e);
                throw new IllegalArgumentException("Formato de fecha inválido: " + dueDateObj);
            }
        }

        // 1) Generar businessKey
        purchaseRequest.generateBusinessKey();

        // 2) Guardar la entidad en la BD
        purchaseRequest = requestRepo.save(purchaseRequest);
        log.info("✅ Solicitud guardada con ID: {}", purchaseRequest.getId());

        // 3) ✅ Mapear a variables de Camunda (nombres exactos para DMN)
        Map<String, Object> vars = new HashMap<>();
        vars.put("businessKey", purchaseRequest.getBusinessKey());
        vars.put("requestId", purchaseRequest.getBusinessKey());
        vars.put("requesterName", purchaseRequest.getRequesterName());
        vars.put("requesterEmail", purchaseRequest.getRequesterEmail());
        vars.put("department", purchaseRequest.getDepartment());
        vars.put("description", purchaseRequest.getDescription());
        
        // ✅ Variables para el DMN (nombres exactos)
        vars.put("Monto", purchaseRequest.getTotalAmount().doubleValue());  // Para DMN
        vars.put("Categoría", purchaseRequest.getCategory().name());        // Para DMN
        vars.put("priority", purchaseRequest.getPriority().name());         // Para DMN
        
        // Variables adicionales
        vars.put("totalAmount", purchaseRequest.getTotalAmount().doubleValue());
        vars.put("currency", purchaseRequest.getCurrency());
        vars.put("category", purchaseRequest.getCategory().name());
        vars.put("supplierName", purchaseRequest.getSupplierName());
        vars.put("supplierEmail", purchaseRequest.getSupplierEmail());
        
        if (purchaseRequest.getDueDate() != null) {
            vars.put("dueDate", purchaseRequest.getDueDate());
        }

        log.info("🔧 Variables para Camunda: {}", vars);

        // 4) Arrancar la instancia de proceso
        ProcessInstance pi = runtimeService
            .startProcessInstanceByKey(
                "purchase-request-process",
                purchaseRequest.getBusinessKey(),
                vars
            );

        log.info("🚀 Proceso iniciado - ID: {}, BusinessKey: {}", pi.getId(), purchaseRequest.getBusinessKey());

        // 5) Guardar el processInstanceId
        purchaseRequest.setProcessInstanceId(pi.getId());
        requestRepo.save(purchaseRequest);

        // 6) Enviar correo de confirmación
        try {
            notificationService.sendFinalNotification(
                purchaseRequest,
                "CREATED",
                "Su solicitud de compra ha sido creada exitosamente."
            );
            log.info("✅ Email enviado para solicitud {}", purchaseRequest.getBusinessKey());
        } catch (Exception e) {
            log.error("❌ Error enviando email: {}", e.getMessage());
        }

        // 7) Responder al frontend
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("processInstanceId", pi.getId());
        response.put("businessKey", purchaseRequest.getBusinessKey());
        response.put("message", "Solicitud creada exitosamente");

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        log.error("❌ Error creando solicitud: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        errorResponse.put("error", e.getClass().getSimpleName());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
    

    @GetMapping("/list")
    public String listRequests(Model model) {
        log.info("📋 Mostrando lista de solicitudes");

        List<PurchaseRequest> requests = purchaseRequestService.findAll();
        model.addAttribute("requests", requests);

        return "process/request-list";
    }

    @GetMapping("/details/{id}")
    public String viewRequestDetails(@PathVariable Long id, Model model) {
        log.info("👁️ Mostrando detalles de solicitud: {}", id);

        PurchaseRequest request = purchaseRequestService.findById(id).orElse(null);
        if (request == null) {
            model.addAttribute("message", "Solicitud no encontrada");
            model.addAttribute("messageType", "error");
            return "error/404";
        }

        model.addAttribute("request", request);
        return "process/request-details";
    }

    
}
