package com.wangchen;


import com.wangchen.api.UserHonorApi;
import com.wangchen.common.Result;
import com.wangchen.service.UserHonorService;
import com.wangchen.service.UserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 每年元旦（1月1日，凌晨、清晨两次）员工等级、段位清零
 * @Description: LiJian
 * @Date: 2021/09/10
 */

@Slf4j
@Configuration
@EnableScheduling
@RequestMapping("/ResetTask")
public class ResetTask {

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserLevelService userLevelService;


    /**
     * 每年元旦（1月1日，凌晨）员工段位清零
     */
//    @Scheduled(cron = "0 15 0 1 1 ?")
//    @RequestMapping("taskResetHonnor01")
    public Result taskResetHonnor01() {
        try {
            userHonorService.taskResetHonnor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度等级、段位清零成功！");
    }

    /**
     * 每年元旦（1月1日，清晨）员工段位清零
     */
//    @Scheduled(cron = "0 15 7 1 1 ?")
    public Result taskResetHonnor02() {
        try {
            userHonorService.taskResetHonnor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度等级、段位清零成功！");
    }

    /**
     * 每年元旦（1月1日，凌晨）员工等级清零
     */
//    @Scheduled(cron = "0 20 0 1 1 ?")
//    @RequestMapping("taskResetLevel01")
    public Result taskResetLevel01() {
        try {
            userLevelService.taskResetLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度等级、段位清零成功！");
    }

    /**
     * 每年元旦（1月1日，清晨）员工等级清零
     */
//    @Scheduled(cron = "0 20 7 1 1 ?")
    public Result taskResetLevel02() {
        try {
            userLevelService.taskResetLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度等级、段位清零成功！");
    }

}
