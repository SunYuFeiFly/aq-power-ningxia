package com.wangchen.mapper;

import com.wangchen.entity.SevenSign;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户七天签到表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-03
 */
@Component
@Mapper
public interface SevenSignMapper extends BaseMapper<SevenSign> {

}
