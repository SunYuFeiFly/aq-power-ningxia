package com.wangchen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.vo
 * @ClassName: GoodsVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/8 14:57
 * @Version: 1.0
 */
@Data
public class GoodsVo {

    private Integer id;

    /**
     * 商品类型(0皮肤 1头像 2实物)
     */
    private Integer type;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片地址
     */
    private String url;

    /**
     * 需要兑换的积分数
     */
    private Integer score;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 库存
     */
    private Integer store;

    /**
     * 商品兑换数量
     */
    private Integer goodsExchangeNum;

    /**
     * 0未兑换 1已兑换
     */
    private Integer isDuiHuan;

}