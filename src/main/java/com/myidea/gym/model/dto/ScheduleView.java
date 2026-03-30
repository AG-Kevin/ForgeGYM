package com.myidea.gym.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleView {
    private Long id;
    private Long courseId;
    private String courseName;
    private String courseType;
    private java.math.BigDecimal price;
    private String courseSummary;
    private String courseVideoUrl;
    private Long coachId;
    private String coachName;
    private Long storeId;
    private String storeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private Long bookedCount;
}
