package com.myidea.gym.service;

import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.dto.ScheduleView;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Store;
import com.myidea.gym.model.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private CourseScheduleMapper courseScheduleMapper;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CoachMapper coachMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private StoreMapper storeMapper;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void listBookableForMember_shouldFilterLegacyUserIdBookings() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("MEMBER");
        user.setRefId(1L);

        Booking booking = new Booking();
        booking.setScheduleId(10L);
        booking.setMemberId(3L);
        booking.setStatus("BOOKED");
        when(bookingMapper.selectList(any())).thenReturn(List.of(booking));

        CourseSchedule schedule = new CourseSchedule();
        schedule.setId(10L);
        schedule.setCourseId(20L);
        schedule.setCoachId(30L);
        schedule.setStoreId(40L);
        schedule.setCapacity(10);
        schedule.setStartTime(LocalDateTime.now().plusDays(1));
        schedule.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        when(courseScheduleMapper.selectList(any())).thenReturn(List.of(schedule));

        Course course = new Course();
        course.setId(20L);
        course.setName("燃脂团课");
        course.setType("GROUP");
        course.setPrice(BigDecimal.valueOf(50));
        when(courseMapper.selectById(20L)).thenReturn(course);

        Coach coach = new Coach();
        coach.setId(30L);
        coach.setName("教练A");
        when(coachMapper.selectById(30L)).thenReturn(coach);

        Store store = new Store();
        store.setId(40L);
        store.setName("中心店");
        when(storeMapper.selectById(40L)).thenReturn(store);

        when(bookingMapper.selectCount(any())).thenReturn(1L);

        List<ScheduleView> result = scheduleService.listBookableForMember(user, null);

        assertThat(result).isEmpty();
    }
}
