package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.AuditReportDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.DashboardMetricsDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final PurchaseRequestRepository repository;
    private final HistoryService historyService;
    private final TaskService taskService;

    public DashboardMetricsDto getDashboardMetrics() {
        log.info("📊 Generando métricas del dashboard");

        try {
            // Contadores básicos
            Long totalRequests = repository.count();
            Long pendingRequests = repository.countByStatus(PurchaseRequest.RequestStatus.PENDING);
            Long approvedRequests = repository.countByStatus(PurchaseRequest.RequestStatus.APPROVED);
            Long rejectedRequests = repository.countByStatus(PurchaseRequest.RequestStatus.REJECTED);

            // Métricas de tiempo
            Double averageProcessingTime = repository.getAverageProcessingTime();
            Long overdueRequests = (long) repository.findOverdueRequests(java.time.LocalDateTime.now()).size();

            // Métricas financieras
            BigDecimal totalApprovedAmount = repository.getTotalApprovedAmount();
            String formattedAmount = totalApprovedAmount != null ? 
                String.format("$%.2f", totalApprovedAmount) : "$0.00";

            // Distribución por categoría
            List<Object[]> categoryData = repository.getRequestsByCategory();
            List<DashboardMetricsDto.CategoryMetricDto> categoryMetrics = categoryData.stream()
                .map(row -> DashboardMetricsDto.CategoryMetricDto.builder()
                    .category(((PurchaseRequest.PurchaseCategory) row[0]).getDisplayName())
                    .count((Long) row[1])
                    .percentage(calculatePercentage((Long) row[1], totalRequests))
                    .build())
                .collect(Collectors.toList());

            // Distribución por departamento
            List<Object[]> departmentData = repository.getRequestsByDepartment();
            List<DashboardMetricsDto.DepartmentMetricDto> departmentMetrics = departmentData.stream()
                .map(row -> DashboardMetricsDto.DepartmentMetricDto.builder()
                    .department((String) row[0])
                    .count((Long) row[1])
                    .percentage(calculatePercentage((Long) row[1], totalRequests))
                    .build())
                .collect(Collectors.toList());

            // Tareas pendientes por usuario
            List<Task> pendingTasks = taskService.createTaskQuery()
                .processDefinitionKey("purchase-request-process")
                .list();

            List<DashboardMetricsDto.UserTaskMetricDto> userTaskMetrics = pendingTasks.stream()
                .collect(Collectors.groupingBy(
                    task -> task.getAssignee() != null ? task.getAssignee() : "Sin asignar",
                    Collectors.counting()))
                .entrySet().stream()
                .map(entry -> DashboardMetricsDto.UserTaskMetricDto.builder()
                    .userId(entry.getKey())
                    .userName(entry.getKey())
                    .taskCount(entry.getValue())
                    .build())
                .collect(Collectors.toList());

            return DashboardMetricsDto.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .approvedRequests(approvedRequests)
                .rejectedRequests(rejectedRequests)
                .averageProcessingTime(averageProcessingTime)
                .overdueRequests(overdueRequests)
                .totalApprovedAmount(formattedAmount)
                .categoryDistribution(categoryMetrics)
                .departmentDistribution(departmentMetrics)
                .userTasks(userTaskMetrics)
                .build();

        } catch (Exception e) {
            log.error("❌ Error generando métricas del dashboard: {}", e.getMessage(), e);
            return DashboardMetricsDto.builder().build();
        }
    }

    public List<AuditReportDto> getAuditReport() {
        log.info("📋 Generando reporte de auditoría");

        try {
            List<HistoricProcessInstance> processInstances = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("purchase-request-process")
                .orderByProcessInstanceStartTime()
                .desc()
                .list();

            return processInstances.stream()
                .map(this::mapToAuditReport)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error generando reporte de auditoría: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private Double calculatePercentage(Long count, Long total) {
        if (total == 0) return 0.0;
        return BigDecimal.valueOf(count)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private AuditReportDto mapToAuditReport(HistoricProcessInstance processInstance) {
        Optional<PurchaseRequest> optionalRequest = repository.findByProcessInstanceId(processInstance.getId());
        PurchaseRequest request = optionalRequest.orElse(null);

        return AuditReportDto.builder()
            .processInstanceId(processInstance.getId())
            .businessKey(processInstance.getBusinessKey())
            .processDefinitionKey(processInstance.getProcessDefinitionKey())
            .startTime(processInstance.getStartTime().toString())
            .endTime(processInstance.getEndTime() != null ? processInstance.getEndTime().toString() : "En proceso")
            .state(processInstance.getState())
            .requesterName(request != null ? request.getRequesterName() : "N/A")
            .requesterEmail(request != null ? request.getRequesterEmail() : "N/A")
            .department(request != null ? request.getDepartment() : "N/A")
            .description(request != null ? request.getDescription() : "N/A")
            .totalAmount(request != null ? request.getFormattedAmount() : "N/A")
            .category(request != null ? request.getCategory().getDisplayName() : "N/A")
            .priority(request != null ? request.getPriority().getDisplayName() : "N/A")
            .approvalRoute(request != null ? request.getApprovalRoute() : "N/A")
            .approvedBy(request != null ? request.getApprovedBy() : "N/A")
            .approvedAt(request != null && request.getApprovedAt() != null ? request.getApprovedAt().toString() : "N/A")
            .rejectionReason(request != null ? request.getRejectionReason() : "N/A")
            .build();
    }

}
