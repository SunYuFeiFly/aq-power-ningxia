package com.wangchen;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.UserLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动赛定时任务：发放前一天的活动赛的奖励信息、定时检测活动赛开始结束时间，更新活动赛状态
 */

@Slf4j
@Configuration
@EnableScheduling
@RequestMapping("/activityTask")
public class ActivityTask {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private HonorService honorService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserActivityRankService userActivityRankService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private ActivityTopicService activityTopicService;

    @Autowired
    private ActivityOptionService activityOptionService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    /**
     * 发放前一天的活动赛的奖励信息
     * （二期主要变更部分为：排名得分 -> 10题全对10经验5塔币，6~9题5经验3塔币，5题及以下2经验1塔币，自动功能停用）
     */
    // @Scheduled(cron = "0 30 0 * * ? ")
    // @Scheduled(cron = "*/5 * * * * ?")
    public void taskActivity() {
        try{
            List<UserActivity> userActivityList = userActivityService.list(new QueryWrapper<UserActivity>()
                    .eq("create_date",DateUtils.getZuoTianDay()).isNotNull("submit_time").orderByDesc("score").orderByAsc("submit_time"));
            Map<Integer,List<UserActivity>> map = new HashMap<Integer,List<UserActivity>>();
            if(CollectionUtils.isEmpty(userActivityList)){
                return;
            }
            List<Integer> scoreList = new ArrayList<Integer>();
            for(UserActivity userActivity:userActivityList){
                if(map.containsKey(userActivity.getScore())){
                    map.get(userActivity.getScore()).add(userActivity);
                }else{
                    scoreList.add(userActivity.getScore());
                    List<UserActivity> userActivityList1 = new ArrayList<UserActivity>();
                    userActivityList1.add(userActivity);
                    map.put(userActivity.getScore(),userActivityList1);
                }
            }
            for(int i = 1; i<=scoreList.size(); i++){
                List<UserActivity> userActivityListRank = map.get(scoreList.get(i-1));
                //获取分数对应的经验值和塔币
                Integer[] jiangli = null;
                if(i < 11){
                    jiangli = Constants.activitySocreMap.get(i);
                }else{
                    jiangli = Constants.activitySocreMap.get(0);
                }
                for(UserActivity userActivity : userActivityListRank){
                    User user = userService.getUserByOpenId(userActivity.getOpenId());
                    UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",userActivity.getOpenId()));
                    //添加一下排行记录表
                    UserActivityRank userActivityRank = new UserActivityRank();
                    userActivityRank.setRankDate(userActivity.getCreateDate());
                    userActivityRank.setRankNo(i);
                    userActivityRank.setScore(userActivity.getScore());
                    userActivityRank.setOpenId(userActivity.getOpenId());
                    userActivityRank.setName(user.getName());
                    userActivityRank.setAvatar(user.getAvatar());
                    userActivityRank.setLevelNo(userLevel.getLevelId()-1);
                    userActivityRank.setCompanyName(user.getCompanyName());
                    List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>()
                            .eq("open_id",userActivity.getOpenId()).orderByDesc("create_time"));
                    if(CollectionUtils.isNotEmpty(userHonorList)){
                        userActivityRank.setHonorNo(userHonorList.get(0).getHonorId());
                        userActivityRank.setHonorName(userHonorList.get(0).getHonorName());
                    }else{
                        userActivityRank.setHonorNo(0);
                        userActivityRank.setHonorName("暂未获得段位");
                    }
                    List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>()
                            .eq("open_id",userActivity.getOpenId()));
                    if(CollectionUtils.isNotEmpty(userBranchList)){
                        userActivityRank.setBranchName(userBranchList.get(0).getBranchName());
                    }else{
                        userActivityRank.setBranchName("暂未拥有部门");
                    }
                    userActivityRank.setGetExp(jiangli[0].intValue());
                    userActivityRank.setGetCoin(jiangli[1].intValue());
                    userActivityRank.setCreateTime(new Date());
                    userActivityRankService.save(userActivityRank);
                    //TODO 排行记录表结束

                    //用户当前等级经验峰值
                    Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);
                    //当前分数加本次应该加的分数还小于本等级的峰值， 那就不用升级
                    if(value > (user.getAllExperience().intValue()+jiangli[0].intValue())){
                        //更新最后添加经验时间
                        userLevel.setUpdateTime(new Date());
                        userLevelService.updateById(userLevel);
                        //经验值增加
                        // user.setAllExperience(user.getAllExperience().intValue()+jiangli[0].intValue());
                        user.addAllExperience(jiangli[0].intValue());
                        //塔币增加
                        user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
                        user.setUpdateDate(new Date());
                        userService.updateById(user);
                    }else{
                        if(userLevel.getLevelId().intValue() != 101){
                            //塔币增加
                            user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
                            //总经验增加
                            // user.setAllExperience(user.getAllExperience() + jiangli[0].intValue());
                            user.addAllExperience(jiangli[0].intValue());
                            user.setUpdateDate(new Date());
                            userService.updateById(user);
                            //先更改等级
                            userLevel.setLevelId(userLevel.getLevelId()+1);
                            userLevel.setLevelName((userLevel.getLevelId()-1)+"级");
                            userLevel.setNowExperience(0);
                            userLevel.setUpdateTime(new Date());
                            userLevelService.updateById(userLevel);
                            //每日公告上面的恭喜
                            HotLog hotLog = new HotLog();
                            hotLog.setOpenId(userActivity.getOpenId());
                            hotLog.setRemarks("恭喜"+user.getName()+"升到"+userLevel.getLevelName());
                            hotLog.setCreateDate(new Date());
                            hotLogService.save(hotLog);
                            Integer honorId = Constants.LEVLE_HONOR_MAP.get(userLevel.getLevelId()-1);
                            //可获得称号
                            if(null != honorId){
                                //添加称号信息
                                Honor honor = honorService.getOne(new QueryWrapper<Honor>().eq("id",honorId));
                                UserHonor userHonor = new UserHonor();
                                userHonor.setHonorId(honorId);
                                userHonor.setHonorName(honor.getName());
                                userHonor.setOpenId(userActivity.getOpenId());
                                userHonor.setCreateTime(new Date());
                                userHonorService.save(userHonor);
                                //每日公告上面的恭喜
                                HotLog hotLog1 = new HotLog();
                                hotLog1.setOpenId(userActivity.getOpenId());
                                hotLog1.setRemarks("恭喜"+user.getName()+"获得"+honor.getName()+"称号");
                                hotLog1.setCreateDate(new Date());
                                hotLogService.save(hotLog1);
                                //弹框提示
                                AlertTips alertTips = new AlertTips();
                                alertTips.setOpenId(userActivity.getOpenId());
                                alertTips.setHonorId(honor.getId());
                                alertTips.setHonorName(honor.getName());
                                alertTips.setType(0);
                                alertTips.setStatus(0);
                                alertTips.setCreateTime(new Date());
                                alertTipsService.save(alertTips);
                            }
                        }else{
                            //更新最后添加经验时间
                            userLevel.setUpdateTime(new Date());
                            userLevelService.updateById(userLevel);
                            //塔币增加
                            user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
                            // user.setAllExperience(user.getAllExperience()+jiangli[0].intValue());
                            user.addAllExperience(jiangli[0].intValue());
                            user.setUpdateDate(new Date());
                            userService.updateById(user);
                        }
                    }
                }

            }

        }catch (Exception e){
            log.error("发放前一天的活动赛的奖励信息出错、 错误信息: {}",e);
        }

    }


    /**
     * 每天定时检查活动赛开始结束时间，更新活动赛状态（第一次，00:01:00）
     */
    // @RequestMapping("updateActivityState01")
    @Scheduled(cron = "0 1 0 * * ?")
    @ResponseBody
    public Result updateActivityState01() {
        log.info("活动赛状态第一波更新（00:01:00），当前时间： {}",DateUtil.date());
        // 获取状态为待上线、上线中活动赛集合
        List<Activity> activityList = activityService.list(new QueryWrapper<Activity>().ne("deleted",1).orderByAsc("id"));
        if (CollUtil.isNotEmpty(activityList)) {
            Map<Integer, List<Activity>> activityMap = activityList.stream().collect(Collectors.groupingBy(Activity :: getCompanyType));
            Iterator iterator = activityMap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer index = (Integer) iterator.next();
                List<Activity> activities = activityMap.get(index);
                if (CollUtil.isNotEmpty(activities)) {
                    // 获取当前时间
                    Date now = cn.hutool.core.date.DateUtil.date();
                    for (Activity activity : activities) {
                        if (0 == activity.getDeleted()) {
                            // 监测状态为活动中活动赛
                            if (activity.getEndTime().getTime() < now.getTime()) {
                                // 将活动赛状态修改为"删除"
                                activity.setDeleted(1);
                                activity.setUpdateTime(new Date());
                                activityService.updateById(activity);
                                // 活动赛结束后将活动赛题目导入到应知应会
                                addToFeiTopic(activity.getId());
                            }
                        } else if (2 == activity.getDeleted()) {
                            // 监测状态为待上线活动赛
                            if (activity.getStartTime().getTime() <= now.getTime() && activity.getEndTime().getTime() > now.getTime()) {
                                // 先查询是否还有其他活动赛状态是"上线中"
                                List<Activity> activitys = activityService.list(new QueryWrapper<Activity>().eq("deleted",0).eq("company_type",index));
                                if (activitys.size() > 0) {
                                    throw new BusinessException("还存在其他进行中的活动赛");
                                }
                                // 将该活动状态修改为"上线中"
                                activity.setDeleted(0);
                                activity.setUpdateTime(new Date());
                                activityService.updateById(activity);
                            }
                        } else {
                            throw new BusinessException("活动赛状态错误");
                        }
                    }
                }
            }
        }

        return Result.newSuccess();
    }


    /**
     * 每天定时检查活动赛开始结束时间，更新活动赛状态（第2-n次，早上06:00:00 - 19:00:00，每5分钟执行一次）
     */
    @Scheduled(cron = "0 0/5 6-19 * * ?")
    @ResponseBody
    public Result updateActivityState02() {
        log.info("活动赛状态第二波更新（06:00:00 - 19:00:00，每5分钟执行一次），当前时间， {}",DateUtil.date());
        // 获取状态为待上线、上线中活动赛集合
        List<Activity> activityList = activityService.list(new QueryWrapper<Activity>().ne("deleted",1).orderByAsc("id"));
        if (CollUtil.isNotEmpty(activityList)) {
            Map<Integer, List<Activity>> activityMap = activityList.stream().collect(Collectors.groupingBy(Activity :: getCompanyType));
            Iterator iterator = activityMap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer index = (Integer) iterator.next();
                List<Activity> activities = activityMap.get(index);
                if (CollUtil.isNotEmpty(activities)) {
                    // 获取当前时间
                    Date now = DateUtil.date();
                    for (Activity activity : activities) {
                        if (0 == activity.getDeleted()) {
                            // 监测状态为活动中活动赛
                            if (activity.getEndTime().getTime() < now.getTime()) {
                                // 将活动赛状态修改为"删除"
                                activity.setDeleted(1);
                                activity.setUpdateTime(new Date());
                                activityService.updateById(activity);
                                // 活动赛结束后将活动赛题目导入到应知应会
                                addToFeiTopic(activity.getId());
                            }
                        } else if (2 == activity.getDeleted()) {
                            // 监测状态为待上线活动赛
                            if (activity.getStartTime().getTime() <= now.getTime() && activity.getEndTime().getTime() > now.getTime()) {
                                // 先查询是否还有其他活动赛状态是"上线中"
                                List<Activity> activitys = activityService.list(new QueryWrapper<Activity>().eq("deleted",0).eq("company_type",index));
                                if (activitys.size() > 0) {
                                    throw new BusinessException("还存在其他进行中的活动赛");
                                }
                                // 将该活动状态修改为"上线中"
                                activity.setDeleted(0);
                                activity.setUpdateTime(new Date());
                                activityService.updateById(activity);
                            }
                        } else {
                            throw new BusinessException("活动赛状态错误");
                        }
                    }
                }
            }
        }

        return Result.newSuccess();
    }

    /**
     * 活动赛结束后将活动赛题目导入到应知应会
     * @param activityId 活动赛id
     */
    private void addToFeiTopic(Integer activityId) {
        // 活动赛结束后将活动赛题目导入到应知应会
        List<ActivityTopic> activityTopicList = activityTopicService.list(new QueryWrapper<ActivityTopic>().eq("activity_id", activityId));
        if (!CollUtil.isEmpty(activityTopicList)) {
            for (ActivityTopic activityTopic : activityTopicList) {
                FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
                BeanUtils.copyProperties(activityTopic, feiBranchTopic);
                // BeanUtils 复制的时候null不拷贝，需单独处理
                if (null == activityTopic.getImageUrl()) {
                    feiBranchTopic.setImageUrl(null);
                }
                if (null == activityTopic.getVideoUrl()) {
                    feiBranchTopic.setVideoUrl(null);
                }
                // 主键设置为null
                feiBranchTopic.setId(null);
                // 正确选项id置空
                feiBranchTopic.setCorrectOptionId(null);
                feiBranchTopicService.save(feiBranchTopic);

                // 正确选项id
                int correctOptionId = 0;
                // 获取题目对应答案
                List<ActivityOption> activityOptionList = activityOptionService.list(new QueryWrapper<ActivityOption>().eq("topic_id", activityTopic.getId()));
                if (!CollUtil.isEmpty(activityOptionList)) {
                    for (ActivityOption activityOption : activityOptionList) {
                        FeiBranchOption feiBranchOption = new FeiBranchOption();
                        BeanUtils.copyProperties(activityOption, feiBranchOption);
                        // 主键设置为null
                        feiBranchOption.setId(null);
                        feiBranchOption.setTopicId(feiBranchTopic.getId());
                        feiBranchOptionService.save(feiBranchOption);
                        // 选择题需要反馈相应的正确选项id
                        if (0 == activityTopic.getTopicType()) {
                            if (activityTopic.getCorrectOptionId().equals(activityOption.getId())) {
                                correctOptionId = feiBranchOption.getId();
                            }
                        }
                    }
                }
                // 如果是选择题，需要更新正确选项id
                if (0 == activityTopic.getTopicType()) {
                    feiBranchTopic.setCorrectOptionId(correctOptionId);
                    feiBranchTopicService.updateById(feiBranchTopic);
                }
            }
        }
    }

}