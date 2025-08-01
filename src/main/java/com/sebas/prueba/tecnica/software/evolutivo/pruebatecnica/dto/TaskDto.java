package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    
    private String id;
    private String name;
    private String assignee;
    private String owner;
    private String processInstanceId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String created;
    private String due;
    private String followUp;
    private Integer priority;
    private String description;
    private String formKey;
    private String tenantId;
    
    // Variables del proceso
    private Object variables;
    
    // Información adicional para el dashboard
    private String requesterName;
    private String businessKey;
    private String requestDescription;
    private String totalAmount;
    private String currency;
    private String department;
    private String category;
    private String status;
    private Boolean overdue;
}