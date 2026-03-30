package com.myidea.gym.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("store")
public class Store {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String region;
    private String address;
    private String businessHours;
    private String coverImage;
    private String contactPhone;
    private Integer capacity;
    private String status;
}
