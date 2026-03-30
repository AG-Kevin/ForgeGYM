package com.myidea.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myidea.gym.mapper.StoreMapper;
import com.myidea.gym.model.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;

    public List<Store> listAll() {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>()
                .orderByAsc(Store::getId));
    }

    public List<Store> listOpenStores() {
        return storeMapper.selectList(new LambdaQueryWrapper<Store>()
                .eq(Store::getStatus, "OPEN")
                .orderByAsc(Store::getId));
    }

    public Store create(Store store) {
        normalize(store);
        storeMapper.insert(store);
        return store;
    }

    public Store update(Store store) {
        if (store.getId() == null || storeMapper.selectById(store.getId()) == null) {
            throw new IllegalArgumentException("门店不存在");
        }
        normalize(store);
        storeMapper.updateById(store);
        return store;
    }

    public void delete(Long id) {
        storeMapper.deleteById(id);
    }

    public Store getById(Long id) {
        return storeMapper.selectById(id);
    }

    private void normalize(Store store) {
        if (store.getName() == null || store.getName().isBlank()) {
            throw new IllegalArgumentException("门店名称不能为空");
        }
        if (store.getCapacity() == null || store.getCapacity() < 0) {
            store.setCapacity(0);
        }
        if (store.getStatus() == null || store.getStatus().isBlank()) {
            store.setStatus("OPEN");
        }
    }
}
