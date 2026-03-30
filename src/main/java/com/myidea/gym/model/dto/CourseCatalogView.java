package com.myidea.gym.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseCatalogView {
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
    private String videoUrl;
    private String summary;
    private List<String> coachNames;
    private List<String> storeNames;
    private Integer upcomingCount;
}
