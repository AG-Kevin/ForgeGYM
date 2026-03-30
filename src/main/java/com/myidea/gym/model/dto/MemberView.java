package com.myidea.gym.model.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class MemberView {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private LocalDate expireDate;
    private BigDecimal balance;
}
