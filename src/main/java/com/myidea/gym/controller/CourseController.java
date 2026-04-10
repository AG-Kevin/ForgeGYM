package com.myidea.gym.controller;

import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.CourseCatalogView;
import com.myidea.gym.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/catalog")
    public Result<List<CourseCatalogView>> catalog() {
        return Result.ok(courseService.listCatalog());
    }

    @GetMapping(value = "/{id}/video", produces = "video/mp4")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> getVideo(@org.springframework.web.bind.annotation.PathVariable("id") Long id) {
        Path dir = Paths.get(System.getProperty("user.dir"), "uploads", "videos");
        java.io.File file = dir.resolve(id + ".mp4").toFile();
        if (!file.exists()) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        return org.springframework.http.ResponseEntity.ok(new org.springframework.core.io.FileSystemResource(file));
    }
}
