package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_goods")
public class Goods extends Model<Goods> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品类型(0皮肤 1头像 2实物)
     */
    private Integer type;

    /**
     * 商品适用 0男 1女 2通用
     */
    private Integer sex;

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

    /**
     * 是否默认皮肤 0不是 1是
     */
    private Integer isMoRen;

    /**
     * 是否上架 0下架 1上架
     */
    private Integer status;

    /**
     * 库存
     */
    private Integer store;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除 0未删除  1已删除
     */
    private Boolean deleted;

    /**
     * 商品兑换数量
     */
    // @TableField(exist = false)
    private Integer goodsExchangeNum;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
