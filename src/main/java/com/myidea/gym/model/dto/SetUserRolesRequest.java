package com.myidea.gym.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SetUserRolesRequest {
    private List<String> roleCodes;
}
