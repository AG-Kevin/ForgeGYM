package com.myidea.gym.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RechargeRequest {
    @Min(0)
    @Max(3650)
    private Integer days;

    @Min(0)
    private java.math.BigDecimal amount;
}
