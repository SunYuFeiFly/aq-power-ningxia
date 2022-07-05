package com.wangchen.vo;

import lombok.Data;

/**
 * 部门导出题库vo（判断题）
 * @author liJian
 * @since 2020-06-08
 */

@Data
public class BranchTopicWritePDVO {

    /**
     * 题目标题
     */
    private String title;

    /**
     * 答案
     */
    private String answer;

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
