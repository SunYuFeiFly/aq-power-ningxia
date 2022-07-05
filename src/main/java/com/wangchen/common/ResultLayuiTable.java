package com.wangchen.common;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
* layui table 返回对象
* @Author: huangyang
* @Date: 2019/7/15 15:13
*/
@Data
public class ResultLayuiTable extends Result {

    // total
    private long count;

    // 结果集
    private Object data;

    public ResultLayuiTable(String code, String message){
        super(code, message);
    }

    public ResultLayuiTable(String code, String message, Object result){
        super(code, message, result);
        this.data = result;
    }

    public ResultLayuiTable(String code, String message, long count, Object result){
        super(code, message, result);
        this.count = count;
        this.data = result;
    }

    /**
    * 成功 有参
    * @Param: [data 结果集]
    * @Return: com.wangchen.plantdoctor.common.ResultLayuiTable
    * @Author: huangyang
    * @Date: 2019/7/15 15:19
    */
    public static ResultLayuiTable newSuccess(Object data){
        return new ResultLayuiTable(BussinessErrorMsg.SUCCESS.getCode(), BussinessErrorMsg.SUCCESS.getMsg(), data);
    }

    /**
    * 成功 有参
    * @Param: [count 表格总数, data 结果集]
    * @Return: com.wangchen.plantdoctor.common.ResultLayuiTable
    * @Author: huangyang
    * @Date: 2019/7/15 15:20
    */
    public static ResultLayuiTable newSuccess(long count, Object data){
        return new ResultLayuiTable(BussinessErrorMsg.SUCCESS.getCode(), BussinessErrorMsg.SUCCESS.getMsg(), count, data);
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
}
