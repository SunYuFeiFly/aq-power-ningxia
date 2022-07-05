package com.wangchen.vo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.ActivityOption;
import com.wangchen.entity.BranchOption;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ProjectName: 活动赛
 * @Package: com.wangchen.vo
 * @ClassName: BranchTopicVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/10 14:40
 * @Version: 1.0
 */
@Data
public class ActivityTopicVo {

    private Integer id;

    /**
     * 活动赛id
     */
    private Integer activityId;

    /**
     * 部门类型 1通信发展 2运行维护 3行业拓展 4能源经营 5财务管理 6人力资源 7党群纪检 8财务管理
     */
    private Integer type;

    /**
     * 部门名称
     */
    private String typeName;

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
    private String parse;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private List<ActivityOption> branchOptionList;

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
}