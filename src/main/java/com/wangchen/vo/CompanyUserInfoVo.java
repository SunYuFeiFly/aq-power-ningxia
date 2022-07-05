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
public class CompanyUserInfoVo {

    private Integer id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 总人数
     */
    private Integer allNum;

    /**
     * 注册人数
     */
    private Integer registerNum;

    /**
     * 今日签到人数
     */
    private Integer signNum;

    /**
     * 完成今日答题人数
     */
    private Integer dayGameNum;

}
