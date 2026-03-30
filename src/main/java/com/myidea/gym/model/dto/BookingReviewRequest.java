package com.myidea.gym.model.dto;

import lombok.Data;

@Data
public class BookingReviewRequest {
    private Integer rating;
    private String reviewContent;
}
