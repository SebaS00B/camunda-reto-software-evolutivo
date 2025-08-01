package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.service;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto.DashboardMetricsDto;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository.PurchaseRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;

    /**
     * 📊 Obtiene métricas completas para el dashboard
     */
    public DashboardMetricsDto getDashboardMetrics() {
        log.info("📊 Generando métricas del dashboard");
        
        try {
            // Obtener todas las solicitudes
            List<PurchaseRequest> allRequests = purchaseRequestRepository.findAll();
            
            // Contadores básicos
            long totalRequests = allRequests.size();
            long pendingRequests = getPendingRequestsCount();
            long approvedRequests = getApprovedRequestsCount();
            long rejectedRequests = getRejectedRequestsCount();
            
            // Métricas de tiempo
            Double avgProcessingTime = calculateAverageProcessingTime();
            long overdueRequests = getOverdueRequestsCount();
            
            // Métricas financieras
            String totalApprovedAmount = calculateTotalApprovedAmount();
            String averageRequestAmount = calculateAverageRequestAmount();
            
            // Distribuciones
            List<DashboardMetricsDto.CategoryMetricDto> categoryDistribution = 
                getCategoryDistribution(allRequests);
            List<DashboardMetricsDto.DepartmentMetricDto> departmentDistribution = 
                getDepartmentDistribution(allRequests);
            List<DashboardMetricsDto.UserTaskMetricDto> userTasks = 
                getUserTaskMetrics();

            return DashboardMetricsDto.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .approvedRequests(approvedRequests)
                .rejectedRequests(rejectedRequests)
                .averageProcessingTime(avgProcessingTime)
                .overdueRequests(overdueRequests)
                .totalApprovedAmount(totalApprovedAmount)
                .averageRequestAmount(averageRequestAmount)
                .categoryDistribution(categoryDistribution)
                .departmentDistribution(departmentDistribution)
                .userTasks(userTasks)
                .build();

        } catch (Exception e) {
            log.error("❌ Error generando métricas del dashboard: {}", e.getMessage(), e);
            return getEmptyMetrics();
        }
    }

    /**
     * 📈 Obtiene el reporte de auditoría
     */
    public Map<String, Object> getAuditReport() {
        log.info("📈 Generando reporte de auditoría");
        
        try {
            // Obtener procesos históricos
            List<HistoricProcessInstance> historicProcesses = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("purchase-request-process")
                .list();

            // Estadísticas básicas
            long totalProcesses = historicProcesses.size();
            long completedProcesses = historicProcesses.stream()
                .mapToLong(p -> p.getEndTime() != null ? 1 : 0)
                .sum();
            long activeProcesses = totalProcesses - completedProcesses;

            // Crear el mapa con los tipos correctos
            Map<String, Object> auditData = Map.of(
                "totalProcesses", totalProcesses,
                "completedProcesses", completedProcesses,
                "activeProcesses", activeProcesses,
                "completionRate", totalProcesses > 0 ? (completedProcesses * 100.0 / totalProcesses) : 0.0,
                "historicProcesses", historicProcesses
            );
            
            return auditData;
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte de auditoría: {}", e.getMessage(), e);
            return Map.of(
                "totalProcesses", 0L,
                "completedProcesses", 0L,
                "activeProcesses", 0L,
                "completionRate", 0.0,
                "historicProcesses", List.of()
            );
        }
    }

    // ===================== MÉTODOS PRIVADOS =====================

    private long getPendingRequestsCount() {
        // Contar instancias de proceso activas
        List<ProcessInstance> activeInstances = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionKey("purchase-request-process")
            .active()
            .list();
        
        return activeInstances.size();
    }

    private long getApprovedRequestsCount() {
        // Buscar procesos históricos que terminaron con aprobación
        List<HistoricProcessInstance> finishedProcesses = historyService
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("purchase-request-process")
            .finished()
            .list();

        long approvedCount = 0;
        for (HistoricProcessInstance process : finishedProcesses) {
            // Buscar variable histórica finalStatus
            List<HistoricVariableInstance> variables = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId())
                .variableName("finalStatus")
                .list();
                
            for (HistoricVariableInstance variable : variables) {
                if ("APPROVED".equals(variable.getValue())) {
                    approvedCount++;
                    break;
                }
            }
        }
        
        return approvedCount;
    }

    private long getRejectedRequestsCount() {
        // Buscar procesos históricos que terminaron con rechazo
        List<HistoricProcessInstance> finishedProcesses = historyService
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("purchase-request-process")
            .finished()
            .list();

        long rejectedCount = 0;
        for (HistoricProcessInstance process : finishedProcesses) {
            // Buscar variable histórica finalStatus
            List<HistoricVariableInstance> variables = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId())
                .variableName("finalStatus")
                .list();
                
            for (HistoricVariableInstance variable : variables) {
                if ("REJECTED".equals(variable.getValue())) {
                    rejectedCount++;
                    break;
                }
            }
        }
        
        return rejectedCount;
    }

    private Double calculateAverageProcessingTime() {
        List<HistoricProcessInstance> completedProcesses = historyService
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("purchase-request-process")
            .finished()
            .list();

        if (completedProcesses.isEmpty()) {
            return 0.0;
        }

        double totalHours = completedProcesses.stream()
            .filter(p -> p.getStartTime() != null && p.getEndTime() != null)
            .mapToDouble(p -> {
                long durationMillis = p.getEndTime().getTime() - p.getStartTime().getTime();
                return durationMillis / (1000.0 * 60.0 * 60.0); // Convertir a horas
            })
            .sum();

        return totalHours / completedProcesses.size();
    }

    private long getOverdueRequestsCount() {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        
        List<ProcessInstance> activeInstances = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionKey("purchase-request-process")
            .active()
            .list();

        return activeInstances.stream()
            .filter(instance -> {
                List<HistoricProcessInstance> historic = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(instance.getId())
                    .list();
                
                if (!historic.isEmpty() && historic.get(0).getStartTime() != null) {
                    LocalDateTime startTime = historic.get(0).getStartTime()
                        .toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
                    return startTime.isBefore(cutoffDate);
                }
                return false;
            })
            .count();
    }

    private String calculateTotalApprovedAmount() {
        List<PurchaseRequest> approvedRequests = purchaseRequestRepository.findAll()
            .stream()
            .filter(r -> r.getProcessInstanceId() != null)
            .filter(this::isRequestApproved)
            .collect(Collectors.toList());

        BigDecimal total = approvedRequests.stream()
            .map(PurchaseRequest::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return String.format("$%,.2f", total);
    }

    private String calculateAverageRequestAmount() {
        List<PurchaseRequest> allRequests = purchaseRequestRepository.findAll();
        
        if (allRequests.isEmpty()) {
            return "$0.00";
        }

        BigDecimal total = allRequests.stream()
            .map(PurchaseRequest::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = total.divide(
            BigDecimal.valueOf(allRequests.size()), 
            2, 
            BigDecimal.ROUND_HALF_UP
        );

        return String.format("$%,.2f", average);
    }

    private boolean isRequestApproved(PurchaseRequest request) {
        try {
            if (request.getProcessInstanceId() == null) {
                return false;
            }
            
            // Buscar variables históricas del proceso
            List<HistoricVariableInstance> variables = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(request.getProcessInstanceId())
                .variableName("finalStatus")
                .list();

            return variables.stream()
                .anyMatch(var -> "APPROVED".equals(var.getValue()));
                
        } catch (Exception e) {
            log.warn("Error verificando estado de aprobación para request {}: {}", 
                request.getId(), e.getMessage());
            return false;
        }
    }

    private List<DashboardMetricsDto.CategoryMetricDto> getCategoryDistribution(
            List<PurchaseRequest> requests) {
        
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> categoryCount = requests.stream()
            .collect(Collectors.groupingBy(
                r -> r.getCategory().name(),
                Collectors.counting()
            ));

        long total = requests.size();

        return categoryCount.entrySet().stream()
            .map(entry -> DashboardMetricsDto.CategoryMetricDto.builder()
                .category(entry.getKey())
                .count(entry.getValue())
                .percentage((entry.getValue() * 100.0) / total)
                .build())
            .collect(Collectors.toList());
    }

    private List<DashboardMetricsDto.DepartmentMetricDto> getDepartmentDistribution(
            List<PurchaseRequest> requests) {
        
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> departmentCount = requests.stream()
            .collect(Collectors.groupingBy(
                PurchaseRequest::getDepartment,
                Collectors.counting()
            ));

        long total = requests.size();

        return departmentCount.entrySet().stream()
            .map(entry -> DashboardMetricsDto.DepartmentMetricDto.builder()
                .department(entry.getKey())
                .count(entry.getValue())
                .percentage((entry.getValue() * 100.0) / total)
                .build())
            .collect(Collectors.toList());
    }

    private List<DashboardMetricsDto.UserTaskMetricDto> getUserTaskMetrics() {
        List<Task> allTasks = taskService.createTaskQuery()
            .processDefinitionKey("purchase-request-process")
            .list();

        Map<String, Long> userTaskCount = allTasks.stream()
            .filter(task -> task.getAssignee() != null)
            .collect(Collectors.groupingBy(
                Task::getAssignee,
                Collectors.counting()
            ));

        return userTaskCount.entrySet().stream()
            .map(entry -> DashboardMetricsDto.UserTaskMetricDto.builder()
                .userId(entry.getKey())
                .userName(entry.getKey()) // En un caso real, buscarías el nombre completo
                .taskCount(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    private DashboardMetricsDto getEmptyMetrics() {
        return DashboardMetricsDto.builder()
            .totalRequests(0L)
            .pendingRequests(0L)
            .approvedRequests(0L)
            .rejectedRequests(0L)
            .averageProcessingTime(0.0)
            .overdueRequests(0L)
            .totalApprovedAmount("$0.00")
            .averageRequestAmount("$0.00")
            .categoryDistribution(new ArrayList<>())
            .departmentDistribution(new ArrayList<>())
            .userTasks(new ArrayList<>())
            .build();
    }
}