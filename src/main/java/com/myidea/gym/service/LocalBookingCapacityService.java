package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.lock.LockService;
import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.CourseSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 本地环境下的课程预订容量服务实现类
 * 使用@Profile("local")注解，表明该服务仅在local环境下生效
 * 使用@RequiredArgsConstructor注解，自动生成包含final字段的构造函数
 */
@Service
@Profile("local")
@RequiredArgsConstructor
public class LocalBookingCapacityService implements BookingCapacityService {

    /**
     * 分布式锁服务，用于处理并发场景下的资源锁定
     */
    private final LockService lockService;
    /**
     * 课程表数据访问层，用于操作课程表数据
     */
    private final CourseScheduleMapper courseScheduleMapper;
    /**
     * 预订记录数据访问层，用于操作预订记录数据
     */
    private final BookingMapper bookingMapper;

    /**
     * 确保预订容量已初始化
     * @param scheduleId 课程表ID
     */
    @Override
    public void ensureInitialized(Long scheduleId) {
    }

    /**
     * 预订课程容量
     * @param scheduleId 课程表ID
     * @param memberId 会员ID
     * @return 返回剩余可预订数量，若返回负数表示预订失败：
     *         -1: 容量已满
     *         -2: 该会员已预订过该课程
     *         -3: 课程不存在
     *         -4: 获取锁失败
     */
    @Override
    public long reserve(Long scheduleId, Long memberId) {
        // 构造锁的key
        String lockKey = "lock:schedule:" + scheduleId;
        // 尝试获取锁，最多等待3000毫秒
        String token = lockService.tryLock(lockKey, 3000);
        if (token == null) {
            return -4;
        }
        try {
            // 查询课程表信息
            CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                return -3;
            }
            // 检查该会员是否已预订过该课程
            Long exists = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getScheduleId, scheduleId)
                    .eq(Booking::getMemberId, memberId)
                    .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")));
            if (exists != null && exists > 0) {
                return -2;
            }
            // 查询已预订数量
            Long bookedCount = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getScheduleId, scheduleId)
                    .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")));
            // 检查是否已达到容量上限
            if (bookedCount >= schedule.getCapacity()) {
                return -1;
            }
            // 返回剩余可预订数量（减1是因为当前预订操作）
            return schedule.getCapacity() - bookedCount - 1;
        } finally {
            // 释放锁
            lockService.unlock(lockKey, token);
        }
    }

    /**
     * 释放课程预订容量
     * @param scheduleId 课程表ID
     * @param memberId 会员ID
     */
    @Override
    public void release(Long scheduleId, Long memberId) {
    }
}

