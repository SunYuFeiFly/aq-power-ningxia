package com.wangchen.common;

import lombok.Data;

@Data
public class ThreeUserResult {
    private ThreeUser threeUser;

    private int isNowAnswer;//当前答题人 0:不是 1:是

    private int isHouseOwner;//是否房主 1:房主  0:房间玩家

    private int is_answer;// 0默认状态 1已答对 2答错了

    private boolean isActive = true;// 是否存活

    private Integer selectAnswer;//用户选择的答案
    private String selectAnswerOpt;//值
    private String selectAnswerOpt2;//值
}
