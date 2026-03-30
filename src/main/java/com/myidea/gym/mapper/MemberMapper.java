package com.myidea.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myidea.gym.model.entity.Member;
import org.apache.ibatis.annotations.Select;

public interface MemberMapper extends BaseMapper<Member> {
    @Select("SELECT * FROM member WHERE id = #{id} FOR UPDATE")
    Member selectByIdForUpdate(Long id);
}
