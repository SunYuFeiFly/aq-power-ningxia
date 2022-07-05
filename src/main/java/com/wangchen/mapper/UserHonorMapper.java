package com.wangchen.mapper;

import com.wangchen.entity.UserHonor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户称号表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface UserHonorMapper extends BaseMapper<UserHonor> {

    @Select("select * from aq_user_honor where open_id =#{openId} order by create_time desc limit 0,1")
    UserHonor getLastHonor(@Param(value = "openId") String openId);

}
