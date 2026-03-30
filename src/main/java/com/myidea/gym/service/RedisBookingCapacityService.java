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
    public void ensureInitialized(Long scheduleId) {
        String seatsKey = seatsKey(scheduleId);
        Boolean exists = stringRedisTemplate.hasKey(seatsKey);
        if (exists != null && exists) {
            return;
        }

        String lockKey = initLockKey(scheduleId);
        String lockToken = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, Duration.ofSeconds(5));
        if (locked == null || !locked) {
            return;
        }

        try {
            Boolean exists2 = stringRedisTemplate.hasKey(seatsKey);
            if (exists2 != null && exists2) {
                return;
            }

            CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                return;
            }

            List<Booking> booked = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getScheduleId, scheduleId)
                    .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")));

            int bookedCount = booked.size();
            int remaining = schedule.getCapacity() - bookedCount;
            if (remaining < 0) {
                remaining = 0;
            }

            stringRedisTemplate.opsForValue().set(seatsKey, String.valueOf(remaining));
            String membersKey = membersKey(scheduleId);
            if (!booked.isEmpty()) {
                String[] memberIds = booked.stream().map(b -> String.valueOf(b.getMemberId())).distinct().toArray(String[]::new);
                if (memberIds.length > 0) {
                    stringRedisTemplate.opsForSet().add(membersKey, memberIds);
                }
            }

            Duration ttl = ttlForSchedule(schedule.getEndTime());
            stringRedisTemplate.expire(seatsKey, ttl);
            stringRedisTemplate.expire(membersKey, ttl);
        } finally {
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
    public void release(Long scheduleId, Long memberId) {
        String seatsKey = seatsKey(scheduleId);
        String membersKey = membersKey(scheduleId);
        stringRedisTemplate.opsForSet().remove(membersKey, String.valueOf(memberId));
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
