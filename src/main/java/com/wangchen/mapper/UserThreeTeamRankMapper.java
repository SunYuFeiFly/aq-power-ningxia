package com.wangchen.mapper;

import com.wangchen.entity.UserThreeTeamRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 团队赛排行榜信息表 Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2020-08-06
 */
@Component
@Mapper
public interface UserThreeTeamRankMapper extends BaseMapper<UserThreeTeamRank> {

}
