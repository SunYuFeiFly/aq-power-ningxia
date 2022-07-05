package com.wangchen.vo;

import lombok.Data;

/**
 * 实物兑换信息对象
 */

@Data
public class ExportExchangeGoodsCsvVo {

    /**
     * 用户id
     */
    private String openId;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户电话号码
     */
    private String mobile;

    /**
     * 用户所属公司名称
     */
    private String companyName;

    /**
     * 兑换商品id
     */
    private String goodId;

    /**
     * 兑换商品类型（0皮肤 1头像 2实物）
     */
    private Integer goodType;

    /**
     * 兑换商品名称
     */
    private String goodName;

    /**
     * 商品兑换时间
     */
    private String exchangeTime;

    /**
     * 商品收货地址
     */
    private String address;

}
