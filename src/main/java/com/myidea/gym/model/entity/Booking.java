package com.myidea.gym.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scheduleId;
    private Long memberId;
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal amount;
    private String attendanceStatus;
    private Integer rating;
    private String reviewContent;
    private LocalDateTime reviewedAt;
}
