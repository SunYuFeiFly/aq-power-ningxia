package com.wangchen.mapper;

import com.wangchen.entity.AlertTips;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户弹框提示(称号、成就获得提示  团队赛和活动赛获得提示) Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@Component
@Mapper
public interface AlertTipsMapper extends BaseMapper<AlertTips> {

}
