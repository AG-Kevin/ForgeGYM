package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.CourseSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!local")
@RequiredArgsConstructor
public class RedisBookingCapacityService implements BookingCapacityService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> bookingReserveScript;
    private final CourseScheduleMapper courseScheduleMapper;
    private final BookingMapper bookingMapper;

    private String seatsKey(Long scheduleId) {
        return "gym:schedule:" + scheduleId + ":seats";
    }

    private String membersKey(Long scheduleId) {
        return "gym:schedule:" + scheduleId + ":members";
    }

    private String initLockKey(Long scheduleId) {
        return "gym:schedule:" + scheduleId + ":initLock";
    }

    @Override
    /**
     * 确保指定课程安排的初始化状态
     * 检查并初始化座位信息和已预订成员信息到Redis缓存中
     * @param scheduleId 课程安排ID
     */
    public void ensureInitialized(Long scheduleId) {
        // 构建座位信息的Redis键
        String seatsKey = seatsKey(scheduleId);
        // 检查座位信息是否已存在于Redis中
        Boolean exists = stringRedisTemplate.hasKey(seatsKey);
        // 如果已存在，直接返回
        if (exists != null && exists) {
            return;
        }

        // 构建初始化锁的Redis键
        String lockKey = initLockKey(scheduleId);
        // 生成锁的唯一标识
        String lockToken = UUID.randomUUID().toString();
        // 尝试获取锁，设置5秒超时
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, Duration.ofSeconds(5));
        // 如果获取锁失败，直接返回
        if (locked == null || !locked) {
            return;
        }

        try {
            // 再次检查座位信息是否已存在（防止在获取锁的过程中已被其他线程初始化）
            Boolean exists2 = stringRedisTemplate.hasKey(seatsKey);
            if (exists2 != null && exists2) {
                return;
            }

            // 从数据库查询课程安排信息
            CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
            // 如果课程安排不存在，直接返回
            if (schedule == null) {
                return;
            }

            // 查询已完成的预订记录
            List<Booking> booked = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getScheduleId, scheduleId)
                    .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")));

            // 计算已预订数量
            int bookedCount = booked.size();
            // 计算剩余座位数
            int remaining = schedule.getCapacity() - bookedCount;
            // 确保剩余座位数不为负数
            if (remaining < 0) {
                remaining = 0;
            }

            // 将剩余座位数存入Redis
            stringRedisTemplate.opsForValue().set(seatsKey, String.valueOf(remaining));
            // 构建已预订成员的Redis键
            String membersKey = membersKey(scheduleId);
            // 如果有预订记录，将成员ID存入Redis集合
            if (!booked.isEmpty()) {
                String[] memberIds = booked.stream().map(b -> String.valueOf(b.getMemberId())).distinct().toArray(String[]::new);
                if (memberIds.length > 0) {
                    stringRedisTemplate.opsForSet().add(membersKey, memberIds);
                }
            }

            // 计算课程结束时间的TTL
            Duration ttl = ttlForSchedule(schedule.getEndTime());
            // 设置座位信息和成员信息的过期时间
            stringRedisTemplate.expire(seatsKey, ttl);
            stringRedisTemplate.expire(membersKey, ttl);
        } finally {
            // 释放锁
            String v = stringRedisTemplate.opsForValue().get(lockKey);
            if (lockToken.equals(v)) {
                stringRedisTemplate.delete(lockKey);
            }
        }
    }

/**
 * 预约座位的方法
 * @param scheduleId 调度ID，用于标识特定的场次
 * @param memberId 会员ID，用于标识要预约的会员
 * @return 返回预约结果状态码：
 *         -3 表示需要重新初始化后再次尝试
 *         -4 表示预约失败（Redis执行返回null）
 *         其他数值表示具体的预约结果
 */
    @Override
    public long reserve(Long scheduleId, Long memberId) {
        // 创建Redis操作的键列表，包含座位键和会员键
        List<String> keys = List.of(seatsKey(scheduleId), membersKey(scheduleId));
        // 确保指定场次的初始化已完成
        ensureInitialized(scheduleId);
        // 执行Redis预约脚本，传入键和会员ID
        Long res = stringRedisTemplate.execute(bookingReserveScript, keys, String.valueOf(memberId));
        // 如果返回值为-3（表示需要重新初始化），则重新初始化并再次执行预约脚本
        if (res != null && res == -3) {
            ensureInitialized(scheduleId);
            res = stringRedisTemplate.execute(bookingReserveScript, keys, String.valueOf(memberId));
        }
        // 返回预约结果，如果Redis执行返回null则返回-4表示失败
        return res == null ? -4 : res;
    }

    @Override
    /**
     * 释放座位方法
     * @param scheduleId 调度ID，用于标识特定的场次安排
     * @param memberId 会员ID，用于标识要释放座位的会员
     */
    public void release(Long scheduleId, Long memberId) {
        // 根据调度ID生成座位键
        String seatsKey = seatsKey(scheduleId);
        // 根据调度ID生成会员键
        String membersKey = membersKey(scheduleId);
        // 从会员集合中移除指定会员，表示该会员不再占用座位
        stringRedisTemplate.opsForSet().remove(membersKey, String.valueOf(memberId));
        // 增加可用座位数量
        stringRedisTemplate.opsForValue().increment(seatsKey);
    }

    private Duration ttlForSchedule(LocalDateTime endTime) {
        if (endTime == null) {
            return Duration.ofDays(7);
        }
        Duration d = Duration.between(LocalDateTime.now(), endTime.plusDays(1));
        if (d.isNegative() || d.isZero()) {
            return Duration.ofDays(1);
        }
        if (d.toDays() > 30) {
            return Duration.ofDays(30);
        }
        return d;
    }
}
