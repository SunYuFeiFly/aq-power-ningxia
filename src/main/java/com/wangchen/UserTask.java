package com.wangchen;

import com.wangchen.common.Result;
import com.wangchen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 主要功能是每天凌晨定时计算各类排名前，清除不是公司游戏成员的体验账号，将此类账号排除在公司各类排名中
 */

@Slf4j
@Configuration
@EnableScheduling
@RequestMapping("/userTask")
public class UserTask {

    @Autowired
    private UserService userService;

    /**
     * 每天凌晨前清除游戏体验账号（每天23:58:00 进行清除）
     */
    @Scheduled(cron = "0 58 23 * * ?")
    public Result taskEveryDayExperience02() {
        try {
            // 定时清除体验账户（体验用户并必回常见其他相关联数据）
            userService.deleteExperienceUser();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工定时清除体验用户账号成功！");
    }




}
