package com.myidea.gym.service;

import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.dto.BookingScheduleView;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.Store;
import com.myidea.gym.model.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CourseScheduleMapper courseScheduleMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CoachMapper coachMapper;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private StoreMapper storeMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private BookingCapacityService bookingCapacityService;
    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void myBookings_shouldReturnLegacyUserIdBookings() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("MEMBER");
        user.setRefId(1L);
        when(currentUserService.getLoginUser()).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(11L);
        booking.setScheduleId(21L);
        booking.setMemberId(3L);
        booking.setStatus("BOOKED");
        booking.setCreatedAt(LocalDateTime.of(2026, 3, 28, 9, 0));
        when(bookingMapper.selectList(any())).thenReturn(List.of(booking));

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(21L);
        schedule.setCourseId(31L);
        schedule.setCoachId(41L);
        schedule.setStoreId(51L);
        schedule.setStartTime(LocalDateTime.of(2026, 3, 29, 10, 0));
        schedule.setEndTime(LocalDateTime.of(2026, 3, 29, 11, 0));
        when(courseScheduleMapper.selectById(21L)).thenReturn(schedule);

        Course course = new Course();
        course.setId(31L);
        course.setName("燃脂团课");
        course.setType("GROUP");
        course.setPrice(BigDecimal.valueOf(50));
        when(courseMapper.selectById(31L)).thenReturn(course);

        Coach coach = new Coach();
        coach.setId(41L);
        coach.setName("教练A");
        when(coachMapper.selectById(41L)).thenReturn(coach);

        Store store = new Store();
        store.setId(51L);
        store.setName("中心店");
        when(storeMapper.selectById(51L)).thenReturn(store);

        List<BookingScheduleView> result = bookingService.myBookings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(0).getStatusText()).isEqualTo("已预约");
    }

    @Test
    void cancel_shouldAcceptLegacyUserIdBooking() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("MEMBER");
        user.setRefId(1L);
        when(currentUserService.getLoginUser()).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(11L);
        booking.setScheduleId(21L);
        booking.setMemberId(3L);
        booking.setStatus("BOOKED");
        when(bookingMapper.selectById(11L)).thenReturn(booking);

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(21L);
        schedule.setCourseId(31L);
        schedule.setStartTime(LocalDateTime.now().plusDays(2));
        schedule.setEndTime(LocalDateTime.now().plusDays(2).plusHours(1));
        schedule.setStoreId(51L);
        when(courseScheduleMapper.selectById(21L)).thenReturn(schedule);

        Course course = new Course();
        course.setId(31L);
        course.setPrice(BigDecimal.valueOf(50));
        when(courseMapper.selectById(31L)).thenReturn(course);

        Member member = new Member();
        member.setId(1L);
        member.setBalance(BigDecimal.valueOf(100));
        when(memberMapper.selectByIdForUpdate(1L)).thenReturn(member);

        bookingService.cancel(11L);

        assertThat(member.getBalance()).isEqualByComparingTo("150");
        assertThat(booking.getStatus()).isEqualTo("LEAVE");
        verify(memberMapper).updateById(member);
        verify(bookingMapper).updateById(booking);
        verify(bookingCapacityService).release(21L, 3L);
    }

    @Test
    void cancel_shouldRejectLeaveInside24Hours() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("MEMBER");
        user.setRefId(1L);
        when(currentUserService.getLoginUser()).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(12L);
        booking.setScheduleId(22L);
        booking.setMemberId(1L);
        booking.setStatus("COMPLETED");
        when(bookingMapper.selectById(12L)).thenReturn(booking);

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(22L);
        schedule.setStartTime(LocalDateTime.now().plusHours(12));
        schedule.setEndTime(LocalDateTime.now().plusHours(13));
        when(courseScheduleMapper.selectById(22L)).thenReturn(schedule);

        assertThatThrownBy(() -> bookingService.cancel(12L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("课前24小时内不可自主请假");
    }

    @Test
    void myBookings_shouldAutoFinalizeAfter24Hours() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("MEMBER");
        user.setRefId(1L);
        when(currentUserService.getLoginUser()).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(13L);
        booking.setScheduleId(23L);
        booking.setMemberId(1L);
        booking.setStatus("COMPLETED");
        booking.setAttendanceStatus("SCHEDULED");
        booking.setCreatedAt(LocalDateTime.now().minusDays(2));
        when(bookingMapper.selectList(any())).thenReturn(List.of(booking));

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(23L);
        schedule.setCourseId(33L);
        schedule.setCoachId(43L);
        schedule.setStoreId(53L);
        schedule.setStartTime(LocalDateTime.now().minusDays(2));
        schedule.setEndTime(LocalDateTime.now().minusHours(30));
        when(courseScheduleMapper.selectById(23L)).thenReturn(schedule);

        Course course = new Course();
        course.setId(33L);
        course.setName("瑜伽课");
        when(courseMapper.selectById(33L)).thenReturn(course);

        Coach coach = new Coach();
        coach.setId(43L);
        coach.setName("教练B");
        when(coachMapper.selectById(43L)).thenReturn(coach);

        Store store = new Store();
        store.setId(53L);
        store.setName("公园店");
        when(storeMapper.selectById(53L)).thenReturn(store);

        List<BookingScheduleView> result = bookingService.myBookings();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAttendanceStatus()).isEqualTo("ATTENDED");
        assertThat(result.get(0).getRating()).isEqualTo(5);
        verify(bookingMapper).updateById(booking);
    }

    @Test
    void markAttendance_shouldRejectCoachUpdateAfter24Hours() {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setRole("COACH");
        user.setRefId(41L);
        when(currentUserService.getLoginUser()).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(14L);
        booking.setScheduleId(24L);
        booking.setStatus("COMPLETED");
        when(bookingMapper.selectById(14L)).thenReturn(booking);

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(24L);
        schedule.setCoachId(41L);
        schedule.setStartTime(LocalDateTime.now().minusDays(2));
        schedule.setEndTime(LocalDateTime.now().minusHours(30));
        when(courseScheduleMapper.selectById(24L)).thenReturn(schedule);

        assertThatThrownBy(() -> bookingService.markAttendance(14L, "ABSENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("课后24小时后不可修改出勤");
    }

    @Test
    void adminMarkAttendance_shouldAllowOverrideAfter24Hours() {
        Booking booking = new Booking();
        booking.setId(15L);
        booking.setScheduleId(25L);
        booking.setStatus("COMPLETED");
        when(bookingMapper.selectById(15L)).thenReturn(booking);

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(25L);
        schedule.setCoachId(45L);
        schedule.setStartTime(LocalDateTime.now().minusDays(2));
        schedule.setEndTime(LocalDateTime.now().minusHours(30));
        when(courseScheduleMapper.selectById(25L)).thenReturn(schedule);

        bookingService.adminMarkAttendance(15L, "ABSENT");

        assertThat(booking.getAttendanceStatus()).isEqualTo("ABSENT");
        verify(bookingMapper).updateById(booking);
    }
}
