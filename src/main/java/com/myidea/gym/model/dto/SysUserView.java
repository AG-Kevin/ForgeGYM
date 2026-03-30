package com.myidea.gym.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SysUserView {
    private Long id;
    private String username;
    private String displayName;
    private String primaryRole;
    private List<String> roles;
    private Long refId;
}
