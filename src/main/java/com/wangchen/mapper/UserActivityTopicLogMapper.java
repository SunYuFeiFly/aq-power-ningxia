package com.wangchen.mapper;

import com.wangchen.entity.UserActivityTopicLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 活动赛记录用户答题信息 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-10-29
 */
@Mapper
@Component
public interface UserActivityTopicLogMapper extends BaseMapper<UserActivityTopicLog> {

}
