package com.wangchen.common;

import lombok.Data;

@Data
public class UserResult {

    /** -1:默认,未答题 1:已答题 */
    private int is_answer;

    /** 用户 */
    private User user;

    /** 分数 */
    private int score;

    /** 用户选择的答案 */
    private String selectAnswer;

    /** 值 */
    private String selectAnswerOpt;

    /** 值，暂时没用? */
    private String selectAnswerOpt2;

    /** 是否存活 */
    private boolean isActive = true;

}
