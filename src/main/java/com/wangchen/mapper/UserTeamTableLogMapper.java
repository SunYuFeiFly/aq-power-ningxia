package com.wangchen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.entity.UserTeamTableLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserTeamTableLogMapper extends BaseMapper<UserTeamTableLog> {

}
