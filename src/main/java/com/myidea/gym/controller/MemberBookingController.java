package com.myidea.gym.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.myidea.gym.common.Result;
import com.myidea.gym.model.dto.BookRequest;
import com.myidea.gym.model.dto.BookingReviewRequest;
import com.myidea.gym.model.dto.BookingScheduleView;
import com.myidea.gym.model.entity.Booking;
import com.myidea.gym.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member/bookings")
@RequiredArgsConstructor
public class MemberBookingController {

    private final BookingService bookingService;

    @SaCheckLogin
    @GetMapping
    public Result<List<BookingScheduleView>> myBookings() {
        return Result.ok(bookingService.myBookings());
    }

    @SaCheckLogin
    @SaCheckPermission("member:booking:book")
    @PostMapping
    public Result<Booking> book(@Valid @RequestBody BookRequest req) {
        return Result.ok(bookingService.book(req.getScheduleId()));
    }

    @SaCheckLogin
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        bookingService.cancel(id);
        return Result.ok();
    }

    @SaCheckLogin
    @PostMapping("/{id}/review")
    public Result<Booking> review(@PathVariable("id") Long id, @RequestBody BookingReviewRequest req) {
        return Result.ok(bookingService.review(id, req.getRating(), req.getReviewContent()));
    }
}
