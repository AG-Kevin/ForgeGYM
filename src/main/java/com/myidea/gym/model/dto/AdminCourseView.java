package com.myidea.gym.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminCourseView {
    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private String type;
    private BigDecimal price;
    private String category;
    private String level;
    private Integer calories;
    private String coverImage;
    private String status;
    private String summary;
    private Boolean hasVideo;
    private String videoUrl;
    private String videoFileName;
}
