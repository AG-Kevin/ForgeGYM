package com.myidea.gym.it;

import com.jayway.jsonpath.JsonPath;
import com.myidea.gym.mapper.BookingMapper;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.MemberMapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.model.entity.Coach;
import com.myidea.gym.model.entity.Course;
import com.myidea.gym.model.entity.CourseSchedule;
import com.myidea.gym.model.entity.Store;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class MemberBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private MemberMapper memberMapper;

    @Test
    void myBookings_shouldRequireLogin() throws Exception {
        mockMvc.perform(get("/api/member/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    void myBookings_shouldReturnListForMember() throws Exception {
        String token = loginAs("member1", "member123");
        mockMvc.perform(get("/api/member/bookings").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void review_shouldUpdateRatingAndContentAfterClassEnd() throws Exception {
        long storeId = ensureStore();
        long coachId = ensureCoach();
        long courseId = ensureCourse();

        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(courseId);
        schedule.setCoachId(coachId);
        schedule.setStoreId(storeId);
        schedule.setCapacity(10);
        schedule.setStartTime(LocalDateTime.now().minusHours(2));
        schedule.setEndTime(LocalDateTime.now().minusHours(1));
        courseScheduleMapper.insert(schedule);

        Booking booking = new Booking();
        booking.setScheduleId(schedule.getId());
        booking.setMemberId(1L);
        booking.setStatus("COMPLETED");
        booking.setCreatedAt(LocalDateTime.now().minusHours(3));
        booking.setAmount(new BigDecimal("50.00"));
        booking.setAttendanceStatus("ATTENDED");
        bookingMapper.insert(booking);

        String token = loginAs("member1", "member123");

        mockMvc.perform(post("/api/member/bookings/" + booking.getId() + "/review")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"reviewContent\":\"体验很好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.rating").value(4))
                .andExpect(jsonPath("$.data.reviewContent").value("体验很好"));

        Booking updated = bookingMapper.selectById(booking.getId());
        assertThat(updated.getRating()).isEqualTo(4);
        assertThat(updated.getReviewContent()).isEqualTo("体验很好");
    }

    private String loginAs(String username, String password) throws Exception {
        MvcResult resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return JsonPath.read(resp.getResponse().getContentAsString(), "$.data.token");
    }

    private long ensureStore() {
        Store store = storeMapper.selectById(1L);
        if (store != null) return store.getId();
        Store s = new Store();
        s.setName("测试门店");
        s.setStatus("OPEN");
        s.setCapacity(100);
        storeMapper.insert(s);
        return s.getId();
    }

    private long ensureCoach() {
        Coach coach = coachMapper.selectById(1L);
        if (coach != null) return coach.getId();
        Coach c = new Coach();
        c.setName("测试教练");
        coachMapper.insert(c);
        return c.getId();
    }

    private long ensureCourse() {
        Course course = courseMapper.selectById(1L);
        if (course != null) return course.getId();
        Course c = new Course();
        c.setName("测试课程");
        c.setType("GROUP");
        c.setStatus("ACTIVE");
        c.setDurationMinutes(60);
        c.setPrice(new BigDecimal("50"));
        c.setSummary("测试课程");
        courseMapper.insert(c);
        return c.getId();
    }
}

