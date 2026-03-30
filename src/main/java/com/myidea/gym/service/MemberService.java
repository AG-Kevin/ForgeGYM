package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final SysUserMapper sysUserMapper;

    public List<Member> listAll() {
        return memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .orderByDesc(Member::getId));
    }

    @Transactional
    public Member create(Member member) {
        if (member.getName() == null || member.getName().isBlank()) {
            throw new IllegalArgumentException("会员名不能为空");
        }
        
        // Check if username (name) already exists in SysUser
        SysUser existingUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, member.getName()));
        if (existingUser != null) {
            throw new IllegalArgumentException("用户名/姓名已存在，请更换或直接关联账号");
        }

        if (member.getBalance() == null) {
            member.setBalance(BigDecimal.ZERO);
        }
        memberMapper.insert(member);
        
        // Also create a SysUser account
        SysUser user = new SysUser();
        user.setUsername(member.getName());
        // Default password for admin-created users
        user.setPasswordHash(PasswordUtil.hash(member.getName(), "123456"));
        user.setRole("MEMBER");
        user.setRefId(member.getId());
        sysUserMapper.insert(user);
        
        return member;
    }

    @Transactional
    public void delete(Long id) {
        // Also delete associated SysUser
        sysUserMapper.delete(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "MEMBER")
                .eq(SysUser::getRefId, id));
        memberMapper.deleteById(id);
    }

    public Member getById(Long id) {
        return memberMapper.selectById(id);
    }
}
