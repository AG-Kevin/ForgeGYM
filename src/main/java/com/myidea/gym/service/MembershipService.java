package com.myidea.gym.service;

import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.model.dto.MemberProfile;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final CurrentUserService currentUserService;
    private final MemberMapper memberMapper;

    public MemberProfile myProfile() {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可访问");
        }
        Long memberId = user.getRefId();
        if (memberId == null) {
            throw new IllegalArgumentException("会员信息不完整");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        boolean active = isActive(member);
        return new MemberProfile(member.getId(), member.getName(), member.getPhone(), member.getExpireDate(), member.getBalance(), active);
    }

    public boolean isActive(Member member) {
        if (member == null || member.getExpireDate() == null) {
            return false;
        }
        return !member.getExpireDate().isBefore(LocalDate.now());
    }

    public void assertActiveMember(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (!isActive(member)) {
            throw new IllegalArgumentException("会员已到期，请先办理按月付费");
        }
    }

    @Transactional
    public MemberProfile recharge(Integer days, BigDecimal amount) {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可充值");
        }
        Long memberId = user.getRefId();
        if (memberId == null) {
            throw new IllegalArgumentException("会员信息不完整");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        if (days != null && days > 0) {
            LocalDate today = LocalDate.now();
            LocalDate base = member.getExpireDate();
            if (base == null || base.isBefore(today)) {
                base = today;
            }
            member.setExpireDate(base.plusDays(days));
        }

        if (amount != null) {
            BigDecimal current = member.getBalance();
            if (current == null) current = BigDecimal.ZERO;
            member.setBalance(current.add(amount));
        }

        memberMapper.updateById(member);
        return new MemberProfile(member.getId(), member.getName(), member.getPhone(), member.getExpireDate(), member.getBalance(), isActive(member));
    }

    @Transactional
    public MemberProfile updateProfile(String name, String phone) {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可修改资料");
        }
        Long memberId = user.getRefId();
        if (memberId == null) {
            throw new IllegalArgumentException("会员信息不完整");
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }

        if (name != null && !name.isBlank()) {
            member.setName(name);
        }
        member.setPhone(phone);
        memberMapper.updateById(member);
        return new MemberProfile(member.getId(), member.getName(), member.getPhone(), member.getExpireDate(), member.getBalance(), isActive(member));
    }
}
