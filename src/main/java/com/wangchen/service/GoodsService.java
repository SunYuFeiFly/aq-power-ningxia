package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 商品列表页面（二期）
     */
    Result selectPages(@Param("page") int page, @Param("limit") int limit, @Param("name") String name);

    /**
     * 商品上下架 (二期)
     */
    void isStatus(@Param("id") Integer id, @Param("status") Integer status);

    /**
     * 新增修改商品 （二期）
     */
    void editGoods(@Param("goods") Goods goods);

    /**
     * 查询兑换列表（二期）
     */
    Result findExchangeList(@Param("name") String name, @Param("page") Integer page, @Param("limit") Integer limit);
}
