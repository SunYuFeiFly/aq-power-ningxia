package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *  用户收货地址对照表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_user_goods_address")
public class UserGoodsAddress extends Model<UserGoodsAddress> {

private static final long serialVersionUID=1L;

    /**
     * 地址ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String openId;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 收货地址
     */
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
