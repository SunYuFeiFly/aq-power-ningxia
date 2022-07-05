package com.wangchen.response;

import lombok.Data;

import java.util.List;

@Data
public class AnswerQuestionResponse {
    // 题目ID
    private Long question_id;
    // 题目类型：1=选择题，2=填空题 ，3判断题
    private int question_type;
    // 题干
    private String question_description;
    // 题目所属部门。一套题库中存在多个维度
    private String subject_name;
    // 选项列表
    private List<AnswerOptionResponse> answerOptionResponses;
}
