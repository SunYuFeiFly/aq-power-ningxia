package com.wangchen.mapper;

import com.wangchen.entity.ActivityOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 活动赛答案 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Component
@Mapper
public interface ActivityOptionMapper extends BaseMapper<ActivityOption> {

}
