package com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest;
import com.sebas.prueba.tecnica.software.evolutivo.pruebatecnica.entities.PurchaseRequest.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    
    // Búsquedas básicas
    Optional<PurchaseRequest> findByProcessInstanceId(String processInstanceId);
    Optional<PurchaseRequest> findByBusinessKey(String businessKey);
    List<PurchaseRequest> findByRequesterEmail(String requesterEmail);
    List<PurchaseRequest> findByStatus(RequestStatus status);
    List<PurchaseRequest> findByDepartment(String department);
    
    
    // Consultas para reportes y métricas
    @Query("SELECT COUNT(p) FROM PurchaseRequest p WHERE p.status = :status")
    Long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT p FROM PurchaseRequest p WHERE p.totalAmount > :amount")
    List<PurchaseRequest> findByAmountGreaterThan(@Param("amount") BigDecimal amount);
    
    @Query("SELECT p FROM PurchaseRequest p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PurchaseRequest> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM PurchaseRequest p WHERE p.dueDate < :currentDate AND p.status = 'PENDING'")
    List<PurchaseRequest> findOverdueRequests(@Param("currentDate") LocalDateTime currentDate);
    
    // Métricas para dashboard
    @Query("SELECT AVG(p.processingTimeHours) FROM PurchaseRequest p WHERE p.processingTimeHours IS NOT NULL")
    Double getAverageProcessingTime();
    
    @Query("SELECT SUM(p.totalAmount) FROM PurchaseRequest p WHERE p.status = 'APPROVED'")
    BigDecimal getTotalApprovedAmount();
    
    @Query("SELECT p.category, COUNT(p) FROM PurchaseRequest p GROUP BY p.category")
    List<Object[]> getRequestsByCategory();
    
    @Query("SELECT p.department, COUNT(p) FROM PurchaseRequest p GROUP BY p.department")
    List<Object[]> getRequestsByDepartment();
}

