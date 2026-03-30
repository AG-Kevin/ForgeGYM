package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachService {

    private final CoachMapper coachMapper;
    private final SysUserMapper sysUserMapper;
    private final CurrentUserService currentUserService;

    public List<Coach> listAll() {
        return coachMapper.selectList(new LambdaQueryWrapper<Coach>()
                .orderByDesc(Coach::getId));
    }

    @Transactional
    public Coach create(Coach coach) {
        if (coach.getName() == null || coach.getName().isBlank()) {
            throw new IllegalArgumentException("教练名不能为空");
        }
        
        // Check if username (name) already exists in SysUser
        SysUser existingUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, coach.getName()));
        if (existingUser != null) {
            throw new IllegalArgumentException("用户名/姓名已存在，请更换或直接关联账号");
        }

        coachMapper.insert(coach);
        
        // Also create a SysUser account for coach login
        SysUser user = new SysUser();
        user.setUsername(coach.getName());
        // Default password for admin-created users
        user.setPasswordHash(PasswordUtil.hash(coach.getName(), "123456"));
        user.setRole("COACH");
        user.setRefId(coach.getId());
        sysUserMapper.insert(user);
        
        return coach;
    }

    @Transactional
    public void delete(Long id) {
        // Also delete associated SysUser
        sysUserMapper.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "COACH")
                .eq(SysUser::getRefId, id));
        coachMapper.deleteById(id);
    }

    public Coach getById(Long id) {
        return coachMapper.selectById(id);
    }

    public Coach myProfile() {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole())) {
            throw new IllegalArgumentException("仅教练可访问");
        }
        Long coachId = user.getRefId();
        if (coachId == null) {
            throw new IllegalArgumentException("教练信息不完整");
        }
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new IllegalArgumentException("教练不存在");
        }
        return coach;
    }

    @Transactional
    public Coach updateProfile(String name, String phone, String intro, String tags, String avatar) {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole())) {
            throw new IllegalArgumentException("仅教练可修改资料");
        }
        Long coachId = user.getRefId();
        if (coachId == null) {
            throw new IllegalArgumentException("教练信息不完整");
        }
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new IllegalArgumentException("教练不存在");
        }

        if (name != null && !name.isBlank()) {
            coach.setName(name);
        }
        coach.setPhone(phone);
        coach.setIntro(intro);
        coach.setTags(tags);
        coach.setAvatar(avatar);
        coachMapper.updateById(coach);
        return coach;
    }

    public List<Coach> getAllCoaches() {
        return coachMapper.selectList(null);
    }
}
