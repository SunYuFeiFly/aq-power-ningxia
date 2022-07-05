package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.entity.Achievement;
import com.wangchen.entity.User;
import com.wangchen.entity.UserAchievement;
import com.wangchen.service.*;
import com.wangchen.vo.AchievementVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户成就
 */

@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/achievemen")
public class UserAchievementApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private SignService signService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private ManguanService manguanService;


    /**
     * 获取用户成就信息（二期）
     * @param openId 用户id
     */
    @PostMapping("/getAchievementList")
    @ResponseBody
    public Result getAchievementList(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            List<AchievementVo> achievementVoList =  achievementService.achievementService(openId);

            return Result.newSuccess(achievementVoList);
        }catch (Exception e){
            log.error("获取用户成就信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }

}
