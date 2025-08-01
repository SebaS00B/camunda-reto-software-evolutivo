package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reports")
public class ReportsController {

  private final ReportService reportService;

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    log.info("📊 Mostrando reportes KPI");
    var metrics = reportService.getDashboardMetrics();
    model.addAttribute("metrics", metrics);
    return "reports/dashboard";
  }

}