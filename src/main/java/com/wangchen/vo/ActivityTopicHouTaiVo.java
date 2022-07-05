package com.wangchen.vo;

import lombok.Data;

/**
 * 用于活动赛添加题目、查看题目vo
 */

@Data
public class ActivityTopicHouTaiVo {

    private Integer id;

    /**
     * A类部门类型： 1:通信发展 2:运行维护 3:行业拓展 4:能源经营 5:财务管理 6:人力资源 7:党群纪检 8:综合管理
     * B类部门类型： 101:代维
     * C类部门类型： 201:地勘 202:监理 203:设计 204:施工 205:土建 206:电力 207:机房 208:动力配套 209:电池管理 210:天线 211:门禁 212:其它
     *
     * 活动赛暂时部分部门
     */
    private Integer type;

    /**
     * 题目类型 0选择题 1填空题 2判断题
     */
    private Integer topicType;

    /**
     * 题目正确分析
     */
    private String correctParse;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 选项1 选择题的正确答案
     */
    private String context1;

    /**
     * 选择题 干扰1
     */
    private String context2;

    /**
     * 选择题 干扰2
     */
    private String context3;

    /**
     * 选择题 干扰3
     */
    private String context4;


    /**
     * 填空题1
     */
    private String tContext1;

    /**
     * 填空题1
     */
    private String tContext2;

    /**
     * 判断题
     */
    private String pContext;

    /**
     * 题目所属公司类型(1:自有公司，2:代维公司，3:设计和监理公司)
     */
    private Integer companyType;

    /**
     * 上传图片地址
     */
    private String imageUrl;

    /**
     * 上传视频地址
     */
    private String videoUrl;

    /**
     * 题目所属活动赛id
     */
    private Integer activityId;
}
