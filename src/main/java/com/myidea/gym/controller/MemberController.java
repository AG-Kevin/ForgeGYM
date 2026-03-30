package com.myidea.gym.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.MemberProfile;
import com.myidea.gym.model.dto.ProfileUpdateRequest;
import com.myidea.gym.model.dto.RechargeRequest;
import com.myidea.gym.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员控制器
 * 处理会员相关的HTTP请求，包括会员信息查询和充值功能
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    // 注入会员服务
    private final MembershipService membershipService;

    /**
     * 获取当前登录会员的个人信息
     * @return 返回会员个人信息结果
     */
    @SaCheckLogin
    @GetMapping("/profile")
    public Result<MemberProfile> profile() {
        return Result.ok(membershipService.myProfile());
    }

    /**
     * 修改会员个人资料
     * @param req 个人资料更新请求参数
     * @return 返回更新后的会员个人信息结果
     */
    @SaCheckLogin
    @PostMapping("/profile")
    public Result<MemberProfile> updateProfile(@RequestBody ProfileUpdateRequest req) {
        return Result.ok(membershipService.updateProfile(req.getName(), req.getPhone()));
    }

    /**
     * 会员充值功能
     * @param req 充值请求参数，包含充值天数
     * @return 返回充值后的会员信息结果
     */
    @SaCheckLogin
    @SaCheckPermission("member:recharge")
    @PostMapping("/recharge")
    public Result<MemberProfile> recharge(@Valid @RequestBody RechargeRequest req) {
        return Result.ok(membershipService.recharge(req.getDays(), req.getAmount()));
    }
}
