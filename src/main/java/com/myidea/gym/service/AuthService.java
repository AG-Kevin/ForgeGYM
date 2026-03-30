package com.myidea.gym.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.model.dto.LoginRequest;
import com.myidea.gym.model.dto.LoginResponse;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.util.PasswordUtil;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.model.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final MemberMapper memberMapper;
    private final RbacService rbacService;

    @Transactional
    public void register(LoginRequest req) {
        // Check if user exists
        SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // Create Member
        Member member = new Member();
        member.setName(req.getUsername());
        member.setBalance(BigDecimal.ZERO);
        memberMapper.insert(member);

        // Create SysUser
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(PasswordUtil.hash(req.getUsername(), req.getPassword()));
        user.setRole("MEMBER");
        user.setRefId(member.getId());
        sysUserMapper.insert(user);

        // Set role in SysUser via RbacService
        rbacService.setUserRoles(user.getId(), Collections.singletonList("MEMBER"));
    }

    public LoginResponse login(LoginRequest req) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String hash = PasswordUtil.hash(user.getUsername(), req.getPassword());
        if (!hash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        SaLoginModel loginModel = new SaLoginModel();
        if (req.getDevice() != null && !req.getDevice().isBlank()) {
            loginModel.setDevice(req.getDevice());
        }
        StpUtil.login(user.getId(), loginModel);

        List<String> roles = rbacService.getRoleCodesByUserId(user.getId());
        String role = roles.isEmpty() ? user.getRole() : roles.get(0);
        return new LoginResponse(user.getId(), role, StpUtil.getTokenValue());
    }

    public void logout() {
        StpUtil.logout();
    }
}
