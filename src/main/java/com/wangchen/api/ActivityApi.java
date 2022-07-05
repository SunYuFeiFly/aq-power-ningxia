package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.mapper.ActivityTopicMapper;
import com.wangchen.service.*;
import com.wangchen.utils.ComputeUtils;
import com.wangchen.utils.DateUtil;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.ActivityTopicVo;
import com.wangchen.vo.ActivityVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @ProjectName: 活动赛
 * @Package: com.wangchen.api
 * @ClassName: ActivityApi
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/19 14:41
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@Controller
@Slf4j
@RequestMapping("/api/activity")
public class ActivityApi {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityOptionService activityOptionService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ActivityTopicMapper activityTopicMapper;

    @Autowired
    private ManguanService manguanService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private ComputeUtils computeUtils;

    @Autowired
    private UserActivityRankService userActivityRankService;

    @Autowired
    private UserActivityTopicLogService userActivityTopicLogService;


    /**
     * 获取活动赛信息(二期)
     * @param openId 用户id
     * @return 活动赛信息
     */
    @ResponseBody
    @PostMapping(value = "/getActivityInfo")
    public Result singleGameLog(@RequestParam(value = "openId", required = false, defaultValue = "") String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }

            // 查看活动赛信息
            Result result = activityService.getActivityInfo(openId);
            return result;
        }catch (Exception e){
            log.error("查看活动赛信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 获取题目信息（二期）
     * @param openId 用户id
     * @param companyType 所属公司分类
     * @return 题目集合
     */
    @ResponseBody
    @PostMapping(value = "/getActivityTopicInfo")
    public Result getActivityTopicInfo(@RequestParam(value = "openId", required = false, defaultValue = "") String openId,
                                       @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }

            // 获取活动赛题目信息
            Result result = activityService.getActivityTopicInfo(openId);
            return result;
        }catch (Exception e){
            log.error("获取答题挑战题目信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 活动赛用户提交答案(二期)
     * @param openId 用户id
     * @param score 得分
     * @param activityId 活动赛id
     * @return 提交成功与否
     */
    @ResponseBody
    @PostMapping(value = "/answerActivity")
    public Result answerActivity(@RequestParam(value = "openId", required = false, defaultValue = "") String openId,
                                 @RequestParam(value = "score", required = false, defaultValue = "") Integer score,
                                 @RequestParam(value = "activityId", required = false, defaultValue = "") Integer activityId){
        try {
            log.info("answerActivity、openId:{} 、score:{} 、activityId:{}",openId,score,activityId);
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为：" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            if(null == score || null == activityId){
                return Result.newFaild("参数为空");
            }
            // 活动赛用户提交答案
            Result result = activityService.answerActivity(openId, score, activityId);
            return result;
        }catch (Exception e){
            log.error("活动赛提交结果出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }

}