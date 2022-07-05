package com.wangchen.vo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.BranchOption;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.vo
 * @ClassName: BranchTopicVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/10 14:40
 * @Version: 1.0
 */
@Data
public class BranchTopicVo {

    private Integer id;

    /**
     * A类部门类型： 1:通信发展 2:运行维护 3:行业拓展 4:能源经营 5:财务管理 6:人力资源 7:党群纪检 8:综合管理
     * B类部门类型： 101:代维
     * C类部门类型： 201:地勘 202:监理 203:设计 204:施工 205:土建 206:电力 207:机房 208:动力配套 209:电池管理 210:天线 211:门禁 212:其它
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
    private String correctParse;

    /**
     * 提示
     */
    private String point;

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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private List<BranchOption> branchOptionList;
}