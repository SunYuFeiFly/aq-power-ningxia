package com.wangchen.vo;

import lombok.Data;

/**
 * <p>
 * 后台功能员工列表信息
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Data
public class CompanyUserInfoDetailVo {

    /**
     * 用户名称
     */
    private String openId;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 今日是否签到
     */
    private String isSign;

    /**
     * 每日答题次数
     */
    private Integer dayGameNum;

    /**
     * 每日答题正确率
     */
    private String dayGameTrueRate;

    /**
     * 活动赛答题正确率
     */
    private String activityRate;


    /**
     * 个人赛次数
     */
    private Integer oneVsOneNum;

    /**
     * 个人赛正确率
     */
    private String oneVsOneTrueRate;

    /**
     * 团队赛正确率
     */
    private String threeVsThreeTrueRate;

    /**
     * 完成题目总数
     */
    private Integer countNum;

    /**
     * 完成题目总正确率
     */
    private String countTrueRate;

    /**
     * 是否拥有成就
     */
    private String isHasAchievement;

}
