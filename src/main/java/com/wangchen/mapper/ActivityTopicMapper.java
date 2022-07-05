package com.wangchen.mapper;

import com.wangchen.entity.ActivityTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 活动赛题库 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Component
@Mapper
public interface ActivityTopicMapper extends BaseMapper<ActivityTopic> {

    @Select("SELECT * FROM aq_activity_topic where activity_id =#{activityId} ORDER BY RAND() LIMIT 10")
        //分页查询问题设置 ${ew.customSqlSegment}
    List<ActivityTopic> listTopicRandom(@Param("activityId") Integer activityId);
}
