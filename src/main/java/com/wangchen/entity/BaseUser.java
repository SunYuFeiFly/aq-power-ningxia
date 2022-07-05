package com.wangchen.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_base_user")
public class BaseUser extends Model<BaseUser> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 身份证号码
     */
    private String idCard;

    private String name;

    private String phone;

    private Integer sex;

    private String company;

    @TableField(strategy = FieldStrategy.IGNORED)
    private String branchOne;

    @TableField(strategy = FieldStrategy.IGNORED)
    private String branchTwo;

    @TableField(strategy = FieldStrategy.IGNORED)
    private String branchThree;

    private Integer type;//2期新增字段：1:自有员工，2:代维公司员工，3:设计和监理公司员工


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
