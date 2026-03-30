package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.dto.AdminDashboardView;
import com.myidea.gym.model.dto.MetricPoint;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final StoreMapper storeMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final BookingMapper bookingMapper;

    public AdminDashboardView dashboard() {
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
                .orderByAsc(CourseSchedule::getStartTime));
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .orderByAsc(Booking::getCreatedAt));
        List<Member> members = memberMapper.selectList(new LambdaQueryWrapper<Member>().orderByAsc(Member::getId));
        List<Store> stores = storeMapper.selectList(new LambdaQueryWrapper<Store>().orderByAsc(Store::getId));

        AdminDashboardView view = new AdminDashboardView();
        view.setCourseCount(Math.toIntExact(courseMapper.selectCount(null)));
        view.setCoachCount(Math.toIntExact(coachMapper.selectCount(null)));
        view.setMemberCount(members.size());
        view.setStoreCount(stores.size());
        view.setScheduleCount(schedules.size());
        view.setTotalBookings(bookings.stream().filter(this::isEffectiveBooking).count());
        view.setTotalCapacity(schedules.stream().map(CourseSchedule::getCapacity).filter(java.util.Objects::nonNull).mapToLong(Integer::longValue).sum());
        view.setActiveMembers(bookings.stream().filter(this::isEffectiveBooking).map(Booking::getMemberId).distinct().count());
        view.setTotalRevenue(bookings.stream()
                .filter(this::isEffectiveBooking)
                .map(Booking::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        view.setBookingTrend(buildBookingTrend(bookings));
        view.setRevenueTrend(buildRevenueTrend(bookings));
        view.setActiveTrend(buildActiveTrend(bookings));
        view.setStoreLoad(buildStoreLoad(stores, schedules, bookings));
        return view;
    }

    private List<MetricPoint> buildBookingTrend(List<Booking> bookings) {
        List<MetricPoint> points = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            double count = bookings.stream()
                    .filter(this::isEffectiveBooking)
                    .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().equals(day))
                    .count();
            points.add(new MetricPoint(day.getMonthValue() + "/" + day.getDayOfMonth(), count, 0D));
        }
        return points;
    }

    private List<MetricPoint> buildRevenueTrend(List<Booking> bookings) {
        List<MetricPoint> points = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            double amount = bookings.stream()
                    .filter(this::isEffectiveBooking)
                    .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().equals(day))
                    .map(Booking::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue();
            points.add(new MetricPoint(day.getMonthValue() + "/" + day.getDayOfMonth(), amount, 0D));
        }
        return points;
    }

    private List<MetricPoint> buildActiveTrend(List<Booking> bookings) {
        List<MetricPoint> points = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            double count = bookings.stream()
                    .filter(this::isEffectiveBooking)
                    .filter(item -> item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().equals(day))
                    .map(Booking::getMemberId)
                    .distinct()
                    .count();
            points.add(new MetricPoint(day.getMonthValue() + "/" + day.getDayOfMonth(), count, 0D));
        }
        return points;
    }

    private List<MetricPoint> buildStoreLoad(List<Store> stores, List<CourseSchedule> schedules, List<Booking> bookings) {
        Map<Long, Long> bookedCount = bookings.stream()
                .filter(this::isEffectiveBooking)
                .collect(Collectors.groupingBy(Booking::getScheduleId, Collectors.counting()));
        return stores.stream().map(store -> {
            long capacity = schedules.stream()
                    .filter(item -> store.getId().equals(item.getStoreId()))
                    .filter(item -> item.getStartTime() != null && item.getStartTime().isAfter(LocalDateTime.now().minusDays(30)))
                    .map(CourseSchedule::getCapacity)
                    .filter(java.util.Objects::nonNull)
                    .mapToLong(Integer::longValue)
                    .sum();
            long booked = schedules.stream()
                    .filter(item -> store.getId().equals(item.getStoreId()))
                    .map(CourseSchedule::getId)
                    .mapToLong(id -> bookedCount.getOrDefault(id, 0L))
                    .sum();
            return new MetricPoint(store.getName(), (double) booked, (double) capacity);
        }).sorted(Comparator.comparing(MetricPoint::getLabel)).toList();
    }

    private boolean isEffectiveBooking(Booking booking) {
        return !"CANCELLED".equals(booking.getStatus()) && !"LEAVE".equals(booking.getStatus());
    }
}
