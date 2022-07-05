package com.wangchen.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 经验明细表
 * </p>
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_experience")
public class Experience {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * openid
     */
    private String openId;

    /**
     * 用户全月每天的经验记录
     */
    private String dayExperience;

    /**
     * 用户全年每月底的经验记录
     */
    private Integer monthExperience;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 用户经验数据月份
     */
    private Integer partMonth;

    /**
     * 用户经验数据年份
     */
    private Integer partYear;

}
