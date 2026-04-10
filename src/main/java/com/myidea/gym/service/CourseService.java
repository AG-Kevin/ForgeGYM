package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.dto.CourseCatalogView;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CoachMapper coachMapper;
    private final StoreMapper storeMapper;

    public List<Course> listAll() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .orderByDesc(Course::getId));
    }

    public List<Course> listActive() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, "ACTIVE")
                .orderByDesc(Course::getId));
    }

    public Course create(Course course) {
        normalizeCourse(course);
        courseMapper.insert(course);
        return course;
    }

    public Course update(Course course) {
        if (course.getId() == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (courseMapper.selectById(course.getId()) == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        normalizeCourse(course);
        courseMapper.updateById(course);
        return course;
    }

    public void delete(Long id) {
        courseMapper.deleteById(id);
    }

    public List<CourseCatalogView> listCatalog() {
        List<Course> courses = listActive();
        List<CourseSchedule> schedules = courseScheduleMapper.selectList(new LambdaQueryWrapper<CourseSchedule>()
                .ge(CourseSchedule::getStartTime, LocalDateTime.now())
                .orderByAsc(CourseSchedule::getStartTime));
        Map<Long, List<CourseSchedule>> scheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(CourseSchedule::getCourseId));
        Map<Long, Coach> coachMap = coachMapper.selectList(new LambdaQueryWrapper<Coach>().orderByAsc(Coach::getId))
                .stream()
                .collect(Collectors.toMap(Coach::getId, Function.identity(), (a, b) -> a));
        Map<Long, Store> storeMap = storeMapper.selectList(new LambdaQueryWrapper<Store>().orderByAsc(Store::getId))
                .stream()
                .collect(Collectors.toMap(Store::getId, Function.identity(), (a, b) -> a));

        Path videoDir = Paths.get(System.getProperty("user.dir"), "uploads", "videos");
        return courses.stream().map(course -> {
            List<CourseSchedule> related = scheduleMap.getOrDefault(course.getId(), List.of());
            CourseCatalogView view = new CourseCatalogView();
            view.setId(course.getId());
            view.setName(course.getName());
            view.setDescription(course.getDescription());
            view.setDurationMinutes(course.getDurationMinutes());
            view.setType(course.getType());
            view.setPrice(course.getPrice());
            view.setCategory(course.getCategory());
            view.setLevel(course.getLevel());
            view.setCalories(course.getCalories());
            view.setCoverImage(course.getCoverImage());

            java.io.File file = videoDir.resolve(course.getId() + ".mp4").toFile();
            if (file.exists()) {
                view.setVideoUrl("http://localhost:8080/api/courses/" + course.getId() + "/video");
            } else {
                view.setVideoUrl(null);
            }

            view.setSummary(course.getSummary());
            view.setCoachNames(related.stream()
                    .map(item -> coachMap.get(item.getCoachId()))
                    .filter(java.util.Objects::nonNull)
                    .map(Coach::getName)
                    .distinct()
                    .toList());
            view.setStoreNames(related.stream()
                    .map(item -> storeMap.get(item.getStoreId()))
                    .filter(java.util.Objects::nonNull)
                    .map(Store::getName)
                    .distinct()
                    .toList());
            view.setUpcomingCount(related.size());
            return view;
        }).toList();
    }

    private void normalizeCourse(Course course) {
        if (course.getName() == null || course.getName().isBlank()) {
            throw new IllegalArgumentException("课程名不能为空");
        }
        if (course.getDurationMinutes() == null || course.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("课程时长必须大于0");
        }
        if (course.getPrice() == null) {
            course.setPrice(BigDecimal.ZERO);
        }
        if (course.getType() == null || course.getType().isBlank()) {
            course.setType("GROUP");
        }
        if (course.getStatus() == null || course.getStatus().isBlank()) {
            course.setStatus("ACTIVE");
        }
        if (course.getSummary() == null || course.getSummary().isBlank()) {
            course.setSummary(course.getDescription());
        }
    }
}
