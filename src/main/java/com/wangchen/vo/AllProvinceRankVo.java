package com.wangchen.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.vo
 * @ClassName: AchievementVo
 * @Author: 2
 * @Description: 全省排行榜信息
 * @Date: 2020/6/12 17:24
 * @Version: 1.0
 */
@Data
public class AllProvinceRankVo {

    /**
     * 称号名称
     */
    private String openId;

    private String name;

    private Integer levelNo;

    private String companyName;

    private Integer allExp;

    private Integer allAch;
    //综合得分
    private Double compositeScore;

    private String avatar;

    /**
     * 用户种类
     * 2期新增字段：1:自有员工，2代维公司员工，3设计和监理公司员工
     */
    private Integer type;

}