package com.wangchen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.entity.Experience;
import com.wangchen.entity.User;
import com.wangchen.service.ExperienceService;
import com.wangchen.service.UserService;
import com.wangchen.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Slf4j                  // 日志
@Configuration          // 配置类
@EnableScheduling       // 支持定时任务类
@RequestMapping("/experienceTask")
public class ExperienceTask {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperienceService experienceService;

    /**
     * 定时计算至年底总积分 （等二期上线执行一次）
     */
//    @Scheduled(cron = "0 24 15 8 9 ?")
//    @RequestMapping("taskExperience")
    public Result taskExperience() {
        // 获取所有游戏用户集合
        List<User> userList = userService.list(new QueryWrapper<User>());
        if (null != userList && !userList.isEmpty()) {
            // 更新游戏用户截至年底所拥有总积分
            try {
                log.info("执行了游戏用户年度积分更新 定时任务");
                return userService.updateLastYearExperience(userList);
            } catch (Exception e) {
                log.info("游戏用户年度积分更新出错！");
                e.printStackTrace();
            }
        }
        return Result.newFaild("没有游戏用户年度积分更新！");
    }


    /**
     * 每天定时更新用户积分（第一次，00:10:00）
     */
    @Scheduled(cron = "0 10 0 * * ?")
    @ResponseBody
//    @RequestMapping("taskEveryDayExperience01")
    public Result taskEveryDayExperience01() {
        try {
            experienceService.updateEveryDayExperience();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工每天经验更新成功！");
    }


    /**
     * 每天定时更新用户积分（第二次，07:00:00）
     */
    // @Scheduled(cron = "0 0 7 * * ?")
    public Result taskEveryDayExperience02() {
        try {
            experienceService.updateEveryDayExperience();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工每天经验更新成功！");
    }

    /**
     * 新年凌晨对人员前一年所拥有的年内经验清零（第一次 2022.01.01 00:10:00）
     */
//    @Scheduled(cron = "0 10 0 1 1 ?")
//    @RequestMapping("taskResetPresentExperience01")
    public Result taskResetPresentExperience01() {
        try {
            experienceService.taskResetPresentExperience();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度年内总积分清零成功！");
    }

    /**
     * 新年凌晨对人员前一年所拥有的年内经验清零（第二次 2022.01.01 07:05:00）
     */
//    @Scheduled(cron = "0 5 7 1 1 ?")
    public Result taskResetPresentExperience02() {
        try {
            experienceService.taskResetPresentExperience();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.newFaild("员工新一年度年内总积分清零成功！");
    }


//    @RequestMapping("test01")
    public Result test01() throws Exception {
        List<User> list = userService.list(new QueryWrapper<User>().eq("deleted", 0).orderByDesc("id"));
        ArrayList<Experience> experienceArrayList = new ArrayList<>();
        for (User user : list) {
            if (null != user.getMobile()) {
                Experience experience = new Experience();
                experience.setOpenId(user.getOpenId());
                experience.setDayExperience("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
                experience.setMonthExperience(null);
                String time = "2021-09-27 16:39:48";
                experience.setCreateTime(DateUtil.parse(time));
                experience.setUpdateTime(DateUtil.parse(time));
                experience.setPartYear(2021);
                experience.setPartMonth(9);
                experienceArrayList.add(experience);
            }
        }
        experienceService.saveBatch(experienceArrayList);
        return Result.newSuccess();
    }
}
