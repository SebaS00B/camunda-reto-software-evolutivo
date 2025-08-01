package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 📋 Entidad principal para las Solicitudes de Compra
 * 
 * Almacena toda la información del proceso de Purchase Request
 * incluyendo datos del negocio, estado del proceso y auditoría.
 */
@Entity
@Table(name = "purchase_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================================================================
    // INFORMACIÓN DEL PROCESO CAMUNDA
    // ================================================================
    @Column(name = "process_instance_id", unique = true)
    private String processInstanceId;

    @Column(name = "business_key", unique = true)
    private String businessKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    
    private RequestStatus status = RequestStatus.PENDING;

    // ================================================================
    // DATOS DEL SOLICITANTE
    // ================================================================
    @NotBlank(message = "El nombre del solicitante es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "requester_name", nullable = false)
    private String requesterName;

    @NotBlank(message = "El email del solicitante es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Column(name = "requester_email", nullable = false)
    private String requesterEmail;

    @NotBlank(message = "El departamento es obligatorio")
    @Column(name = "department", nullable = false)
    private String department;

    // ================================================================
    // INFORMACIÓN DE LA COMPRA
    // ================================================================
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "USD|EUR|COP|ECU", message = "Moneda debe ser USD, EUR, COP o ECU")
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PurchaseCategory category;

    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    // ================================================================
    // INFORMACIÓN DEL PROVEEDOR
    // ================================================================
    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Email(message = "Email del proveedor debe ser válido")
    @Column(name = "supplier_email")
    private String supplierEmail;

    @Column(name = "supplier_phone")
    private String supplierPhone;

    // ================================================================
    // INFORMACIÓN DE APROBACIÓN
    // ================================================================
    @Column(name = "approval_route")
    private String approvalRoute;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // ================================================================
    // FECHAS Y AUDITORÍA
    // ================================================================
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column(name = "due_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;

    // ================================================================
    // CAMPOS ADICIONALES PARA MÉTRICAS
    // ================================================================
    @Column(name = "processing_time_hours")
    private Integer processingTimeHours;

    @Column(name = "reminder_count")
    private Integer reminderCount = 0;

    @Column(name = "comments", length = 1000)
    private String comments;

    // ================================================================
    // ENUMS PARA CATEGORIZACIÓN
    // ================================================================
    public enum RequestStatus {
        PENDING("Pendiente"),
        IN_APPROVAL("En Aprobación"),
        APPROVED("Aprobado"),
        REJECTED("Rechazado"),
        CANCELLED("Cancelado");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PurchaseCategory {
        OFFICE_SUPPLIES("Suministros de Oficina"),
        IT_HARDWARE("Hardware IT"),
        SOFTWARE("Software"),
        EQUIPMENT("Equipamiento"),
        CONSULTING("Consultoría"),
        STRATEGIC("Estratégico"),
        MAINTENANCE("Mantenimiento"),
        OTHER("Otros");

        private final String displayName;

        PurchaseCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Priority {
        LOW("Baja"),
        NORMAL("Normal"),
        HIGH("Alta"),
        URGENT("Urgente");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ================================================================
    // MÉTODOS DE UTILIDAD
    // ================================================================
    
    /**
     * Genera la clave de negocio única
     */
    public void generateBusinessKey() {
        if (this.businessKey == null) {
            this.businessKey = "PR-" + System.currentTimeMillis();
        }
    }

    /**
     * Calcula el tiempo de procesamiento en horas
     */
    public void calculateProcessingTime() {
        if (this.createdAt != null && this.approvedAt != null) {
            this.processingTimeHours = (int) java.time.Duration.between(
                this.createdAt, this.approvedAt
            ).toHours();
        }
    }

    /**
     * Incrementa el contador de recordatorios
     */
    public void incrementReminderCount() {
        this.reminderCount = (this.reminderCount == null) ? 1 : this.reminderCount + 1;
    }

    /**
     * Verifica si la solicitud está vencida
     */
    public boolean isOverdue() {
        return this.dueDate != null && 
               LocalDateTime.now().isAfter(this.dueDate) && 
               this.status == RequestStatus.PENDING;
    }

    /**
     * Obtiene el monto formateado para mostrar
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", this.currency, this.totalAmount);
    }

    @Override
    public String toString() {
        return String.format("PurchaseRequest{id=%d, businessKey='%s', requesterName='%s', totalAmount=%s %s, status=%s}", 
                id, businessKey, requesterName, currency, totalAmount, status);
    }
}