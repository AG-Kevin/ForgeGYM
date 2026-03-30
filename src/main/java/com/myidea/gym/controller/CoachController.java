package com.myidea.gym.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.BookingAttendanceRequest;
import com.myidea.gym.model.dto.BookingMemberView;
import com.myidea.gym.model.dto.CoachPerformanceView;
import com.myidea.gym.model.dto.ProfileUpdateRequest;
import com.myidea.gym.model.dto.ScheduleView;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.service.BookingService;
import com.myidea.gym.service.CoachService;
import com.myidea.gym.service.CourseService;
import com.myidea.gym.service.CurrentUserService;
import com.myidea.gym.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class CoachController {

    private final CurrentUserService currentUserService;
    private final ScheduleService scheduleService;
    private final BookingService bookingService;
    private final CoachService coachService;
    private final CourseService courseService;

    @SaCheckLogin
    @GetMapping("/profile")
    public Result<Coach> profile() {
        return Result.ok(coachService.myProfile());
    }

    @SaCheckLogin
    @PostMapping("/profile")
    public Result<Coach> updateProfile(@RequestBody ProfileUpdateRequest req) {
        return Result.ok(coachService.updateProfile(req.getName(), req.getPhone(), req.getIntro(), req.getTags(), req.getAvatar()));
    }

    @SaCheckLogin
    @GetMapping("/schedules/upcoming")
    public Result<List<ScheduleView>> myUpcomingSchedules() {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole())) {
            throw new IllegalArgumentException("仅教练可访问");
        }
        if (user.getRefId() == null) {
            throw new IllegalArgumentException("教练信息不完整");
        }
        return Result.ok(scheduleService.listUpcomingByCoach(user.getRefId()));
    }

    @SaCheckLogin
    @GetMapping("/courses")
    public Result<List<Course>> listCourses() {
        SysUser user = currentUserService.getLoginUser();
        if (!"COACH".equals(user.getRole())) {
            throw new IllegalArgumentException("仅教练可访问");
        }
        return Result.ok(courseService.listActive());
    }

    @SaCheckLogin
    @SaCheckPermission("coach:booking:list")
    @GetMapping("/schedules/{id}/bookings")
    public Result<List<BookingMemberView>> scheduleBookings(@PathVariable("id") Long id) {
        return Result.ok(bookingService.listBookingsForCoach(id));
    }

    @SaCheckLogin
    @GetMapping("/students")
    public Result<List<BookingMemberView>> students() {
        return Result.ok(bookingService.listStudentsForCoach());
    }

    @SaCheckLogin
    @GetMapping("/performance")
    public Result<CoachPerformanceView> performance() {
        return Result.ok(bookingService.coachPerformance());
    }

    @SaCheckLogin
    @PostMapping("/bookings/{id}/attendance")
    public Result<Booking> markAttendance(@PathVariable("id") Long id, @RequestBody BookingAttendanceRequest req) {
        return Result.ok(bookingService.markAttendance(id, req.getAttendanceStatus()));
    }

    @GetMapping("/{id}")
    public Result<Coach> getCoach(@PathVariable("id") Long id) {
        return Result.ok(coachService.getById(id));
    }

    @GetMapping("/all")
    public Result<List<Coach>> getAllCoaches() {
        return Result.ok(coachService.getAllCoaches());
    }
}
