package com.myidea.gym.model.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String phone;
    private String intro;
    private String tags;
    private String avatar;
}
