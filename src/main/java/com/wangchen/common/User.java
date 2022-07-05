package com.wangchen.common;

import lombok.Data;

@Data
public class User {
    private String nickName;
    private String avatar;
    private String openId;
    private int honorId;
    private String honorName;
    private String achievementName;
    private int level;
    private int type;
}
