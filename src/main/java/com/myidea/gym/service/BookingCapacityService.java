package com.myidea.gym.service;

public interface BookingCapacityService {
    void ensureInitialized(Long scheduleId);

/**
 * 预约方法
 * @param scheduleId 预约ID
 * @param memberId 会员ID
 * @return 预约结果，返回类型为long
 */
    long reserve(Long scheduleId, Long memberId);

    void release(Long scheduleId, Long memberId);
}
