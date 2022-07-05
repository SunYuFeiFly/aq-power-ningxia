package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 活动赛题库
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_activity_topic")
public class ActivityTopic extends Model<ActivityTopic> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 活动赛id
     */
    private Integer activityId;

    /**
     * A类部门类型： 1:通信发展 2:运行维护 3:行业拓展 4:能源经营 5:财务管理 6:人力资源 7:党群纪检 8:综合管理
     * B类部门类型： 101:代维
     * C类部门类型： 201:地勘 202:监理 203:设计 204:施工 205:土建 206:电力 207:机房 208:动力配套 209:电池管理 210:天线 211:门禁 212:其它
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
     * 正确选项id 对应答案表id
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
     * 在部门题库中的编号
     */
    private Integer branchTopicId;

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
     * 活动答题所属公司 1:自有公司活动答题、2代维公司活动答题、3设计和监理公司活动答题
     */
    private Integer companyType;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
