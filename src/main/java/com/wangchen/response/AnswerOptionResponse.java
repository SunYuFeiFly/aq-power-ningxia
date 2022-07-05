package com.wangchen.response;

import lombok.Data;

@Data
public class AnswerOptionResponse {
    // 选项所属题目ID
    private long answer_question_id;
    // 选项ID
    private long answer_id;
    // 选项正确与否，0=错误，1=正确
    private int answer_isright;
    // 选项内容
    private String answer_description;
}
