package com.wangchen.vo;


import lombok.Data;

/**
 * 部门题目导出临时对象
 */

@Data
public class TopicExportVo {

    /**
     * 题目标题
     */
    private String title;

    /**
     * 答案
     */
    private String select;

    /**
     * 选择题 选项1
     */
    private String context1;

    /**
     * 选择题 选项2
     */
    private String context2;

    /**
     * 选择题 干扰3
     */
    private String context3;

    /**
     * 选择题 干扰4
     */
    private String context4;

    /**
     * 填空题 填空项1
     */
    private String tContext1;

    /**
     * 填空题 填空项2
     */
    private String tContext2;

    /**
     * 题目正确分析
     */
    private String correctParse;

    /**
     * 上传图片地址
     */
    private String imageUrl;

    /**
     * 上传视频地址
     */
    private String videoUrl;
}
