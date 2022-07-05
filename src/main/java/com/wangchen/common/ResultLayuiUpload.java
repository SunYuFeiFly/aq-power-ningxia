package com.wangchen.common;

import lombok.Data;

/**
* layui upload 返回对象
* @Author: huangyang
* @Date: 2019/7/15 15:21
*/
@Data
public class ResultLayuiUpload {

    // 编码
    private String code;
    // 信息
    private String msg;
    // 结果集
    private Object data;

    public ResultLayuiUpload(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultLayuiUpload(String code, String msg, Object data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
    * 是否成功
    * @Return: boolean
    * @Author: huangyang
    * @Date: 2019/7/15 15:22
    */
    public boolean isSuccess(){
        return "0".equals(code);
    }

    /**
    * 成功
    * @Param: [data 结果集]
    * @Return: com.wangchen.plantdoctor.common.ResultLayuiUpload
    * @Author: huangyang
    * @Date: 2019/7/15 15:23
    */
    public static ResultLayuiUpload newSuccess(Object data){
        return new ResultLayuiUpload("0", "success", data);
    }

    /**
    * 失败
    * @Param: [msg 信息]
    * @Return: com.wangchen.plantdoctor.common.ResultLayuiUpload
    * @Author: huangyang
    * @Date: 2019/7/15 15:23
    */
    public static ResultLayuiUpload newFaild(String msg){
        return new ResultLayuiUpload("9999", msg);
    }
}
