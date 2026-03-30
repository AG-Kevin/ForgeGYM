package com.myidea.gym.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingScheduleView {
    private Long id;
    private Long scheduleId;
    private String status;
    private String statusText;
    private LocalDateTime createdAt;
    private java.math.BigDecimal amount;
    private String attendanceStatus;
    private Integer rating;
    private String reviewContent;
    private Boolean canCancel;
    private Boolean canReview;
    private Long courseId;
    private String courseName;
    private String courseType;
    private java.math.BigDecimal price;
    private Long coachId;
    private String coachName;
    private Long storeId;
    private String storeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
