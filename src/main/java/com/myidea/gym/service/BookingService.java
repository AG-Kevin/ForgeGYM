package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.dto.BookingMemberView;
import com.myidea.gym.model.dto.BookingScheduleView;
import com.myidea.gym.model.dto.CoachPerformanceView;
import com.myidea.gym.model.dto.CommissionDetailView;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.Store;
import com.myidea.gym.model.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String DEFAULT_REVIEW_CONTENT = "系统默认好评";

    private final BookingMapper bookingMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final StoreMapper storeMapper;
    private final CurrentUserService currentUserService;
    private final BookingCapacityService bookingCapacityService;
    private final MembershipService membershipService;

    public List<BookingScheduleView> myBookings() {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可查看预约");
        }
        List<Long> memberIds = resolveMemberIds(user);
        if (memberIds.isEmpty()) {
            return List.of();
        }
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .in(Booking::getMemberId, memberIds)
                .orderByDesc(Booking::getId));
        finalizeOverdueBookings(bookings);
        return bookings.stream().map(this::toBookingScheduleView).toList();
    }

/**
 * 预约课程的方法
 * @Transactional 注解确保整个方法在事务中执行，如果过程中出现异常，将会回滚
 * @param scheduleId 排课ID
 * @return Booking 预约成功后的预约信息
 * @throws IllegalArgumentException 当不符合预约条件时抛出异常
 */
    @Transactional
    public Booking book(Long scheduleId) {
    // 获取当前登录用户
        SysUser user = currentUserService.getLoginUser();
    // 检查用户角色是否为会员
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可预约");
        }
    // 获取会员ID
        Long memberId = user.getRefId();
        if (memberId == null) {
            throw new IllegalArgumentException("会员信息不完整");
        }
    // 确保会员是激活状态
        membershipService.assertActiveMember(memberId);

    // 根据排课ID查询排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("排课不存在");
        }
    // 根据课程ID查询课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("关联课程不存在");
        }
    // 获取课程价格，如果为null则设为0
        BigDecimal price = course.getPrice();
        if (price == null) price = BigDecimal.ZERO;

    // 检查排课是否已开始
        if (schedule.getStartTime() != null && !schedule.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("排课已开始，无法预约");
        }

        // Lock member and check balance
        Member member = memberMapper.selectByIdForUpdate(memberId);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        if (member.getBalance() == null || member.getBalance().compareTo(price) < 0) {
            throw new IllegalArgumentException("账户余额不足，请先充值（课程费用: ￥" + price + "）");
        }

        List<Long> memberIds = resolveMemberIds(user);
        Booking existing = bookingMapper.selectOne(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getScheduleId, scheduleId)
                .in(Booking::getMemberId, memberIds)
                .last("limit 1"));
        if (existing != null && ("COMPLETED".equals(existing.getStatus()) || "BOOKED".equals(existing.getStatus()))) {
            throw new IllegalArgumentException("不可重复预约");
        }

        long reserveRes = bookingCapacityService.reserve(scheduleId, memberId);
        if (reserveRes == -1) {
            throw new IllegalArgumentException("名额已满");
        }
        if (reserveRes == -2) {
            throw new IllegalArgumentException("不可重复预约");
        }
        if (reserveRes == -3) {
            throw new IllegalArgumentException("预约系统初始化中，请稍后重试");
        }
        if (reserveRes == -4) {
            throw new IllegalArgumentException("预约繁忙，请稍后重试");
        }

        // Deduct balance
        member.setBalance(member.getBalance().subtract(price));
        memberMapper.updateById(member);

        Booking booking = existing == null ? new Booking() : existing;
        booking.setScheduleId(scheduleId);
        booking.setMemberId(memberId);
        booking.setStatus("COMPLETED");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setAmount(price);
        booking.setAttendanceStatus("SCHEDULED");
        try {
            if (existing == null) {
                bookingMapper.insert(booking);
            } else {
                bookingMapper.updateById(booking);
            }
        } catch (RuntimeException e) {
            // Rollback capacity if DB insert/update fails
            bookingCapacityService.release(scheduleId, memberId);
            throw e;
        }
        return booking;
    }

    @Transactional
    public void cancel(Long bookingId) {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可取消预约");
        }
        Long memberId = user.getRefId();
        List<Long> memberIds = resolveMemberIds(user);
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return;
        }
        if (!memberIds.contains(booking.getMemberId())) {
            throw new IllegalArgumentException("不可取消他人预约");
        }
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("关联排课不存在");
        }
        if (!canMemberLeave(booking, schedule)) {
            throw new IllegalArgumentException("课前24小时内不可自主请假，请联系教练或管理员处理");
        }
        Course course = courseMapper.selectById(schedule.getCourseId());
        BigDecimal price = booking.getAmount() != null
                ? booking.getAmount()
                : (course != null && course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO);

        // Refund balance with lock
        Member member = memberMapper.selectByIdForUpdate(memberId);
        if (member != null) {
            java.math.BigDecimal current = member.getBalance() != null ? member.getBalance() : java.math.BigDecimal.ZERO;
            member.setBalance(current.add(price));
            memberMapper.updateById(member);
        }

        booking.setStatus("LEAVE");
        booking.setAttendanceStatus("LEAVE");
        bookingMapper.updateById(booking);
        bookingCapacityService.release(booking.getScheduleId(), booking.getMemberId());
    }

    public List<BookingMemberView> listBookingsForCoach(Long scheduleId) {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole())) {
            throw new IllegalArgumentException("仅教练可访问");
        }
        Long coachId = user.getRefId();
        if (coachId == null) {
            throw new IllegalArgumentException("教练信息不完整");
        }

        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("排课不存在");
        }
        if (!coachId.equals(schedule.getCoachId())) {
            throw new IllegalArgumentException("不可查看他人排课的预约名单");
        }

        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getScheduleId, scheduleId)
                .in(Booking::getStatus, List.of("COMPLETED", "BOOKED"))
                .orderByAsc(Booking::getCreatedAt));
        finalizeOverdueBookings(bookings);

        if (bookings.isEmpty()) {
            return List.of();
        }
        List<Long> memberIds = bookings.stream().map(Booking::getMemberId).distinct().toList();
        List<Member> members = memberMapper.selectBatchIds(memberIds);
        Map<Long, Member> memberMap = members.stream().collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));

        return bookings.stream().map(b -> toBookingMemberView(b, memberMap.get(b.getMemberId()), false)).toList();
    }

    public List<BookingMemberView> listStudentsForCoach() {
        Long coachId = requireCoachId();
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCoachId, coachId)
                .orderByDesc(CourseSchedule::getStartTime));
        if (schedules.isEmpty()) {
            return List.of();
        }
        Map<Long, CourseSchedule> scheduleMap = schedules.stream().collect(Collectors.toMap(CourseSchedule::getId, Function.identity(), (a, b) -> a));
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .in(Booking::getScheduleId, scheduleMap.keySet())
                .orderByDesc(Booking::getCreatedAt));
        finalizeOverdueBookings(bookings);
        List<Long> memberIds = bookings.stream().map(Booking::getMemberId).distinct().toList();
        Map<Long, Member> memberMap = memberMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));
        return bookings.stream()
                .map(item -> toBookingMemberView(item, memberMap.get(item.getMemberId()), false))
                .toList();
    }

    public CoachPerformanceView coachPerformance() {
        Long coachId = requireCoachId();
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCoachId, coachId)
                .orderByDesc(CourseSchedule::getStartTime));
        Map<Long, CourseSchedule> scheduleMap = schedules.stream().collect(Collectors.toMap(CourseSchedule::getId, Function.identity(), (a, b) -> a));
        List<Booking> bookings = scheduleMap.isEmpty()
                ? List.of()
                : bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .in(Booking::getScheduleId, scheduleMap.keySet())
                .orderByDesc(Booking::getCreatedAt));
        finalizeOverdueBookings(bookings);
        List<Member> members = memberMapper.selectList(new LambdaQueryWrapper<Member>().orderByAsc(Member::getId));
        Map<Long, Member> memberMap = members.stream().collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));

        List<Booking> effectiveBookings = bookings.stream().filter(this::isActiveBooking).toList();
        List<Booking> ratedBookings = effectiveBookings.stream().filter(item -> item.getRating() != null).toList();

        CoachPerformanceView view = new CoachPerformanceView();
        view.setScheduleCount((long) schedules.size());
        view.setTeachingCount((long) effectiveBookings.size());
        view.setAttendedCount(effectiveBookings.stream().filter(item -> "ATTENDED".equals(item.getAttendanceStatus())).count());
        view.setSatisfaction(ratedBookings.isEmpty()
                ? 0D
                : ratedBookings.stream().mapToInt(Booking::getRating).average().orElse(0D));
        view.setCommissionTotal(effectiveBookings.stream()
                .map(this::commissionFor)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        view.setCommissionDetails(effectiveBookings.stream().map(item -> {
            CourseSchedule schedule = scheduleMap.get(item.getScheduleId());
            Course course = schedule == null ? null : courseMapper.selectById(schedule.getCourseId());
            Store store = schedule == null ? null : storeMapper.selectById(schedule.getStoreId());
            Member member = memberMap.get(item.getMemberId());
            CommissionDetailView detail = new CommissionDetailView();
            detail.setBookingId(item.getId());
            detail.setMemberName(member == null ? null : member.getName());
            detail.setCourseName(course == null ? null : course.getName());
            detail.setStoreName(store == null ? null : store.getName());
            detail.setAttendanceStatus(item.getAttendanceStatus());
            detail.setRating(item.getRating());
            detail.setAmount(item.getAmount());
            detail.setCommissionAmount(commissionFor(item));
            detail.setStartTime(schedule == null ? null : schedule.getStartTime());
            return detail;
        }).toList());
        return view;
    }

    @Transactional
    public Booking markAttendance(Long bookingId, String attendanceStatus) {
        return markAttendanceInternal(bookingId, attendanceStatus, false);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void finalizeOverdueBookings() {
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .in(Booking::getStatus, List.of("COMPLETED", "BOOKED"))
                .orderByAsc(Booking::getId));
        finalizeOverdueBookings(bookings);
    }

    @Transactional
    public Booking adminMarkAttendance(Long bookingId, String attendanceStatus) {
        return markAttendanceInternal(bookingId, attendanceStatus, true);
    }

    public List<BookingMemberView> listBookingsForAdmin(boolean reviewedOnly) {
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                .orderByDesc(Booking::getCreatedAt));
        finalizeOverdueBookings(bookings);
        List<Long> memberIds = bookings.stream().map(Booking::getMemberId).distinct().toList();
        Map<Long, Member> memberMap = memberIds.isEmpty()
                ? Map.of()
                : memberMapper.selectBatchIds(memberIds).stream().collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));
        return bookings.stream()
                .filter(item -> !reviewedOnly || item.getRating() != null || (item.getReviewContent() != null && !item.getReviewContent().isBlank()))
                .map(item -> toBookingMemberView(item, memberMap.get(item.getMemberId()), true))
                .toList();
    }

    @Transactional
    Booking markAttendanceInternal(Long bookingId, String attendanceStatus, boolean adminOverride) {
        Long coachId = adminOverride ? null : requireCoachId();
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("预约不存在");
        }
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("关联排课不存在");
        }
        if (!adminOverride && !coachId.equals(schedule.getCoachId())) {
            throw new IllegalArgumentException("不可操作他人学员");
        }
        if (!List.of("ATTENDED", "ABSENT", "SCHEDULED").contains(attendanceStatus)) {
            throw new IllegalArgumentException("出勤状态不合法");
        }
        if (!adminOverride && !canCoachMarkAttendance(booking, schedule)) {
            throw new IllegalArgumentException("课后24小时后不可修改出勤，请联系管理员");
        }
        if (!adminOverride && schedule.getStartTime() != null && schedule.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("课程开始后才可登记出勤");
        }
        if (!adminOverride && "LEAVE".equals(booking.getStatus())) {
            throw new IllegalArgumentException("合规请假记录不可改为缺勤");
        }
        if (!adminOverride && "CANCELLED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("已取消预约不可修改出勤");
        }
        booking.setAttendanceStatus(attendanceStatus);
        bookingMapper.updateById(booking);
        return booking;
    }

    @Transactional
    public Booking review(Long bookingId, Integer rating, String reviewContent) {
        SysUser user = currentUserService.getLoginUser();
        if (!"MEMBER".equals(user.getRole())) {
            throw new IllegalArgumentException("仅会员可评价");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分范围为1-5");
        }
        List<Long> memberIds = resolveMemberIds(user);
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || !memberIds.contains(booking.getMemberId())) {
            throw new IllegalArgumentException("预约不存在");
        }
        finalizeOverdueBookings(List.of(booking));
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (!canMemberReview(booking, schedule)) {
            throw new IllegalArgumentException("仅上课结束后的有效出勤记录可评价");
        }
        booking.setRating(rating);
        booking.setReviewContent(reviewContent);
        booking.setReviewedAt(LocalDateTime.now());
        bookingMapper.updateById(booking);
        return booking;
    }

    private BookingScheduleView toBookingScheduleView(Booking booking) {
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        Course course = schedule == null ? null : courseMapper.selectById(schedule.getCourseId());
        Coach coach = schedule == null ? null : coachMapper.selectById(schedule.getCoachId());
        Store store = schedule == null ? null : storeMapper.selectById(schedule.getStoreId());

        BookingScheduleView view = new BookingScheduleView();
        view.setId(booking.getId());
        view.setScheduleId(booking.getScheduleId());
        view.setStatus(booking.getStatus());
        view.setStatusText(resolveStatusText(booking));
        view.setCreatedAt(booking.getCreatedAt());
        view.setAmount(booking.getAmount());
        view.setAttendanceStatus(booking.getAttendanceStatus());
        view.setRating(booking.getRating());
        view.setReviewContent(booking.getReviewContent());
        if (schedule != null) {
            view.setCourseId(schedule.getCourseId());
            view.setCoachId(schedule.getCoachId());
            view.setStoreId(schedule.getStoreId());
            view.setStartTime(schedule.getStartTime());
            view.setEndTime(schedule.getEndTime());
        }
        view.setCourseName(course == null ? null : course.getName());
        view.setCourseType(course == null ? null : course.getType());
        view.setPrice(course == null ? null : course.getPrice());
        view.setCoachName(coach == null ? null : coach.getName());
        view.setStoreName(store == null ? null : store.getName());
        view.setCanCancel(canMemberLeave(booking, schedule));
        view.setCanReview(canMemberReview(booking, schedule));
        return view;
    }

    private List<Long> resolveMemberIds(SysUser user) {
        java.util.LinkedHashSet<Long> memberIds = new java.util.LinkedHashSet<>();
        if (user.getRefId() != null) {
            memberIds.add(user.getRefId());
        }
        if (user.getId() != null) {
            memberIds.add(user.getId());
        }
        return List.copyOf(memberIds);
    }

    private BookingMemberView toBookingMemberView(Booking booking, Member member, boolean adminView) {
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        Course course = schedule == null ? null : courseMapper.selectById(schedule.getCourseId());
        Coach coach = schedule == null ? null : coachMapper.selectById(schedule.getCoachId());
        Store store = schedule == null ? null : storeMapper.selectById(schedule.getStoreId());
        BookingMemberView view = new BookingMemberView();
        view.setBookingId(booking.getId());
        view.setMemberId(booking.getMemberId());
        view.setMemberName(member == null ? null : member.getName());
        view.setPhone(member == null ? null : member.getPhone());
        view.setCreatedAt(booking.getCreatedAt());
        view.setStatus(booking.getStatus());
        view.setAttendanceStatus(booking.getAttendanceStatus());
        view.setRating(booking.getRating());
        view.setReviewContent(booking.getReviewContent());
        view.setAmount(booking.getAmount());
        view.setCourseName(course == null ? null : course.getName());
        view.setCoachName(coach == null ? null : coach.getName());
        view.setStoreName(store == null ? null : store.getName());
        view.setStartTime(schedule == null ? null : schedule.getStartTime());
        view.setEndTime(schedule == null ? null : schedule.getEndTime());
        view.setCanMarkAttendance(adminView || canCoachMarkAttendance(booking, schedule));
        return view;
    }

    private Long requireCoachId() {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole()) || user.getRefId() == null) {
            throw new IllegalArgumentException("仅教练可访问");
        }
        return user.getRefId();
    }

    private boolean isActiveBooking(Booking booking) {
        return List.of("COMPLETED", "BOOKED").contains(booking.getStatus());
    }

    private BigDecimal commissionFor(Booking booking) {
        BigDecimal amount = booking.getAmount() == null ? BigDecimal.ZERO : booking.getAmount();
        BigDecimal rate = "ATTENDED".equals(booking.getAttendanceStatus()) ? new BigDecimal("0.35") : new BigDecimal("0.2");
        return amount.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void finalizeOverdueBookings(List<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (!isActiveBooking(booking)) {
                continue;
            }
            CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
            if (schedule == null || schedule.getEndTime() == null || now.isBefore(schedule.getEndTime().plusHours(24))) {
                continue;
            }
            boolean changed = false;
            if (!List.of("ATTENDED", "ABSENT", "LEAVE", "CANCELLED").contains(booking.getAttendanceStatus())) {
                booking.setAttendanceStatus("ATTENDED");
                changed = true;
            }
            if (booking.getRating() == null) {
                booking.setRating(5);
                booking.setReviewContent(DEFAULT_REVIEW_CONTENT);
                booking.setReviewedAt(now);
                changed = true;
            }
            if (changed) {
                bookingMapper.updateById(booking);
            }
        }
    }

    private boolean canMemberLeave(Booking booking, CourseSchedule schedule) {
        if (!isActiveBooking(booking) || schedule == null || schedule.getStartTime() == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(schedule.getStartTime().minusHours(24));
    }

    private boolean canMemberReview(Booking booking, CourseSchedule schedule) {
        if (!isActiveBooking(booking) || schedule == null || schedule.getEndTime() == null) {
            return false;
        }
        if ("ABSENT".equals(booking.getAttendanceStatus())
                || "LEAVE".equals(booking.getAttendanceStatus())
                || "CANCELLED".equals(booking.getAttendanceStatus())) {
            return false;
        }
        return !LocalDateTime.now().isBefore(schedule.getEndTime());
    }

    private boolean canCoachMarkAttendance(Booking booking, CourseSchedule schedule) {
        if (booking == null || schedule == null || !isActiveBooking(booking)) {
            return false;
        }
        if (schedule.getEndTime() == null) {
            return false;
        }
        return !LocalDateTime.now().isAfter(schedule.getEndTime().plusHours(24));
    }

    private String resolveStatusText(Booking booking) {
        if ("LEAVE".equals(booking.getStatus()) || "LEAVE".equals(booking.getAttendanceStatus())) {
            return "已请假";
        }
        if ("CANCELLED".equals(booking.getStatus()) || "CANCELLED".equals(booking.getAttendanceStatus())) {
            return "已取消";
        }
        if ("ABSENT".equals(booking.getAttendanceStatus())) {
            return "已缺勤";
        }
        if ("ATTENDED".equals(booking.getAttendanceStatus())) {
            return "已出勤";
        }
        return "已预约";
    }
}
