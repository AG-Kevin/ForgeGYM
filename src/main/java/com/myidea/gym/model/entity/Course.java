package com.myidea.gym.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private String type;
    private java.math.BigDecimal price;
    private String category;
    private String level;
    private Integer calories;
    private String coverImage;
    private String videoUrl;
    private String status;
    private String summary;
}
