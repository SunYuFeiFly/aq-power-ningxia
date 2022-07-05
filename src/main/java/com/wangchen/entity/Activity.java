package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.utils.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 活动赛表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_activity")
public class Activity extends Model<Activity> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 活动赛名称
     */
    private String name;

    /**
     * 开始的日期 年-月-日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除 0:未删除 1:已删除(一期)
     * 是否删除 0:上线中 1:已删除 2:待上线(二期)
     */
    private Integer deleted;

    /**
     * 活动所属公司 1:自有公司活动答题题库、2代维公司活动答题题库、3设计和监理公司活动答题题库
     */
    private Integer companyType;

    public String getStartTime2() {
        return DateUtil.format(startTime);
    }

    public String getEndTime2() {
        return DateUtil.format(endTime);
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
