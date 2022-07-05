package com.wangchen.common;

public enum BussinessErrorMsg {
    SUCCESS("0000", "SUCCESS"),
    FAILD("9999", "FAIL"),
    SIGN_FAIL("2100", "您还没有体验资格"),
    NEED_RELOGIN("9000", "登录过期，请重新登录"),
    BUSINESS_ERROR("5000", "服务器繁忙，请稍后再试"),
    NOT_FOUND_GAME("4000", "没有游戏记录"),
    NOT_FOUND_GAMES("4001", "地址已存在"),
    BUSINESS_FIELD_NULL("9002", "页面可能走丢了，请稍后试试"),
    BUSINESS_QUALI("8888", "抱歉，您没有抽奖资格哦"),
    BUSINESS_FILE_NULL("9001",  "网络开小差了，刷新看看"),
    RED_PACK_LIMIT("6000",  "红包次数超限"),


    PLAYER_LEAVE("9993","有玩家离开游戏"),
    PVP_NOT_READY("9994","团队中有玩家今天已经玩过游戏啦"),
    PVP_ROOM_IS_ONE("9995","只有自己一个人不能开始游戏"),
    PVP_ROOM_IS_THREE("9996","必须要有三个成员才能开始游戏"),
    PVP_ROOM_IS_ONE_VS_ONE("9997","您一天只能跟该好友对战一次"),
    PVP_ROOM_IS_HAS_ANSWER("9998","请勿重复答题"),
    PVP_ROOM_IS_OUT_ROOM("9991","您被请出房间了"),
    PVP_ROOM_IS_PEIPEI_ZHONG_ROOM("9000","房主正在匹配中，抱歉您来晚了"),

    USER_TOKEN_IS_NULL("8001", "用户不存在"),
    ROOM_IS_NULL("8002", "房间已经不存在"),
    ROOM_YI_JING_MAN_YUAN("8003", "房间已经满员"),
    ROOM_YI_JING_ROOM_LI("8004", "您已经在该房间里了"),

    PONG_ONE("8008", "使用了心跳code"),


    CITY_NOT_INFO("9996","没有城市版本信息"),

    ANSWER("2000","SUCCESS"),              // 题目结果
//    ANSWER_START("2001","SUCCESS"),              // 开始游戏
    QUESTION_IS_NULL("9992","题目为null"), // 题目为空
    QUESTION_YOU_REN_DA_DUI("9995","题目已经有人答对了"), // 题目已经有人答对了
    ANSWER_MY_RESULT("1000","SUCCESS"),// 答题结果
    ANSWER_END("3000", "SUCCESS"), // 答题结束
    ANSWER_BENJU_END("4000", "SUCCESS"), // 答题结束
    ;


    BussinessErrorMsg(String code, String msg){
        this.code = code;
        this.msg = msg;
    }
    private String code;
    private String msg;
    public void setCode(String code){
        this.code = code;
    }
    public String getCode(){
        return code;
    }
    public void setMsg(String msg){
        this.msg = msg;
    }
    public String getMsg(){
        return msg;
    }
}
