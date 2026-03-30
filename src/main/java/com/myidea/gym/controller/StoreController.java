package com.myidea.gym.controller;

import com.myidea.gym.common.Result;
import com.myidea.gym.model.entity.Store;
import com.myidea.gym.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public Result<List<Store>> listStores() {
        return Result.ok(storeService.listOpenStores());
    }
}
