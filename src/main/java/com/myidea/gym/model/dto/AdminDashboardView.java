package com.myidea.gym.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminDashboardView {
    private Integer courseCount;
    private Integer coachCount;
    private Integer memberCount;
    private Integer storeCount;
    private Integer scheduleCount;
    private Long totalBookings;
    private Long totalCapacity;
    private Long activeMembers;
    private BigDecimal totalRevenue;
    private List<MetricPoint> bookingTrend;
    private List<MetricPoint> revenueTrend;
    private List<MetricPoint> activeTrend;
    private List<MetricPoint> storeLoad;
}
