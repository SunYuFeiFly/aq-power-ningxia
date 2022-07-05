package com.wangchen.mapper;

import com.wangchen.entity.UserGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.vo.ExportExchangeGoodsCsvVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface UserGoodsMapper extends BaseMapper<UserGoods> {

    // 查询特定类型下所有时间段内兑换记录
    @Select("select u.open_id as openId, u.name as name, u.mobile as mobile, u.company_name as companyName, g.goods_id as goodId, g.goods_type as goodType, g.goods_name as goodName, g.create_time as exchangeTime, a.address as address " +
            "FROM aq_user_goods g LEFT JOIN aq_user u ON g.open_id = u.open_id " +
            "LEFT JOIN aq_user_goods_address a ON g.address_id = a.id WHERE g.goods_type = #{goodType} ORDER BY g.id")
    List<ExportExchangeGoodsCsvVo> exportExchangeGoodsInfo(@Param("goodType") Integer goodType);
}
