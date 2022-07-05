package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 用户弹框提示(称号、成就获得提示  团队赛和活动赛获得提示)
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_alert_tips")
public class AlertTips extends Model<AlertTips> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 提示类型 0称号 1成就 2活动赛 3团队赛
     */
    private Integer type;

    private String openId;

    /**
     * 称号编号
     */
    private Integer honorId;

    /**
     * 称号名称
     */
    private String honorName;

    /**
     * 成就编号
     */
    private Integer achievementId;

    /**
     * 成就名称
     */
    private String achievementName;

    /**
     * 成就图片路径
     */
    @TableField(exist = false)
    private String achievementUrl;

    /**
     * 获得时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date  getDate;

    /**
     * 排名
     */
    private Integer rankNo;

    /**
     * 本次获得经验(活动赛和团队赛会有)
     */
    private Integer experience;

    /**
     * 本次获得塔币(活动赛和团队赛会有)
     */
    private Integer coin;

    /**
     * 是否查看 0未查看 1已查看
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 查看时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
