package com.myidea.gym.service;

import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.model.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private CoachMapper coachMapper;

    @InjectMocks
    private RbacService rbacService;

    @Test
    void getPermissionCodesByUserId_shouldMapByRole() {
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setRole("ADMIN");
        when(sysUserMapper.selectById(1L)).thenReturn(admin);
        assertThat(rbacService.getPermissionCodesByUserId(1L)).containsExactly("admin:manage");

        SysUser coach = new SysUser();
        coach.setId(2L);
        coach.setRole("COACH");
        when(sysUserMapper.selectById(2L)).thenReturn(coach);
        assertThat(rbacService.getPermissionCodesByUserId(2L)).containsExactly("coach:booking:list");

        SysUser member = new SysUser();
        member.setId(3L);
        member.setRole("MEMBER");
        when(sysUserMapper.selectById(3L)).thenReturn(member);
        assertThat(rbacService.getPermissionCodesByUserId(3L)).containsExactly("member:booking:book", "member:recharge");
    }

    @Test
    void setUserRoles_shouldRejectMissingUser() {
        when(sysUserMapper.selectById(99L)).thenReturn(null);
        assertThatThrownBy(() -> rbacService.setUserRoles(99L, List.of("ADMIN")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
    }
}

