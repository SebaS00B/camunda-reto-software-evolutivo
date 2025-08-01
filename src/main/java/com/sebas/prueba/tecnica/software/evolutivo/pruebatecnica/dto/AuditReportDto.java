package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditReportDto {
    
    private String processInstanceId;
    private String businessKey;
    private String processDefinitionKey;
    private String startTime;
    private String endTime;
    private String duration;
    private String state;
    
    // Información del solicitante
    private String requesterName;
    private String requesterEmail;
    private String department;
    
    // Información de la solicitud
    private String description;
    private String totalAmount;
    private String currency;
    private String category;
    private String priority;
    
    // Información de aprobación
    private String approvalRoute;
    private String approvedBy;
    private String approvedAt;
    private String rejectionReason;
    
    // Actividades del proceso
    private List<ActivityDto> activities;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityDto {
        private String activityId;
        private String activityName;
        private String activityType;
        private String startTime;
        private String endTime;
        private String duration;
        private String assignee;
        private String state;
    }
}