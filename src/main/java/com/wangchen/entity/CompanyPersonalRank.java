package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 公司个人排行榜表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_company_personal_rank")
public class CompanyPersonalRank extends Model<CompanyPersonalRank> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
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
     * 段位荣誉id
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
     * 经验值
     */
    private Integer allExp;

    /**
     * 成就点数
     */
    private Integer allAch;

    /**
     * 用于分辨公司个人排行榜信息是 1:历史总排行 2:年内总排行
     */
    private Integer type;

    /**
     * 所属公司类型 1:自有公司、2代维公司、3设计和监理公司
     */
    private Integer companyType;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
