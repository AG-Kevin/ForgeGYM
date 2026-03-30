package com.myidea.gym.model.dto;

import lombok.Data;

@Data
public class CoachView {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String intro;
    private String tags;
    private String avatar;
}
