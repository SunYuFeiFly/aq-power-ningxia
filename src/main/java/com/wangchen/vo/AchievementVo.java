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
public class AchievementVo {

    private Integer id;

    /**
     * 成就类型
     */
    private Integer type;

    /**
     * 成就名称
     */
    private String name;

    /**
     * 成就描述
     */
    private String remarks;

    /**
     * 成就点数
     */
    private Integer num;

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
     * 是否是最后获取得 0不是 1是
     */
    private Integer isZuiHou;

    /**
     * 达成条件
     */
    private String condition;

    /**
     * 未达成当前成就显示图片
     */
    private String backImageUrl;

    /**
     * 达成当前成就显示图片
     */
    private String imageUrl;
}