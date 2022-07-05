package com.wangchen.utils;

import java.util.List;

public class StringUtils {

    /**
    * is empty object
    *
    * @Param: [value]
    * @Return: boolean
    * @Author: huangyang
    * @Date: 2019/7/15 15:58
    */
    public static boolean isEmpty(Object value){
        if (value == null){
            return value == null;
        }else if (value instanceof Integer){
            return ((Integer) value) == 0;
        }else if (value instanceof Long){
            return ((Long) value) == 0;
        }else if (value instanceof String){
            return "".equals(value) || ((String) value).length() == 0;
        }else if (value instanceof List){
            return ((List) value).size() == 0;
        }
        return false;
    }
}
