package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.UserGoods;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface UserGoodsService extends IService<UserGoods> {

    /**
     * 头像、皮肤商品兑换
     */
    Result userConvertGoods(@Param("openId") String openId, @Param("goodsId") Integer goodsId);

    /**
     * 实物商品兑换
     */
    Result userConvertShiWuGoods(@Param("openId") String openId, @Param("goodsId") Integer goodsId);

    /**
     * 用户保存收货地址、更新用户商品、用户、商品相关信息(二期)
     */
    Result saveAddress(@Param("openId") String openId, @Param("goodsId") Integer goodsId, @Param("name") String name, @Param("phone") String phone, @Param("address") String address);
}
