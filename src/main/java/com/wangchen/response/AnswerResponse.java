package com.wangchen.response;

import com.wangchen.entity.BranchTopic;
import com.wangchen.vo.OneVsOneTopicVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class    AnswerResponse {

    // 题目列表
    private List<OneVsOneTopicVo> answerQuestionResponses;

}
