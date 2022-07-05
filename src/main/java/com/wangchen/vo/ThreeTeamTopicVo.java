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
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.BranchOption;
import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ThreeTeamTopicVo {

    private Integer id;

    /**
     * 部门类型 1通信发展 2运行维护 3行业拓展 4能源经营 5财务管理 6人力资源 7党群纪检 8财务管理
     */
    private Integer type;

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