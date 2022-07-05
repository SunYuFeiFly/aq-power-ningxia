package com.wangchen.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.utils.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 活动赛表
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Data
public class ActivityVo {

    private Integer id;

    /**
     * 活动赛名称
     */
    private String name;


    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;


}
