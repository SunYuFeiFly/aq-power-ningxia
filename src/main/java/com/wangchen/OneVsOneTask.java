package com.wangchen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.UserLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * 查询是否有获得单挑之王成就的用户 (1v1赛中一天内与不同的人比赛连续获胜10场)
 * @ProjectName: aq-power
 * @Package: com.wangchen
 * @Description: zhangcheng
 * @Date: 2020/7/2 16:10
 * @Version: 1.0
 */
@Slf4j                  // 日志
@Configuration          // 配置类
@EnableScheduling       // 支持定时任务类
@RequestMapping("/OneVsOneTask")
public class OneVsOneTask {

    @Autowired
    private UserService userService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private AlertTipsService alertTipsService;

    /**
     * 查询是否有获得单挑之王成就的用户
     */
    @Scheduled(cron = "0 30 1 * * ? ")
//    @Scheduled(cron = "*/5 * * * * ?")
    @RequestMapping("taskActivity")
    public void taskActivity() {
        try{ //DateUtils.getZuoTianDay()
           List<String> openIdList = userOneVsOneLogService.getGameGtTenNumUser(DateUtils.getZuoTianDay());

           for(String openId : openIdList){
               int ying = 0;
               List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.getGameLogByOpenId(DateUtils.getZuoTianDay(),openId);

               for(UserOneVsOneLog userOneVsOneLog : userOneVsOneLogList){
                    if(openId.equals(userOneVsOneLog.getRoomOpenId())){
                        if(2 != userOneVsOneLog.getIsWin().intValue()){
                            ying ++;
                        }else{
                            ying = 0;
                        }
                    }else{
                        if(1 != userOneVsOneLog.getIsWin().intValue()){
                            ying ++;
                        }else{
                            ying = 0;
                        }
                    }

                    if(ying>= 10){
                        break;
                    }
               }

               if(ying>= 10){
                   User user = userService.getOne(new QueryWrapper<User>().eq("open_id",openId));

                   Achievement achievement = achievementService.getOne(new QueryWrapper<Achievement>().eq("id",6));

                   UserAchievement userAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>()
                           .eq("open_id",openId).eq("achievement_id",achievement.getId()));

                   if(null == userAchievement){
                       //用户总成就值添加
                       user.setAllAchievement(user.getAllAchievement()+achievement.getNum());
                       user.setUpdateDate(new Date());
                       userService.updateById(user);

                       //用户成就表里加数据
                       userAchievement = new UserAchievement();
                       userAchievement.setOpenId(openId);
                       userAchievement.setAchievementId(achievement.getId());
                       userAchievement.setAchievementName(achievement.getName());
                       userAchievement.setCreateTime(new Date());
                       userAchievementService.save(userAchievement);

                       //每日公告上面的恭喜
                       HotLog hotLog1 = new HotLog();
                       hotLog1.setOpenId(openId);
                       hotLog1.setRemarks("恭喜"+user.getName()+"获得了"+achievement.getName()+"成就");
                       hotLog1.setCreateDate(new Date());
                       hotLogService.save(hotLog1);

                       //弹框提示
                       AlertTips alertTips = new AlertTips();
                       alertTips.setOpenId(openId);
                       alertTips.setAchievementId(achievement.getId());
                       alertTips.setAchievementName(achievement.getName());
                       alertTips.setType(1);
                       alertTips.setStatus(0);
                       alertTips.setCreateTime(new Date());
                       alertTipsService.save(alertTips);
                   }
               }
           }


        }catch (Exception e){
            log.error("查询是否有获得单挑之王成就的用户、 错误信息: {}",e);
        }

    }

}