package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import org.camunda.bpm.engine.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/process")
public class ProcessMonitorController {

  private final PurchaseRequestService purchaseRequestService;
  private final TaskService taskService;

  @GetMapping("/monitor")
  public String monitor(Model model) {
    log.info("🔍 Mostrando monitor de procesos");
    long running = taskService.createTaskQuery()
                              .processDefinitionKey("purchase-request-process")
                              .active()
                              .count();
    long total = purchaseRequestService.getTotalRequests();
    model.addAttribute("runningCount", running);
    model.addAttribute("totalRequests", total);
    return "process/monitor";
  }
  }
