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
public class ExportCompanyCsvVo {

    private Integer id;

    // 单位
    private String companyName;

    // 总人数
    private Integer countNum;

    // 注册人数
    private Integer registerNum;

    // 塔币
    private Integer allCoinNum;

    // 成就值
    private Integer allAchievementNum;

    // 总经验值
    private Integer allExperienceNum;

    // 年度经验值
    private Integer presentExperienceNum;

    // 签到人数
    private Integer allSignNum;

    // 签到率（%）
    private String allSignRate;

    // 每日答题参与度（%）
    private String dayGameRate;

    // 每日答题正确率（%）
    private String dayGameTrueRate;

    // 个人赛参与度
    private String oneVsOneRate;

//    // 团队赛参与度
//    private String threeVsThreeRate;

    // 团队赛参与度
    private String teamVsTeamRate;


}