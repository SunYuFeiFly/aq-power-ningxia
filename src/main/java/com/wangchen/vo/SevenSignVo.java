package com.wangchen.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户七天签到表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-03
 */
@Data
public class SevenSignVo{
    /**
     * 是否是今天
     */
    private int isNow;

    /**
     * 0 显示X  1显示√  2 不显示
     */
    private Integer isFlag;

    /**
     * 当日获得塔币数量
     */
    private Integer coin;

    /**
     * 当日获得经验数量
     */
    private Integer experience;
}
