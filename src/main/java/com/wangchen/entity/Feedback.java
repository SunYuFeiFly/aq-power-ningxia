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
 * 用户反馈表信息
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_feedback")
public class Feedback extends Model<Feedback> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String openId;
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String phone;
    //0普通用户反馈 1部门反馈
    private Integer type;

    private String context;
    /**
     * 状态描述 0未处理 1反馈成功 2反馈不成功
     */
    private Integer status;

    /**
     * 反馈问题人员所属所属公司类型(1:自有公司，2:代维公司，3:设计和监理公司)
     */
    private Integer companyType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
