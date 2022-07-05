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
public class CompanyRankVo {

    /**
     * 称号名称
     */
    private Integer companyId;

    private String companyName;

    //总人数
    private Integer count;

    //综合得分
    private Double compositeScore;

    /**
     * 用户种类
     * 2期新增字段：1:自有员工，2代维公司员工，3设计和监理公司员工
     */
    private Integer type;

}