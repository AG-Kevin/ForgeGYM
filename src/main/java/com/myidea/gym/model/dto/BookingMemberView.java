package com.myidea.gym.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingMemberView {
    private Long bookingId;
    private Long memberId;
    private String memberName;
    private String phone;
    private LocalDateTime createdAt;
    private String status;
    private String attendanceStatus;
    private Integer rating;
    private String reviewContent;
    private java.math.BigDecimal amount;
    private String courseName;
    private String coachName;
    private String storeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean canMarkAttendance;
}
