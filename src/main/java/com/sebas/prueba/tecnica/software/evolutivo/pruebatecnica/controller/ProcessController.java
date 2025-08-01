package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.CurrencyService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ExternalServiceClient;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.NotificationService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

import jakarta.validation.Valid;

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
    public ResponseEntity<?> startProcess(@RequestBody PurchaseRequest purchaseRequest) {
        // 1) Generar businessKey si aún no existe
        purchaseRequest.generateBusinessKey();

        // 2) Guardar la entidad en la BD (aquí JPA asigna el id)
        purchaseRequest = requestRepo.save(purchaseRequest);

        // 3) Mapear a variables de Camunda
        Map<String, Object> vars = new HashMap<>();
        vars.put("businessKey",    purchaseRequest.getBusinessKey());
        vars.put("requestId",    purchaseRequest.getBusinessKey());
        vars.put("requesterName",  purchaseRequest.getRequesterName());
        vars.put("requesterEmail", purchaseRequest.getRequesterEmail());
        vars.put("department",     purchaseRequest.getDepartment());
        vars.put("description",    purchaseRequest.getDescription());
        vars.put("totalAmount",    purchaseRequest.getTotalAmount());
        vars.put("currency",       purchaseRequest.getCurrency());
        vars.put("category",       purchaseRequest.getCategory().name());
        vars.put("priority",       purchaseRequest.getPriority().name());
        vars.put("supplierName",   purchaseRequest.getSupplierName());
        vars.put("supplierEmail",  purchaseRequest.getSupplierEmail());
        vars.put("dueDate",        purchaseRequest.getDueDate());

        // 4) Arrancar la instancia de proceso con key + businessKey
        ProcessInstance pi = runtimeService
            .startProcessInstanceByKey(
                "purchase-request-process",
                purchaseRequest.getBusinessKey(),
                vars
            );

        log.info("🔥 Started process instance {}, businessKey={}", pi.getId(), purchaseRequest.getBusinessKey());

        // 5) Guardar el processInstanceId en la entidad
        purchaseRequest.setProcessInstanceId(pi.getId());
        requestRepo.save(purchaseRequest);

        // --- 6) Enviar correo automáticamente ---
        try {
            notificationService.sendFinalNotification(
                purchaseRequest,
                "CREATED",
                "Su solicitud de compra ha sido creada exitosamente."
            );
            log.info("✅ Email enviado para solicitud {}", purchaseRequest.getBusinessKey());
        } catch (Exception e) {
            log.error("❌ Error enviando email para solicitud {}: {}", purchaseRequest.getBusinessKey(), e.getMessage());
            // Puedes decidir si quieres devolver error o continuar sin fallo
        }

        // 7) Responder al frontend
        Map<String, Object> resp = Map.of(
            "success",           true,
            "processInstanceId", pi.getId(),
            "businessKey",       purchaseRequest.getBusinessKey()
        );

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/start")
    public String startProcess(@Valid @ModelAttribute PurchaseRequest request,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("⚠️ Errores de validación en el formulario");
            model.addAttribute("suppliers", externalServiceClient.getSuppliers());
            model.addAttribute("currencies", externalServiceClient.getCurrencies());
            model.addAttribute("categories", PurchaseRequest.PurchaseCategory.values());
            model.addAttribute("priorities", PurchaseRequest.Priority.values());
            return "process/create-purchase-request";
        }

        try {
            log.info("🚀 Iniciando proceso Purchase Request para: {}", request.getRequesterName());

            Map<String, Object> variables = new HashMap<>();
            variables.put("requesterName", request.getRequesterName());
            variables.put("requesterEmail", request.getRequesterEmail());
            variables.put("department", request.getDepartment());
            variables.put("description", request.getDescription());
            variables.put("totalAmount", request.getTotalAmount().doubleValue());
            variables.put("currency", request.getCurrency());
            variables.put("category", request.getCategory().name());
            variables.put("priority", request.getPriority().name());
            variables.put("supplierName", request.getSupplierName());
            variables.put("supplierEmail", request.getSupplierEmail());

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    "purchase-request-process",
                    variables
            );

            log.info("✅ Proceso iniciado exitosamente: {}", processInstance.getId());

            model.addAttribute("processInstanceId", processInstance.getId());
            model.addAttribute("message", "¡Solicitud creada exitosamente!");
            model.addAttribute("messageType", "success");

            return "process/process-started";

        } catch (Exception e) {
            log.error("❌ Error iniciando proceso: {}", e.getMessage(), e);
            model.addAttribute("message", "Error iniciando proceso: " + e.getMessage());
            model.addAttribute("messageType", "error");

            model.addAttribute("suppliers", externalServiceClient.getSuppliers());
            model.addAttribute("currencies", externalServiceClient.getCurrencies());
            model.addAttribute("categories", PurchaseRequest.PurchaseCategory.values());
            model.addAttribute("priorities", PurchaseRequest.Priority.values());

            return "process/create-purchase-request";
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
