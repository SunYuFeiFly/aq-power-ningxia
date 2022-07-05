package com.wangchen.common;

import lombok.Data;

@Data
public class ThreeUser {
    private String nickName;//微信平台获得的昵称
    private String avatar;//微信平台获得的用户头像url
    private String openId;//微信平台用户唯一id：openid
    private String honorName;//游戏获得的荣誉等级名字

    private int level;
}
