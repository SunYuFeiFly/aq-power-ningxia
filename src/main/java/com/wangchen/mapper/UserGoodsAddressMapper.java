package com.wangchen.mapper;

import com.wangchen.entity.UserGoodsAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
@Component
@Mapper
public interface UserGoodsAddressMapper extends BaseMapper<UserGoodsAddress> {

}
