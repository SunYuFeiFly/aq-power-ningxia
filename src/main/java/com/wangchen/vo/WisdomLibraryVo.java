package com.wangchen.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wangchen.entity.BranchOption;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ProjectName: 铁塔智库查询题目信息
 * @Package: com.wangchen.vo
 * @ClassName: BranchTopicVo
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/10 14:40
 * @Version: 1.0
 */
@Data
public class WisdomLibraryVo {
    /**
     * 问题解析
     */
    private String correctParse;

    private List<String> optionList;
}