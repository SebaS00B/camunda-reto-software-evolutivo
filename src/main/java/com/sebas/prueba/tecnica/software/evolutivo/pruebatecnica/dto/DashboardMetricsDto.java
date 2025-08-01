package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDto {
    
    // Contadores principales
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long rejectedRequests;
    
    // Métricas de tiempo
    private Double averageProcessingTime;
    private Long overdueRequests;
    
    // Métricas financieras
    private String totalApprovedAmount;
    private String averageRequestAmount;
    
    // Distribución por categoría
    private List<CategoryMetricDto> categoryDistribution;
    
    // Distribución por departamento
    private List<DepartmentMetricDto> departmentDistribution;
    
    // Tareas pendientes por usuario
    private List<UserTaskMetricDto> userTasks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryMetricDto {
        private String category;
        private Long count;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DepartmentMetricDto {
        private String department;
        private Long count;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserTaskMetricDto {
        private String userId;
        private String userName;
        private Long taskCount;
    }
}