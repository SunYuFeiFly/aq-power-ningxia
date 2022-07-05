package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 非专业部门题库表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
@Data
//@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_fei_branch_topic")
public class FeiBranchTopic extends Model<FeiBranchTopic> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题目类型 0选择题 1填空题 2判断题
     */
    private Integer topicType;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 正确选项id 对应非专业部门答案表id
     */
    private Integer correctOptionId;

    /**
     * 正确解析
     */
    private String correctParse;

    /**
     * 提示
     */
    private String point;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 上传图片地址
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String imageUrl;

    /**
     * 上传视频地址
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String videoUrl;

    /**
     * 题目所属公司类型(1:自有公司，2:代维公司，3:设计和监理公司)
     */
    private Integer companyType;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeiBranchTopic)) {
            return false;
        }
        FeiBranchTopic that = (FeiBranchTopic) o;
        return getCorrectParse().equals(that.getCorrectParse());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCorrectParse());
    }
}
