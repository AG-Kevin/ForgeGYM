package com.myidea.gym.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {
    @NotNull
    private Long scheduleId;
}
