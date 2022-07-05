package com.wangchen.common;

public enum BusinessErrorMsg {
    SUCCESS("0000", "SUCCESS"),
    FAILD("9999", "FAIL"),
    NEED_RELOGIN("9000", "登录过期，请重新登录"),
    SIGN_FAIL("9997", "登录过期，请重新登录"),
    USER_NOT_ROOT_FAIL("9998", "用户未授权"),
    BUSINESS_ERROR("5000", "服务器繁忙，请稍后再试"),
    USER_NAME_NULL("1001", "用户名不能为空"),
    PASSWROD_NULL("1002", "密码不能为空"),
    OPEN_ID_IS_NULL("1003", "openId不能为空"),
    PASSWORD_ERROR("4000", "账号或密码错误"),
    USER_TOKEN_IS_NULL("8001", "用户不存在"),
    ROOM_IS_NULL("8002", "房间已经不存在"),
    ROOM_YI_JING_MAN_YUAN("8003", "房间已经满员"),


    ANSWER("2000","SUCCESS"),                           // 题目结果
    ANSWER_MY_RESULT("1000","SUCCESS"),                 // 答题结果
    ANSWER_OTHER_RESULT("1000","SUCCESS"),              // 答题结果
    ANSWER_PERSON("1001","SUCCESS"),                    // 匹配结果
    QUESTION_NOT_MATCH("8000","重复提交答案"), // 选项重复
    QUESTION_IS_NULL("7000","题目为null"), // 题目为空
    PLAYER_LEAVE("5000","有玩家离开游戏"), // 题目为空

    TASKID_ERROR("5002", "签到任务查询不存在"),

    PARAM_IS_NULL("5003", "有必填参数为空"),
    FORUM_ID_IS_NULL("5004", "未获取到论坛编号"),
    FORUM_IS_NULL("5005", "未查询到论坛"),
    FORUM_NOT_TONG_GUO_NULL("5008", "论坛还没审核通过啊"),
    FORUM_IS_GET_ONE("5009", "该帖子已经领取过了"),
    COMMENT_ID_IS_NULL("5006", "未获取到评论编号"),
    COMMENT_IS_NULL("5007", "未查询到评论信息"),
    ;

    BusinessErrorMsg(String code, String msg){
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
