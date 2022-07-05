package com.wangchen.vo;


import lombok.Data;

/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.vo
 * @ClassName: AchievementVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/12 17:24
 * @Version: 1.0
 */
@Data
public class CompanyPersonalRankVo {

    /**
     * 称号名称
     */
    private String openId;

    private String name;

    private Integer levelNo;

    private String companyName;
    //综合得分
    private Double compositeScore;

    private String avatar;

    private Integer allExp;

    private Integer allAch;

    /**
     * 用户种类
     * 2期新增字段：1:自有员工，2代维公司员工，3设计和监理公司员工
     */
    private Integer type;

    private Integer companyId;
}