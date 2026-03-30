package com.myidea.gym.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("coach")
public class Coach {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String intro;
    private String tags;
    private String avatar;
}
