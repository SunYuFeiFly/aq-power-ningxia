package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 用户表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_user")
public class User extends Model<User> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * openId
     */
    private String openId;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 用户真实昵称
     */
    private String name;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 所属公司
     */
    private Integer companyId;

    /**
     * 所属名称
     */
    private String companyName;

    /**
     * 备用字段1
     */
    private String content1;

    /**
     * 备用字段2
     */
    private String content2;

    /**
     * 备用字段3
     */
    private String content3;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registeredTime;

    /**
     * 总经验
     */
    private Integer allExperience;

    /**
     * 总塔币
     */
    private Integer allCoin;

    /**
     * 总成就点
     */
    private Integer allAchievement;

    /**
     * 兑换奖品总数
     */
    private Integer prizeCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    /**
     * 是否删除(0未删除 1已删除)
     */
    private Integer deleted;

    /**
     * 用户种类
     * 2期新增字段：1:自有员工，2代维公司员工，3设计和监理公司员工
     */
    private Integer type;

    /**
     * 截至上一年年底用户获得总积分（如2020.12.31 23:59:59时所拥有积分）
     */
    private Integer lastYearExperience;

    /**
     * 当前年度截至目前获取总积分（如2021.01.01 00:00:00 - 目前所拥有积分）
     */
    private Integer presentExperience;

    /**
     * 升至下一级所需经验
     */
    @TableField(exist = false)
    private Integer nextLevelNeedExperience;

    /**
     * 下一级所占总经验
     */
    @TableField(exist = false)
    private Integer nextLevelExperience;

    /**
     * 下一成就名称
     */
    @TableField(exist = false)
    private String nextAchievementName;

    /**
     * 升至下一成就所需条件
     */
    @TableField(exist = false)
    private String nextAchievementCondition;

    /**
     * 所有成就总共兑换的积分
     */
    @TableField(exist = false)
    private Integer allIntegral;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    /**
     * 封裝增加用戶總經驗方法
     */
    public void addAllExperience(Integer experience) {
        this.allExperience +=  experience;
        this.presentExperience +=  experience;
    }

}
