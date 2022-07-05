package com.wangchen.utils;

import cn.hutool.core.date.DateUnit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static final String defaultFormat = "yyyy-MM-dd HH:mm:ss";

    public static String format(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(defaultFormat);
        return sdf.format(date);
    }

    public static String format(Date date, String formate){
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        return sdf.format(date);
    }

    public static Date parse(String value) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat(defaultFormat);
        return sdf.parse(value);
    }

    public static String longTimeToDay(Long ms){
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
//        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
//        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
//        int count = 0;
        if(day >= 0) {
            sb.append(day+"天");
//            count++;
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }else{
            sb.append("0小时");

        }
//        if(count < 2 && minute > 0) {
//            sb.append(minute+"分");
//        }
//        if(second > 0) {
//            sb.append(second+"秒");
//        }
//        if(milliSecond > 0) {
//            sb.append(milliSecond+"毫秒");
//        }
        return sb.toString();
    }

}
