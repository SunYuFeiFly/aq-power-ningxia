package com.wangchen.mapper;

import com.wangchen.entity.BaseUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface BaseUserMapper extends BaseMapper<BaseUser> {

    // 批量删除选中基本用户信息
    void deleteByIds(@Param("ids") int[] ids);
}
