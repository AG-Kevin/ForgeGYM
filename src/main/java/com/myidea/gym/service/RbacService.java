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

/**
 * 根据用户ID获取用户的权限代码列表
 * @param userId 用户ID
 * @return 权限代码列表，如果用户不存在则返回空列表
 */
    public List<String> getPermissionCodesByUserId(Long userId) {
    // 根据用户ID查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
    // 如果用户不存在，返回空列表
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

/**
 * 设置用户角色的方法
 * 该方法使用@Transactional注解，确保事务性操作
 * @param userId 用户ID
 * @param roleCodes 角色代码列表
 */
    @Transactional
    public void setUserRoles(Long userId, List<String> roleCodes) {
    // 根据用户ID查询用户信息
        SysUser user = sysUserMapper.selectById(userId);
    // 如果用户不存在，抛出异常
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    // 检查角色代码列表是否不为空
        if (roleCodes != null && !roleCodes.isEmpty()) {
        // 获取用户原有的角色
            String oldRole = user.getRole();
        // 获取新的角色（取列表中的第一个角色）
            String newRole = roleCodes.get(0);
        // 如果新角色与旧角色不同，则更新用户角色
            if (!newRole.equals(oldRole)) {
            // 设置用户的新角色
                user.setRole(newRole);
            // 处理角色变更相关的业务逻辑
                handleProfileTransition(user, oldRole, newRole);
            // 更新用户信息到数据库
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

