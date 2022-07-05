package com.wangchen.mapper;

import com.wangchen.entity.UserActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户活动赛 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Component
@Mapper
public interface UserActivityMapper extends BaseMapper<UserActivity> {

}
