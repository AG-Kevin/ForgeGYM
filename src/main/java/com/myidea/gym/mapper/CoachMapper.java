package com.myidea.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myidea.gym.model.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {
}
