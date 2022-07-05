package com.wangchen.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.UserHonor;
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
public class CompanyPersonalRankQianDuanLookVo {

    private Integer id;

    /**
     * 公司编号
     */
    private Integer companyId;

    /**
     * 排行榜时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date rankDate;

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
     * 头像
     */
    private String avatar;

    /**
     * 等级
     */
    private Integer levelNo;

    /**
     * 总经验
     */
    private Integer allExp;

    /**
     * 综合得分
     */
    private Double compositeScore;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 段位名称 没有的话返回0
     */
    private Integer honorNo;

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