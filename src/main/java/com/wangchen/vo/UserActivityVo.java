package com.wangchen.vo;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 用户活动赛排行榜
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Data
public class UserActivityVo extends Model<UserActivityVo> {

    private Integer id;
    /**
     * 排行
     */
    private Integer rankNo;
    /**
     * 用户名称
     */
    private String name;

    /**
     * 分数
     */
    private Integer score;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
}
