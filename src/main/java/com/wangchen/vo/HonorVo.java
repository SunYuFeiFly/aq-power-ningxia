package com.wangchen.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
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
public class HonorVo {

    private Integer id;

    /**
     * 称号名称
     */
    private String name;

    private String remarks;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 是否拥有 0未拥有 1已拥有
     */
    private Integer isHas;

    /**
     * 距离达到本称号所需经验条件
     */
    private String condition;

}