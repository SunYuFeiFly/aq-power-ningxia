package com.wangchen.vo;

/**
 * 团对赛题目信息
 * @Package: com.wangchen.vo
 * @ClassName: ThreeTeamTopicVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/7/13 13:44
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.BranchOption;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OneVsOneTopicVo {

    private Integer id;

    /**
     * A类部门类型： 1:通信发展 2:行业拓展 3:能源经营 4:运营维护 5:综合管理 6:财务管理 7:人力资源 8:纪委办公室 9:党群工作组 10:商合中心 11:技术支撑中心
     */
    private Integer type;

    /**
     * 部门名称
     */
    private String typeName;

    /**
     * 题目类型 0选择题 1填空题 2判断题
     */
    private Integer topicType;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 正确选项id 对应部门答案表id
     */
    private Integer correctOptionId;

    /**
     * 问题解析
     */
    private String parse;

    /**
     * 上传图片地址
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String imageUrl;

    /**
     * 上传视频地址
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private String videoUrl;

    /**
     * 题目所属公司类型(1:自有公司，2:代维公司，3:设计和监理公司)
     */
    private Integer companyType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    List<BranchOption> optionList;

    /**
     * 当前是第几题
     */
    private Integer rankNo;
}