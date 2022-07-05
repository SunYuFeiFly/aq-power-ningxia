package com.wangchen.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.Achievement;
import com.wangchen.entity.AlertTips;
import com.wangchen.entity.User;
import com.wangchen.service.AchievementService;
import com.wangchen.service.AlertTipsService;
import com.wangchen.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 弹出框提示恭喜用户获得 称号,成就，活动赛第几名，团队赛第几名等等
 * @Package: com.wangchen.api
 * @ClassName: RankApi
 * @Author: 2
 * @Description: zhangcheng
 * @Date: 2020/6/23 14:20
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/alerttips")
public class AlertTipsApi {

    @Autowired
    private UserService userService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 查询弹出框提示恭喜用户获得 称号,成就，活动赛第几名，团队赛第几名等等
     * @param openId 用户id
     */
    @PostMapping("/getAlertTipsList")
    @ResponseBody
    public Result getAlertTipsList(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }

            List<AlertTips> alertTipsList = alertTipsService.list(new QueryWrapper<AlertTips>().eq("open_id",openId).eq("status",0));
            // 获取所有成就集合
            List<Achievement> achievementList = achievementService.list(new QueryWrapper<>());
            Map<String, List<Achievement>> achievementMap = new HashMap<>();
            if (CollUtil.isNotEmpty(achievementList)) {
                achievementMap = achievementList.stream().collect(Collectors.groupingBy(Achievement::getName));
            }
            for(AlertTips alertTips : alertTipsList){
                alertTips.setStatus(1);
                alertTips.setUpdateTime(new Date());
                if (!StringUtils.isEmpty(alertTips.getAchievementName())) {
                    List<Achievement> achievements = achievementMap.get(alertTips.getAchievementName());
                    Integer type = achievements.get(0).getType();
                    String achievementUrl = Constants.ACHIEVEMENT_IMAGE_ADDRESS + "image_0"+ type +".png";
                    alertTips.setAchievementUrl(achievementUrl);
                } else {
                    alertTips.setAchievementUrl(null);
                }
                alertTipsService.updateById(alertTips);
            }
            return Result.newSuccess(alertTipsList);
        }catch (Exception e){
            log.error("用户获取用户获得称号,成就等信息出错，错误信息: {}", e);
            return Result.newFail();
        }
    }



}