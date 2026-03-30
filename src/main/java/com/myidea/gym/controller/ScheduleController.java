package com.myidea.gym.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.ScheduleView;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.service.CurrentUserService;
import com.myidea.gym.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程安排控制器
 * 提供课程安排相关的API接口
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    // 当前用户服务，用于获取当前登录用户信息
    private final CurrentUserService currentUserService;
    // 课程安排服务，用于处理课程安排相关的业务逻辑
    private final ScheduleService scheduleService;

    /**
     * 获取即将到来的课程安排列表
     * 根据用户角色返回不同的课程安排信息
     *
     * @return 返回即将到来的课程安排列表
     * @throws IllegalArgumentException 当会员信息不完整时抛出异常
     */
    @SaCheckLogin
    @GetMapping("/upcoming")
    public Result<List<ScheduleView>> upcoming(@RequestParam(value = "storeId", required = false) Long storeId) {
        // 获取当前登录用户
        SysUser user = currentUserService.getLoginUser();
        // 判断用户是否为会员角色
        if ("MEMBER".equals(user.getRole())) {
            // 检查会员ID是否存在
            if (user.getRefId() == null) {
                throw new IllegalArgumentException("会员信息不完整");
            }
            // 返回会员可预约的课程安排
            return Result.ok(scheduleService.listBookableForMember(user, storeId));
        }
        // 非会员用户返回所有即将到来的课程安排
        return Result.ok(scheduleService.listUpcoming(storeId));
    }
}
