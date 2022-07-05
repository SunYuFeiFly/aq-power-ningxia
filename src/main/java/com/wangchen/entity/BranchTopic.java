package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
     * 部门题库表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
@Data
//@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_branch_topic")
public class BranchTopic extends Model<BranchTopic> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * A类部门类型： 1:通信发展 2:行业拓展 3:能源经营 4:运营维护 5:综合管理 6:财务管理 7:人力资源 8:纪委办公室 9:党群工作组 10:商合中心 11:技术支撑中心
     */
    private Integer type;

    /**
     * 题目类型 0选择题 1填空题 2判断题
     */
    private Integer topicType;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 正确选项id 对应部门答案表id
     */
    private Integer correctOptionId;

    /**
     * 问题解析
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

    @TableField(exist = false)
    List<BranchOption> optionList;

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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BranchTopic that = (BranchTopic) o;
        return correctParse.equals(that.correctParse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correctParse);
    }
}
