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
 * 
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_user_goods")
public class UserGoods extends Model<UserGoods> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户openId
     */
    private String openId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品类型(0皮肤 1头像 2实物)
     */
    private Integer goodsType;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片地址
     */
    private String goodsUrl;

    /**
     * 当时是多少积分兑换的
     */
    private Integer scoreNow;

    /**
     * 是否使用当前这个(0不使用 1使用)
     */
    private Integer isFlag;

    /**
     * 兑换地址,只有实物奖品才有
     */
    private Integer addressId;
    @TableField(exist = false)
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除 0未删除  1已删除
     */
    private Integer deleted;

    /**
     * 是否使用过 0未使用过 1使用过
     */
    private Integer isUsed;

    @TableField(exist = false)
    private String nickName;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
