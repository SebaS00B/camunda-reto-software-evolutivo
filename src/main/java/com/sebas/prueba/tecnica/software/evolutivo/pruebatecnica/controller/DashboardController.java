package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.PurchaseRequestService;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service.ReportService;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final ReportService reportService;
    private final PurchaseRequestService purchaseRequestService;

    /**
     * 🎯 Dashboard principal
     */
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        log.info("🎯 Mostrando dashboard principal");
        
        try {
            // Obtener métricas del dashboard
            var metrics = reportService.getDashboardMetrics();
            model.addAttribute("metrics", metrics);
            
            // Obtener solicitudes recientes
            List<PurchaseRequest> recentRequests = purchaseRequestService.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();
            
            model.addAttribute("recentRequests", recentRequests);
            
            return "dashboard/index";
            
        } catch (Exception e) {
            log.error("❌ Error cargando dashboard: {}", e.getMessage(), e);
            model.addAttribute("message", "Error cargando datos del dashboard");
            model.addAttribute("messageType", "error");
            return "dashboard/index";
        }
    }

    /**
     * 📊 Página de reportes
     */
    @GetMapping("/reports")
    public String reports(Model model) {
        log.info("📊 Mostrando página de reportes");
        
        try {
            var auditReport = reportService.getAuditReport();
            model.addAttribute("auditReport", auditReport);
            
            return "dashboard/reports";
            
        } catch (Exception e) {
            log.error("❌ Error cargando reportes: {}", e.getMessage(), e);
            model.addAttribute("message", "Error cargando reportes");
            model.addAttribute("messageType", "error");
            return "dashboard/reports";
        }
    }
}