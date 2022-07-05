package com.wangchen.vo;

import lombok.Data;

/**
 * 部门导出题库vo（填空题）
 * @author liJian
 * @since 2020-06-08
 */

@Data
public class BranchTopicWriteTKVO {

    /**
     * 题目标题
     */
    private String title;

    /**
     * 填空项1
     */
    private String tContext1;

    /**
     * 填空项2
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
