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
 * 非专业部门题库答案表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_fei_branch_option")
public class FeiBranchOption extends Model<FeiBranchOption> {

private static final long serialVersionUID=1L;

    /**
     * 选项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 选项内容
     */
    private String content;

    /**
     * 题目ID
     */
    private Integer topicId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
