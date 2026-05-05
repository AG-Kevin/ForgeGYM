package com.myidea.gym.it;

import com.jayway.jsonpath.JsonPath;
import com.myidea.gym.mapper.CoachMapper;
import com.myidea.gym.mapper.CourseMapper;
import com.myidea.gym.mapper.CourseScheduleMapper;
import com.myidea.gym.mapper.StoreMapper;
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
class MemberBookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseScheduleMapper courseScheduleMapper;

    @Test
    void recharge_book_cancel_shouldSucceed() throws Exception {
        String username = "flow_" + System.currentTimeMillis();
        String password = "p123456";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        String token = login(username, password);

        mockMvc.perform(post("/api/member/recharge")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\":30,\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.active").value(true));

        long scheduleId = prepareFutureSchedule(new BigDecimal("50.00"));

        MvcResult bookResp = mockMvc.perform(post("/api/member/bookings")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scheduleId\":" + scheduleId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        Number bookingId = JsonPath.read(bookResp.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/member/bookings/" + bookingId.longValue() + "/cancel")
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult profileResp = mockMvc.perform(get("/api/member/profile").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        Double balance = JsonPath.read(profileResp.getResponse().getContentAsString(), "$.data.balance");
        assertThat(balance).isEqualTo(200.0);
    }

    private String login(String username, String password) throws Exception {
        MvcResult resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return JsonPath.read(resp.getResponse().getContentAsString(), "$.data.token");
    }

    private long prepareFutureSchedule(BigDecimal price) {
        Store store = new Store();
        store.setName("流程测试门店");
        store.setStatus("OPEN");
        store.setCapacity(100);
        storeMapper.insert(store);

        Coach coach = new Coach();
        coach.setName("流程测试教练");
        coachMapper.insert(coach);

        Course course = new Course();
        course.setName("流程测试课程");
        course.setType("GROUP");
        course.setStatus("ACTIVE");
        course.setDurationMinutes(60);
        course.setPrice(price);
        course.setSummary("流程测试课程");
        courseMapper.insert(course);

        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(course.getId());
        schedule.setCoachId(coach.getId());
        schedule.setStoreId(store.getId());
        schedule.setCapacity(5);
        schedule.setStartTime(LocalDateTime.now().plusDays(3));
        schedule.setEndTime(LocalDateTime.now().plusDays(3).plusHours(1));
        courseScheduleMapper.insert(schedule);
        return schedule.getId();
    }
}
