package com.myidea.gym.it;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CourseVideoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getVideo_shouldReturn404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/courses/987654/video"))
                .andExpect(status().isNotFound());
    }

    @Test
    void upload_thenGetVideo_shouldWork() throws Exception {
        String token = loginAsAdmin();
        long courseId = 9999L;
        Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "videos");
        Files.createDirectories(dir);
        Path dest = dir.resolve(courseId + ".mp4");
        Files.deleteIfExists(dest);

        MockMultipartFile file = new MockMultipartFile("file", "demo.mp4", "video/mp4", new byte[]{0, 1, 2, 3, 4, 5});
        mockMvc.perform(multipart("/api/admin/courses/" + courseId + "/video")
                        .file(file)
                        .header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/courses/" + courseId + "/video"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp4"));
    }

    private String loginAsAdmin() throws Exception {
        MvcResult resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return JsonPath.read(resp.getResponse().getContentAsString(), "$.data.token");
    }
}

