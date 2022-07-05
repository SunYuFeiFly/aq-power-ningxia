package com.wangchen.mapper;

import com.wangchen.entity.UserActivityRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 活动赛排行榜信息 Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2020-10-22
 */
@Component
@Mapper
public interface UserActivityRankMapper extends BaseMapper<UserActivityRank> {

}
