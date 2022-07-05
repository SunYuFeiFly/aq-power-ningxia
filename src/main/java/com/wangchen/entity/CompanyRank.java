package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
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
 * 所有公司的排行榜
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_company_rank")
public class CompanyRank extends Model<CompanyRank> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公司编号
     */
    private Integer companyId;

    /**
     * 公司名称
     */
    private String companyName;

//    /**
//     * 排行榜时间
//     */
//    private String rankMonth;

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
     * 总人数
     */
    private Integer count;

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
     * 2期新增字段：1:自有员工公司，2代维公司，3设计和监理公司
     */
    private Integer companyType;

    /**
     * 用于分辨公司排行榜信息是 1:历史总排行 2:年内总排行
     */
    private Integer type;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
