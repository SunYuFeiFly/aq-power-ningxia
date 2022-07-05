package com.wangchen.mapper;

import com.wangchen.entity.Manguan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-04
 */
@Component
@Mapper
public interface ManguanMapper extends BaseMapper<Manguan> {

}
