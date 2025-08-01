package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.SupplierDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ExternalServiceClient;
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
    private final TaskService taskService;
    private final PurchaseRequestService purchaseRequestService;
    private final ExternalServiceClient externalServiceClient;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.info("🔥 Mostrando formulario de creación de solicitud");

        List<SupplierDto> suppliers = externalServiceClient.getSuppliers();
        List<String> currencies = externalServiceClient.getCurrencies();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currencies", currencies);
        model.addAttribute("categories", PurchaseRequest.PurchaseCategory.values());
        model.addAttribute("priorities", PurchaseRequest.Priority.values());

        return "process/create-purchase-request";
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

    @GetMapping("/monitor")
    public String processMonitor(Model model) {
        log.info("📊 Mostrando monitor de procesos");

        Long totalRequests = purchaseRequestService.getTotalRequests();
        Long pendingRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.PENDING);
        Long approvedRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.APPROVED);
        Long rejectedRequests = purchaseRequestService.getRequestsByStatus(PurchaseRequest.RequestStatus.REJECTED);

        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("rejectedRequests", rejectedRequests);

        List<Task> pendingTasks = taskService.createTaskQuery()
                .processDefinitionKey("purchase-request-process")
                .list();

        model.addAttribute("pendingTasks", pendingTasks);

        return "process/process-monitor";
    }
}
