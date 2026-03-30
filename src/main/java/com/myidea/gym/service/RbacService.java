package com.myidea.gym.service;

import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysUserMapper sysUserMapper;
    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;

    public List<String> getRoleCodesByUserId(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }
        return List.of(user.getRole());
    }

    public List<String> getPermissionCodesByUserId(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        // Simple role-based permission mapping
        return switch (user.getRole()) {
            case "ADMIN" -> List.of("admin:manage");
            case "COACH" -> List.of("coach:booking:list");
            case "MEMBER" -> List.of("member:booking:book", "member:recharge");
            default -> Collections.emptyList();
        };
    }

    @Transactional
    public void setUserRoles(Long userId, List<String> roleCodes) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (roleCodes != null && !roleCodes.isEmpty()) {
            String oldRole = user.getRole();
            String newRole = roleCodes.get(0);
            if (!newRole.equals(oldRole)) {
                user.setRole(newRole);
                handleProfileTransition(user, oldRole, newRole);
                sysUserMapper.updateById(user);
            }
        }
    }

    private void handleProfileTransition(SysUser user, String oldRole, String newRole) {
        // From MEMBER to COACH
        if ("MEMBER".equals(oldRole) && "COACH".equals(newRole)) {
            Coach coach = new Coach();
            coach.setName(user.getUsername());
            coachMapper.insert(coach);
            user.setRefId(coach.getId());
        } 
        // From COACH to MEMBER
        else if ("COACH".equals(oldRole) && "MEMBER".equals(newRole)) {
            Member member = new Member();
            member.setName(user.getUsername());
            member.setBalance(BigDecimal.ZERO);
            memberMapper.insert(member);
            user.setRefId(member.getId());
        }
        // If transitioning from/to ADMIN, we might want to clear refId or handle differently
        else if ("ADMIN".equals(newRole)) {
            user.setRefId(null);
        }
    }
}

