package com.wangchen.vo;

import lombok.Data;

/**
 * 公司活跃查询
 * @Package: com.wangchen.vo
 * @ClassName: ExportCompanyCsvVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/9/14 14:08
 * @Version: 1.0
 */
@Data
public class ExportUserGameCsvVo {

    //用户名称
    private String openId;
    //单位
    private String companyName;
    //用户名称
    private String name;
    //现总经验值
    private Integer experienceNum;
    //现年度经验值
    private Integer presentExperienceNum;
    //现总经验值
    private Integer endExperienceNum;
    //现年度经验值
    private Integer endPresentExperienceNum;
    //经验值变化
    private Integer experienceChangeNum;
    //塔币
    private Integer coinNum;
    //成就点数
    private Integer achievementNum;
    //查询时间内签到次数
    private Integer signNum;


    //每日答题总次数
    private Integer dayGameNum;
    //每日答题正确题数
    private Integer dayGameTrueNum;
    //每日答题总正确率
    private String dayGameTrueRate;

    //个人赛总次数
    private Integer oneVsOneNum;
    //个人赛正确题数
    private Integer oneVsOneTrueNum;
    //个人赛总正确率
    private String oneVsOneTrueRate;

//    //团队赛总次数
//    private Integer threeVsThreeNum;
//    //团队赛正确题数
//    private Integer threeVsThreeTrueNum;
//    //团队赛总正确率
//    private String threeVsThreeTrueRate;

    //团队赛总次数
    private Integer teamVsTeamNum;
    //团队赛正确题数
    private Integer teamVsTeamTrueNum;
    //团队赛总正确率
    private String teamVsTeamTrueRate;

    //完成总题目数
    private Integer allAnswerNum;
    //总正确率
    private String allAnswerTrueRate;

}