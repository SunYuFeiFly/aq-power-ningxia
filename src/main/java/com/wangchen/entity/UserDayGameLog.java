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
 * 因为每日答题一天只能进行一次,所以这个表在用户获取每日答题的题目的时候就记录下来，这样的话就可以保证一天只玩一次（如果用户答了一半也就是5道题退出了，我们捕捉正常能捕捉到的退出情况，记录下来，如果是什么关机的话， 那就不记录了）
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("aq_user_day_game_log")
public class UserDayGameLog extends Model<UserDayGameLog> {

private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String openId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dayGameDate;

    //分数
    private Integer score;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    private Integer deleted;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
