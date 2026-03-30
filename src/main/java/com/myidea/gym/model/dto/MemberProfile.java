package com.myidea.gym.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MemberProfile {
    private Long memberId;
    private String name;
    private String phone;
    private LocalDate expireDate;
    private java.math.BigDecimal balance;
    private boolean active;
}
