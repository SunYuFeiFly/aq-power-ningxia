package com.wangchen.utils;/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.utils
 * @ClassName: DateUtils
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/3 15:04
 * @Version: 1.0
 */

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DateUtils
 * @auther cheng.zhang
 * @create 2020/6/3
 *
 */
public class DateUtils {


    /**
     * 查询今天是周几
     * @return
     */
    public static Integer getWeek() {
        String week = "";
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
//        if (weekday == 1) {
//            week = "周日";
//        } else if (weekday == 2) {
//            week = "周一";
//        } else if (weekday == 3) {
//            week = "周二";
//        } else if (weekday == 4) {
//            week = "周三";
//        } else if (weekday == 5) {
//            week = "周四";
//        } else if (weekday == 6) {
//            week = "周五";
//        } else if (weekday == 7) {
//            week = "周六";
//        }
        return weekday;
    }

    public static String getZuoTianDay(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        return Constants.sdf.format(cal.getTime());
    }

    /**
     * 获取上个月的日期 yyyy-MM
     * @return
     */
    public static String getShangMonthDate(){
        LocalDate today = LocalDate.now();
        today = today.minusMonths(1);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM");
        return formatters.format(today);
    }




    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,2);
        Date d=cal.getTime();
        System.out.println(Constants.sdf.format(d));
//        System.out.println(getWeek());
    }
}