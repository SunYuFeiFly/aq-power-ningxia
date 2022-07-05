package com.wangchen.mapper;

import com.wangchen.entity.HotLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户完成公告表 比如:(某某完成了每日答题)、某某获得了什么成就等等 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface HotLogMapper extends BaseMapper<HotLog> {

}
