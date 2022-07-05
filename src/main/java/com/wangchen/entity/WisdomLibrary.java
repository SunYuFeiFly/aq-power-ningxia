package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 铁塔智库表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_wisdom_library")
public class WisdomLibrary extends Model<WisdomLibrary> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String openId;

    /**
     * 部门类型 1通信发展 2运行维护 3行业拓展 4能源经营 5财务管理 6人力资源 7党群纪检 8综合管理
     */
    private Integer buMenType;

    /**
     * 题库类型 0专业题库，1非专业题库
     */
    private Integer tikuType;

    /**
     * 题目类型 0选择题 1填空题 2判断题
     */
    private Integer topicType;

    /**
     * 标题
     */
    private String title;
    /**
     * 提交人
     */
    @TableField(exist = false)
    private String name;

    /**
     * 提交人
     */
    @TableField(exist = false)
    private String buMenTypeTxt;

    /**
     * 题目类型
     */
    @TableField(exist = false)
    private String topicTypeTxtStr;

    /**
     * 选项1
     */
    private String context1;

    /**
     * 选项2
     */
    private String context2;

    /**
     * 选项3
     */
    private String context3;

    /**
     * 选项4
     */
    private String context4;

    /**
     * 状态 0提交还未处理 1通过 2未通过
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 题目所属公司分类  1:自有公司题目、2:代维公司题目、3:设计和监理公司题目
     */
    private Integer companyType;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    public String getTypeTxt() {
        //1创建 2待付款 3代发货 4已发货 5 已完成 6已关闭 7待退货 8退货中 9退货成功 10已拒绝 11关闭
        String txt = null;
        if (buMenType == 1) {
            txt = "通信发展";
        } else if (buMenType == 2) {
            txt = "行业拓展";
        } else if (buMenType == 3) {
            txt = "能源经营";
        } else if (buMenType == 4) {
            txt = "运营维护";
        } else if (buMenType == 5) {
            txt = "综合管理";
        } else if (buMenType == 6) {
            txt = "财务管理";
        } else if (buMenType == 7) {
            txt = "人力资源";
        } else if (buMenType == 8 || buMenType == 9) {
            txt = "党委纪检";
        }  else if (buMenType == 10) {
            txt = "商合管理";
        } else  {
            txt = "技术支撑中心";
        }
        return txt;
    }
    public String getTopicTypeTxt() {
        String txt = null;
        if (topicType == 0) {
            txt = "选择题";
        } else if (topicType == 1) {
            txt = "填空题";
        } else {
            txt = "判断题";
        }
        return txt;
    }
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
