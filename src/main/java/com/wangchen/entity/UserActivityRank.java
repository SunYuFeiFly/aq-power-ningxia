package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
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
 * 活动赛排行榜信息
 * </p>
 *
 * @author yinguang
 * @since 2020-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_user_activity_rank")
public class UserActivityRank extends Model<UserActivityRank> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 排行榜时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date rankDate;

    /**
     * 排名
     */
    private Integer rankNo;

    /**
     * 分数值
     */
    private Integer score;

    /**
     * openId
     */
    private String openId;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 等级值 已经用的是当前等级值 而不是等级Id
     */
    private Integer levelNo;

    /**
     * 段位荣誉id
     */
    private Integer honorNo;

    /**
     * 段位荣誉名称
     */
    private String honorName;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 部门名称
     */
    private String branchName;

    /**
     * 本次获得经验
     */
    private Integer getExp;

    /**
     * 本次获得塔币
     */
    private Integer getCoin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
