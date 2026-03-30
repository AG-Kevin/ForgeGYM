package com.myidea.gym.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionDetailView {
    private Long bookingId;
    private String memberName;
    private String courseName;
    private String storeName;
    private String attendanceStatus;
    private Integer rating;
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private LocalDateTime startTime;
}
