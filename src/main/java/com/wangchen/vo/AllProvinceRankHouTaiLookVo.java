package com.wangchen.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

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
public class AllProvinceRankHouTaiLookVo {


    /**
     * 排名
     */
    private Integer rankNo;

    /**
     * openId
     */
    private String openId;

    /**
     * 名称
     */
    private String name;

    /**
     * 经验值
     */
    private Integer allExp;

    /**
     * 成就点数
     */
    private Integer allAch;

    /**
     * 综合得分
     */
    private Double compositeScore;

    /**
     * 段位荣誉名称
     */
    private String honorName;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 部门名称
     */
    private String branchName;
}