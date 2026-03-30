package com.myidea.gym.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricPoint {
    private String label;
    private Double value;
    private Double secondaryValue;
}
