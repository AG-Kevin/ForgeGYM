package com.myidea.gym.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoachPerformanceView {
    private Long scheduleCount;
    private Long teachingCount;
    private Long attendedCount;
    private Double satisfaction;
    private BigDecimal commissionTotal;
    private List<CommissionDetailView> commissionDetails;
}
