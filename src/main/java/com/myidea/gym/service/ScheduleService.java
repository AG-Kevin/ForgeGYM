package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final BookingMapper bookingMapper;
    private final StoreMapper storeMapper;

    public List<ScheduleView> listUpcoming() {
        return listUpcoming(null);
    }

    public List<ScheduleView> listUpcoming(Long storeId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<CourseSchedule>()
                .ge(CourseSchedule::getStartTime, now)
                .orderByAsc(CourseSchedule::getStartTime);
        if (storeId != null) {
            wrapper.eq(CourseSchedule::getStoreId, storeId);
        }
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(wrapper);
        return schedules.stream()
                .map(this::toView)
                .filter(view -> view.getCourseName() != null && view.getCoachName() != null && view.getStoreName() != null)
                .toList();
    }

    public List<ScheduleView> listUpcomingByCoach(Long coachId) {
        LocalDateTime now = LocalDateTime.now();
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCoachId, coachId)
                .ge(CourseSchedule::getStartTime, now)
                .orderByAsc(CourseSchedule::getStartTime));
        return schedules.stream()
                .map(this::toView)
                .filter(view -> view.getCourseName() != null && view.getCoachName() != null)
                .toList();
    }

    public CourseSchedule create(CourseSchedule schedule) {
        validateSchedule(schedule);
        courseScheduleMapper.insert(schedule);
        return schedule;
    }

    public CourseSchedule update(CourseSchedule schedule) {
        if (schedule.getId() == null || courseScheduleMapper.selectById(schedule.getId()) == null) {
            throw new IllegalArgumentException("排课不存在");
        }
        validateSchedule(schedule);
        courseScheduleMapper.updateById(schedule);
        return schedule;
    }

    public CourseSchedule createByCoach(Long coachId, CourseSchedule schedule) {
        schedule.setCoachId(coachId);
        validateSchedule(schedule);
        courseScheduleMapper.insert(schedule);
        return schedule;
    }

    public List<ScheduleView> listBookableForMember(SysUser user, Long storeId) {
        List<Long> memberIds = resolveMemberIds(user);
        if (memberIds.isEmpty()) {
            return listUpcoming(storeId);
        }
        Set<Long> bookedScheduleIds = new HashSet<>(bookingMapper.selectList(new LambdaQueryWrapper<Booking>()
                        .in(Booking::getMemberId, memberIds)
                        .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")))
                .stream()
                .map(Booking::getScheduleId)
                .toList());

        return listUpcoming(storeId).stream()
                .filter(view -> !bookedScheduleIds.contains(view.getId()))
                .filter(view -> view.getBookedCount() < view.getCapacity())
                .toList();
    }

    public void delete(Long id) {
        courseScheduleMapper.deleteById(id);
    }

    private void validateTimeSlot(LocalDateTime start, LocalDateTime end) {
        int startHour = start.getHour();
        int startMin = start.getMinute();
        int endHour = end.getHour();
        int endMin = end.getMinute();

        // 规范时间段检查：
        // 8-10, 10-12, 14-16, 16-18, 19-21
        boolean valid = false;
        if (startMin == 0 && endMin == 0) {
            if (startHour == 8 && endHour == 10) valid = true;
            else if (startHour == 10 && endHour == 12) valid = true;
            else if (startHour == 14 && endHour == 16) valid = true;
            else if (startHour == 16 && endHour == 18) valid = true;
            else if (startHour == 19 && endHour == 21) valid = true;
        }

        if (!valid) {
            throw new IllegalArgumentException("排课时间必须符合标准时段：08-10, 10-12, 14-16, 16-18, 19-21");
        }
    }

    private void validateSchedule(CourseSchedule schedule) {
        if (schedule.getCourseId() == null) {
            throw new IllegalArgumentException("课程不能为空");
        }
        if (schedule.getCoachId() == null) {
            throw new IllegalArgumentException("教练不能为空");
        }
        if (schedule.getStoreId() == null) {
            throw new IllegalArgumentException("门店不能为空");
        }
        if (courseMapper.selectById(schedule.getCourseId()) == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (coachMapper.selectById(schedule.getCoachId()) == null) {
            throw new IllegalArgumentException("教练不存在");
        }
        if (storeMapper.selectById(schedule.getStoreId()) == null) {
            throw new IllegalArgumentException("门店不存在");
        }
        if (schedule.getCapacity() == null || schedule.getCapacity() <= 0) {
            throw new IllegalArgumentException("容量必须大于0");
        }
        if (schedule.getStartTime() == null || schedule.getEndTime() == null || !schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new IllegalArgumentException("开始/结束时间不合法");
        }
        validateTimeSlot(schedule.getStartTime(), schedule.getEndTime());

        if (!schedule.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("排课开始时间必须晚于当前时间");
        }

        LambdaQueryWrapper<CourseSchedule> conflictWrapper = new LambdaQueryWrapper<CourseSchedule>()
                .eq(CourseSchedule::getCoachId, schedule.getCoachId())
                .lt(CourseSchedule::getStartTime, schedule.getEndTime())
                .gt(CourseSchedule::getEndTime, schedule.getStartTime());
        if (schedule.getId() != null) {
            conflictWrapper.ne(CourseSchedule::getId, schedule.getId());
        }
        Long conflictCount = courseScheduleMapper.selectCount(conflictWrapper);
        if (conflictCount != null && conflictCount > 0) {
            throw new IllegalArgumentException("该教练在所选时间段已有排课");
        }
    }

    private ScheduleView toView(CourseSchedule s) {
        Course course = courseMapper.selectById(s.getCourseId());
        Coach coach = coachMapper.selectById(s.getCoachId());
        Store store = storeMapper.selectById(s.getStoreId());
        Long bookedCount = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getScheduleId, s.getId())
                .in(Booking::getStatus, List.of("COMPLETED", "BOOKED")));

        ScheduleView view = new ScheduleView();
        view.setId(s.getId());
        view.setCourseId(s.getCourseId());
        view.setCourseName(course == null ? null : course.getName());
        view.setCourseType(course == null ? null : course.getType());
        view.setPrice(course == null ? null : course.getPrice());
        view.setCourseSummary(course == null ? null : course.getSummary());
        
        if (course != null) {
            java.io.File file = new java.io.File("uploads/videos/" + course.getId() + ".mp4");
            if (file.exists()) {
                view.setCourseVideoUrl("http://localhost:8080/api/courses/" + course.getId() + "/video");
            } else {
                view.setCourseVideoUrl(null);
            }
        } else {
            view.setCourseVideoUrl(null);
        }
        
        view.setCoachId(s.getCoachId());
        view.setCoachName(coach == null ? null : coach.getName());
        view.setStoreId(s.getStoreId());
        view.setStoreName(store == null ? null : store.getName());
        view.setStartTime(s.getStartTime());
        view.setEndTime(s.getEndTime());
        view.setCapacity(s.getCapacity());
        view.setBookedCount(bookedCount);
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
}
