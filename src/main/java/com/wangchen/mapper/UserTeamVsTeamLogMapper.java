package com.wangchen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.entity.UserTeamVsTeamLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserTeamVsTeamLogMapper extends BaseMapper<UserTeamVsTeamLog> {

}
