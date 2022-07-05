package com.wangchen.common;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class Result {
    private String code;    //返回码
    private String msg;     //返回信息
    private Object data;    //返回结果对象

    public Result(){}

    public Result (String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Result (String code, String msg, Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    /**
     * 是否成功
     * @Return: boolean
     * @Author: huangyang
     * @Date: 2019/8/1 17:25
     */
    public boolean isSuccess(){
        return BusinessErrorMsg.SUCCESS.getCode().equals(code);
    }

    /**
     * 功能描述: 成功 无参
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:05
     */
    public static Result newSuccess(){
        return new Result(BusinessErrorMsg.SUCCESS.getCode(), BusinessErrorMsg.SUCCESS.getMsg());
    }

    /**
     * 功能描述: 成功 有参
     *
     * @Param: [result 结果对象]
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:06
     */
    public static Result newSuccess(Object data){
        return new Result(BusinessErrorMsg.SUCCESS.getCode(), BusinessErrorMsg.SUCCESS.getMsg(), data);
    }

    public static Result newSuccess(BusinessErrorMsg businessErrorMsg, Object data){
        return new Result(businessErrorMsg.getCode(), businessErrorMsg.getMsg(), data);
    }

    /**
     * 功能描述: 失败 无参
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:07
     */
    public static Result newFail(){
        return new Result(BusinessErrorMsg.FAILD.getCode(), BusinessErrorMsg.FAILD.getMsg());
    }

    /**
     * 功能描述: 失败 有参
     *
     * @Param: [code 编码, message 信息]
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:08
     */
    public static Result newFail(String code, String msg){
        return new Result(code, msg);
    }

    /**
     * 功能描述: 重新登录
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:08
     */
    public static Result reLogin(){
        return newFail(BusinessErrorMsg.SIGN_FAIL.getCode(), BusinessErrorMsg.SIGN_FAIL.getMsg());
    }

    /**
     * 功能描述: 失败 有参
     *
     * @Param: [message 失败信息]
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:09
     */
    public static Result newFail(String msg){
        return newFail(BusinessErrorMsg.FAILD.getCode(), msg);
    }

    /**
     * 功能描述: 失败 有参
     *
     * @Param: [businessErrorMsg 业务异常枚举]
     * @Return: Result
     * @Author: huangyang
     * @Date: 2019/7/15 15:10
     */
    public static Result newFail(BusinessErrorMsg businessErrorMsg){
        return new Result(businessErrorMsg.getCode(), businessErrorMsg.getMsg());
    }

    public static Result signFail(){
        return newFail(BusinessErrorMsg.SIGN_FAIL);
    }

    /**
     * 失败
     * @Param: [message 失败信息]
     * @Return: com.wangchen.plantdoctor.common.ResultLayuiTable
     * @Author: huangyang
     * @Date: 2019/7/15 15:20
     */
    public static ResultLayuiTable newFaild(String message){
        return new ResultLayuiTable(BussinessErrorMsg.FAILD.getCode(), StringUtils.isEmpty(message) ? BussinessErrorMsg.FAILD.getMsg() : message);
    }

    public static Result SIGN_FAIL(){
        return new Result(BussinessErrorMsg.SIGN_FAIL.getCode(), BussinessErrorMsg.SIGN_FAIL.getMsg());
    }
}
