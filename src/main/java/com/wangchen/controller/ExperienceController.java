package com.wangchen.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.entity.Experience;
import com.wangchen.entity.User;
import com.wangchen.service.ExperienceService;
import com.wangchen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  用户经验明细表
 * </p>
 *
 * @author LiJian
 * @since 2021-09-09
 */

@Slf4j
@Controller
@RequestMapping("/system/experience")
public class ExperienceController {

    @Autowired
    private ExperienceService experienceService;

    @Autowired
    private UserService userService;

    /**
     * 第一次创建用户-经验对照表数据使用
     */
//    @RequestMapping("updateEx")
    public void updateEx() {
        // 获取当前时间
        Date date = DateUtil.date();
        // 获取日期
        int day = DateUtil.dayOfMonth(date);
        //获得月份，从0开始计数
        int month = DateUtil.month(date) + 1;
        //获得年的部分
        int year = DateUtil.year(date);

        ArrayList<Experience> experiences = new ArrayList<>();
        // 获取所有用户
        List<User> users = userService.list(new QueryWrapper<User>().eq("deleted", 0));
        for (User user : users) {
            Experience experience = new Experience();
            experience.setOpenId(user.getOpenId());
            experience.setMonthExperience(null);
            experience.setCreateTime(DateUtil.offsetDay(new Date(),-1));
            experience.setUpdateTime(DateUtil.offsetDay(new Date(),-1));
//            experience.setCreateTime(new Date());
//            experience.setUpdateTime(new Date());
            experience.setPartMonth(month);
            experience.setPartYear(year);
            String dayExperience = "0";
            if (day != 1) {
                for (int i = 1; i < day-1; i++) {
                    dayExperience += ",0";
                }
            }
            experience.setDayExperience(dayExperience);
            experienceService.save(experience);
        }
    }





}
