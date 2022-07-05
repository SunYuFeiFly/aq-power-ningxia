package com.wangchen.mapper;

import com.wangchen.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@Component
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

}
