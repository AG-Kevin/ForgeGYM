package com.myidea.gym.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.AdminCourseView;
import com.myidea.gym.model.dto.AdminDashboardView;
import com.myidea.gym.model.dto.BookingAttendanceRequest;
import com.myidea.gym.model.dto.BookingMemberView;
import com.myidea.gym.model.dto.CoachView;
import com.myidea.gym.model.dto.MemberView;
import com.myidea.gym.model.dto.ScheduleView;
import com.myidea.gym.model.dto.SetUserRolesRequest;
import com.myidea.gym.model.dto.SysUserView;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Member;
import com.myidea.gym.model.entity.Store;
import com.myidea.gym.model.entity.SysUser;
import com.myidea.gym.mapper.SysUserMapper;
import com.myidea.gym.service.AdminAnalyticsService;
import com.myidea.gym.service.BookingService;
import com.myidea.gym.service.CoachService;
import com.myidea.gym.service.CourseService;
import com.myidea.gym.service.MemberService;
import com.myidea.gym.service.RbacService;
import com.myidea.gym.service.ScheduleService;
import com.myidea.gym.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminController {

    private final CourseService courseService;
    private final CoachService coachService;
    private final MemberService memberService;
    private final ScheduleService scheduleService;
    private final StoreService storeService;
    private final AdminAnalyticsService adminAnalyticsService;
    private final BookingService bookingService;
    private final SysUserMapper sysUserMapper;
    private final RbacService rbacService;

    @GetMapping("/dashboard")
    public Result<AdminDashboardView> dashboard() {
        return Result.ok(adminAnalyticsService.dashboard());
    }

    @GetMapping("/courses")
    public Result<List<AdminCourseView>> listCourses() {
        Path videoDir = Paths.get(System.getProperty("user.dir"), "uploads", "videos");
        List<Course> courses = courseService.listAll();
        List<AdminCourseView> views = courses.stream().map(course -> {
            AdminCourseView v = new AdminCourseView();
            v.setId(course.getId());
            v.setName(course.getName());
            v.setDescription(course.getDescription());
            v.setDurationMinutes(course.getDurationMinutes());
            v.setType(course.getType());
            v.setPrice(course.getPrice());
            v.setCategory(course.getCategory());
            v.setLevel(course.getLevel());
            v.setCalories(course.getCalories());
            v.setCoverImage(course.getCoverImage());
            v.setStatus(course.getStatus());
            v.setSummary(course.getSummary());
            String fileName = course.getId() == null ? null : course.getId() + ".mp4";
            v.setVideoFileName(fileName);
            File file = fileName == null ? null : videoDir.resolve(fileName).toFile();
            boolean exists = file != null && file.isFile();
            v.setHasVideo(exists);
            v.setVideoUrl(exists ? "http://localhost:8080/api/courses/" + course.getId() + "/video" : null);
            return v;
        }).toList();
        return Result.ok(views);
    }

    @PostMapping("/courses")
    public Result<Course> createCourse(@RequestBody Course course) {
        return Result.ok(courseService.create(course));
    }

    @PutMapping("/courses/{id}")
    public Result<Course> updateCourse(@PathVariable("id") Long id, @RequestBody Course course) {
        course.setId(id);
        return Result.ok(courseService.update(course));
    }

    @PostMapping("/courses/{id}/video")
    public Result<Void> uploadCourseVideo(@PathVariable("id") Long id, @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "videos");
            Files.createDirectories(dir);
            Path dest = dir.resolve(id + ".mp4");
            file.transferTo(dest);
            return Result.ok();
        } catch (java.io.IOException e) {
            throw new RuntimeException("上传视频失败", e);
        }
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> deleteCourse(@PathVariable("id") Long id) {
        courseService.delete(id);
        return Result.ok();
    }

    @GetMapping("/coaches")
    public Result<List<CoachView>> listCoaches() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "COACH")
                .orderByAsc(SysUser::getId));
        List<CoachView> views = users.stream().map(u -> {
            CoachView v = new CoachView();
            v.setUsername(u.getUsername());
            if (u.getRefId() != null) {
                Coach c = coachService.getById(u.getRefId());
                if (c != null) {
                    v.setId(c.getId());
                    v.setName(c.getName());
                    v.setPhone(c.getPhone());
                    v.setIntro(c.getIntro());
                    v.setTags(c.getTags());
                    v.setAvatar(c.getAvatar());
                }
            }
            if (v.getName() == null) v.setName(u.getUsername());
            return v;
        }).toList();
        return Result.ok(views);
    }

    @PostMapping("/coaches")
    public Result<Coach> createCoach(@RequestBody Coach coach) {
        return Result.ok(coachService.create(coach));
    }

    @DeleteMapping("/coaches/{id}")
    public Result<Void> deleteCoach(@PathVariable("id") Long id) {
        coachService.delete(id);
        return Result.ok();
    }

    @GetMapping("/members")
    public Result<List<MemberView>> listMembers() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, "MEMBER")
                .orderByAsc(SysUser::getId));
        List<MemberView> views = users.stream().map(u -> {
            MemberView v = new MemberView();
            v.setUsername(u.getUsername());
            if (u.getRefId() != null) {
                Member m = memberService.getById(u.getRefId());
                if (m != null) {
                    v.setId(m.getId());
                    v.setName(m.getName());
                    v.setPhone(m.getPhone());
                    v.setExpireDate(m.getExpireDate());
                    v.setBalance(m.getBalance());
                }
            }
            if (v.getName() == null) v.setName(u.getUsername());
            return v;
        }).toList();
        return Result.ok(views);
    }

    @PostMapping("/members")
    public Result<Member> createMember(@RequestBody Member member) {
        return Result.ok(memberService.create(member));
    }

    @DeleteMapping("/members/{id}")
    public Result<Void> deleteMember(@PathVariable("id") Long id) {
        memberService.delete(id);
        return Result.ok();
    }

    @GetMapping("/schedules/upcoming")
    public Result<List<ScheduleView>> upcomingSchedules() {
        return Result.ok(scheduleService.listUpcoming());
    }

    @PostMapping("/schedules")
    public Result<CourseSchedule> createSchedule(@RequestBody CourseSchedule schedule) {
        return Result.ok(scheduleService.create(schedule));
    }

    @PutMapping("/schedules/{id}")
    public Result<CourseSchedule> updateSchedule(@PathVariable("id") Long id, @RequestBody CourseSchedule schedule) {
        schedule.setId(id);
        return Result.ok(scheduleService.update(schedule));
    }

    @DeleteMapping("/schedules/{id}")
    public Result<Void> deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/stores")
    public Result<List<Store>> listStores() {
        return Result.ok(storeService.listAll());
    }

    @PostMapping("/stores")
    public Result<Store> createStore(@RequestBody Store store) {
        return Result.ok(storeService.create(store));
    }

    @PutMapping("/stores/{id}")
    public Result<Store> updateStore(@PathVariable("id") Long id, @RequestBody Store store) {
        store.setId(id);
        return Result.ok(storeService.update(store));
    }

    @DeleteMapping("/stores/{id}")
    public Result<Void> deleteStore(@PathVariable("id") Long id) {
        storeService.delete(id);
        return Result.ok();
    }

    @GetMapping("/bookings")
    public Result<List<BookingMemberView>> bookings() {
        return Result.ok(bookingService.listBookingsForAdmin(false));
    }

    @GetMapping("/reviews")
    public Result<List<BookingMemberView>> reviews() {
        return Result.ok(bookingService.listBookingsForAdmin(true));
    }

    @PostMapping("/bookings/{id}/attendance")
    public Result<Booking> markAttendance(@PathVariable("id") Long id, @RequestBody BookingAttendanceRequest req) {
        return Result.ok(bookingService.adminMarkAttendance(id, req.getAttendanceStatus()));
    }

    @PostMapping("/users/{id}/kick")
    public Result<Void> kick(@PathVariable("id") Long id) {
        StpUtil.kickout(id);
        return Result.ok();
    }

    @GetMapping("/roles")
    public Result<List<java.util.Map<String, String>>> listRoles() {
        return Result.ok(List.of(
                java.util.Map.of("code", "ADMIN", "name", "管理员"),
                java.util.Map.of("code", "COACH", "name", "教练"),
                java.util.Map.of("code", "MEMBER", "name", "会员")
        ));
    }

    @GetMapping("/users")
    public Result<List<SysUserView>> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .orderByAsc(SysUser::getId));
        List<SysUserView> views = users.stream().map(u -> {
            List<String> roles = rbacService.getRoleCodesByUserId(u.getId());
            SysUserView v = new SysUserView();
            v.setId(u.getId());
            v.setUsername(u.getUsername());
            v.setPrimaryRole(roles.isEmpty() ? u.getRole() : roles.get(0));
            v.setRoles(roles);
            v.setRefId(u.getRefId());

            // Set displayName based on role
            if ("COACH".equals(u.getRole()) && u.getRefId() != null) {
                Coach coach = coachService.getById(u.getRefId());
                if (coach != null) v.setDisplayName(coach.getName());
            } else if ("MEMBER".equals(u.getRole()) && u.getRefId() != null) {
                Member member = memberService.getById(u.getRefId());
                if (member != null) v.setDisplayName(member.getName());
            } else {
                v.setDisplayName(u.getUsername());
            }
            return v;
        }).toList();
        return Result.ok(views);
    }

    @PostMapping("/users/{id}/roles")
    public Result<Void> setUserRoles(@PathVariable("id") Long id, @RequestBody SetUserRolesRequest req) {
        rbacService.setUserRoles(id, req.getRoleCodes());
        return Result.ok();
    }
}
