package com.wangchen.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.UserLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日答题游戏接口
 *
 * @author cheng.zhang
 * @company wangcheng
 * @create 2020-02-19 13:57
 * @Version 1.0
 */
@CrossOrigin(origins = "*")
@Controller
@Slf4j
@RequestMapping("/api/daygame")
public class DayGameApi {

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private SignService signService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private HonorService honorService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private ManguanService manguanService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserActivityService userActivityService;

    /**
     * 二期，每日答题，个人赛、团队赛随意进行，已经不需要查询是否完成每日答题，只需返回每日答题相关数据即可、
     *
     * @param openId 用户id
     * @return 每日答题相关数据
     * 规则：每天仅第1次有奖 10题全对5经验 5塔币  6-9题3经验3塔币 1-5题1经验0塔币
     */
    @ResponseBody
    @PostMapping(value = "/userIsDayGame")
    public Result singleRank(@RequestParam(value = "openId", required = false, defaultValue = "") String openId) {
        try {
            User user = userService.getUserByOpenId(openId);
            if (null == user) {
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if (StringUtils.isEmpty(user.getIdCard())) {
                return Result.newFaild("用户不是内部员工");
            }
            Result result = userDayGameLogService.userIsDayGame(openId);
            return result;
        } catch (Exception e) {
            log.error("查询用户每日答题出错，错误信息: {}", e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 获取题目信息(二期)
     *
     * @param openId 用户id
     * @return 获取每日答题题目集合
     */
    @PostMapping(value = "/selectTopicInfo")
    @ResponseBody
    public Result selectSingleTopicInfo(@RequestParam(value = "openId", required = false, defaultValue = "") String openId) {
        try {
            User user = userService.getUserByOpenId(openId);
            if (null == user) {
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if (StringUtils.isEmpty(user.getIdCard())) {
                return Result.newFail("用户不是内部员工");
            }
            Result result = userDayGameLogService.selectTopicInfo(openId);
            return result;
        } catch (Exception e) {
            log.error("每日题目获取失败 错误信息: {}", e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 每日答题接收结果信息
     *
     * @param model
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/endAnswer")
    public Result endAnswer(Model model,
                            @RequestParam(value = "openId", required = false, defaultValue = "") String openId,
                            @RequestParam(value = "gameId", required = false, defaultValue = "") Integer gameId,
                            @RequestParam(value = "score", required = false) Integer score) {
        try {
            log.info("每日答题接收结果信息接口...openId :{} ,score : {} ", openId, score);
            User user = userService.getUserByOpenId(openId);
            if (null == user) {
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if (StringUtils.isEmpty(user.getIdCard())) {
                return Result.newFail("用户不是内部员工");
            }
            if (null == gameId || 0 == gameId) {
                return Result.newFail("游戏编号不能为空");
            }

            if (null == score) {
                return Result.newFail("未获取到分数信息");
            }

            UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id", openId));
            // 用户当前等级经验峰值
            Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId().intValue() + 1);
            // 获取分数对应的经验值和塔币（二期数值有变动）
            Integer[] jiangli = Constants.socreMap.get(score.intValue());
            // 记录一下分数
            List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().eq("open_id", openId).eq("day_game_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
            if (CollectionUtils.isEmpty(userDayGameLogList)) {
                return Result.newFail("在提交答案的时候，没有查到获取题目的操作，这是异常的提交答案");
            }
            // 如果找到每日答题列表中有该用户今天的答题信息，那么该次作答没有经验和塔币奖励
            if (userDayGameLogList.size() > 1) {
                UserDayGameLog userDayGameLog = userDayGameLogService.getById(gameId);
                // 前期用分数为零判断是否已答题不严谨，应判断提交时间是否为空
                if (null != userDayGameLog.getSubmitTime()) {
                    return Result.newFail("请勿重复提交答题结果");
                }
                userDayGameLog.setSubmitTime(new Date());
                userDayGameLog.setScore(score);
                userDayGameLogService.updateById(userDayGameLog);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("score", score);
                map.put("experience", 0);
                map.put("coin", 0);
                map.put("level", userLevel.getLevelId() - 1);
                return Result.newSuccess(map);
            }
            UserDayGameLog userDayGameLog = userDayGameLogService.getById(gameId);
            if (null == userDayGameLog) {
                log.error("未获取到本次答题信息 gameId:{}", gameId);
                return Result.newFail("未获取到本次答题信息");
            }
            if (0 != userDayGameLog.getScore()) {
                return Result.newFail("请勿重复提交答题结果");
            }
            userDayGameLog.setSubmitTime(new Date());
            userDayGameLog.setScore(score);
            userDayGameLogService.updateById(userDayGameLog);

            // 如果经验值相加 小于峰值 那就是还不用升级
            if (value > (user.getPresentExperience() + jiangli[0].intValue())) {
                // 更新最后添加经验时间
                userLevel.setUpdateTime(new Date());
                userLevelService.updateById(userLevel);
                // 经验增加
                user.addAllExperience(jiangli[0].intValue());
                // 塔币增加
                user.setAllCoin(user.getAllCoin() + jiangli[1].intValue());
                user.setUpdateDate(new Date());
                userService.updateById(user);
            } else {
                // 二期，对应最高等级由100级变更为120级
                if (userLevel.getLevelId().intValue() != 121) {
                    // 塔币增加
                    user.setAllCoin(user.getAllCoin() + jiangli[1].intValue());
                    // 经验增加
                    user.addAllExperience(jiangli[0].intValue());
                    user.setUpdateDate(new Date());
                    userService.updateById(user);
                    // 先更改等级
                    userLevel.setLevelId(userLevel.getLevelId() + 1);
                    userLevel.setLevelName((userLevel.getLevelId() - 1) + "级");
                    userLevel.setNowExperience(0);
                    userLevel.setUpdateTime(new Date());
                    userLevelService.updateById(userLevel);
                    // 每日公告上面的恭喜
                    HotLog hotLog = new HotLog();
                    hotLog.setOpenId(openId);
                    hotLog.setRemarks("恭喜" + user.getName() + "升到" + userLevel.getLevelName());
                    hotLog.setCreateDate(new Date());
                    hotLogService.save(hotLog);

                    Integer honorId = Constants.LEVLE_HONOR_MAP.get(userLevel.getLevelId() - 1);
                    // 可获得称号
                    if (null != honorId) {
                        // 添加称号信息
                        Honor honor = honorService.getOne(new QueryWrapper<Honor>().eq("id", honorId));
                        UserHonor userHonor = new UserHonor();
                        userHonor.setHonorId(honorId);
                        userHonor.setHonorName(honor.getName());
                        userHonor.setOpenId(openId);
                        userHonor.setCreateTime(new Date());
                        userHonorService.save(userHonor);
                        // 每日公告上面的恭喜
                        HotLog hotLog1 = new HotLog();
                        hotLog1.setOpenId(openId);
                        hotLog1.setRemarks("恭喜" + user.getName() + "获得" + honor.getName() + "称号");
                        hotLog1.setCreateDate(new Date());
                        hotLogService.save(hotLog1);
                        //弹框提示
                        AlertTips alertTips = new AlertTips();
                        alertTips.setOpenId(openId);
                        alertTips.setHonorId(honor.getId());
                        alertTips.setHonorName(honor.getName());
                        alertTips.setType(0);
                        alertTips.setStatus(0);
                        alertTips.setCreateTime(new Date());
                        alertTipsService.save(alertTips);
                    }
                } else {
                    // 更新最后添加经验时间
                    userLevel.setUpdateTime(new Date());
                    userLevelService.updateById(userLevel);
                    // 塔币增加
                    user.setAllCoin(user.getAllCoin() + jiangli[1].intValue());
                    // 经验增加
                    user.addAllExperience(jiangli[0].intValue());
                    user.setUpdateDate(new Date());
                    userService.updateById(user);
                }
            }

            // 每日公告上面的恭喜
            HotLog hotLog = new HotLog();
            hotLog.setOpenId(openId);
            hotLog.setRemarks(user.getName() + "完成了每日答题");
            hotLog.setCreateDate(new Date());
            hotLogService.save(hotLog);

            if (score >= 60) {
                // 由于取消参与活动赛必须先参与每日答题的限制，所以在完成每日答题时同样需要判断是否以满足"大满贯得主"、"超级大满贯得主"称号条件
                // 获取昨日每日答题、活动赛完成情况
                Manguan zuoTianManguan = manguanService.getOne(new QueryWrapper<Manguan>().eq("open_id", openId).eq("answer_date", DateUtils.getZuoTianDay()));
                // 查询当天参与活动赛信息
                UserActivity userActivity = userActivityService.getOne(new QueryWrapper<UserActivity>().eq("open_id", openId).eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
                // 大满贯的记录 每日答题这里加数据
                Manguan manguan = new Manguan();
                manguan.setOpenId(openId);
                manguan.setMeiriAnswer(1);
                manguan.setAnswerTiaozhan(null == userActivity ? 0 : 1);
                manguan.setAnswerDate(new Date());
                manguan.setHowNum(null == userActivity ? 0 : userActivity.getScore() >= 60 ? (null == zuoTianManguan ? 0 : zuoTianManguan.getHowNum()) + 1 : 0);
                manguan.setCreateTime(new Date());
                manguanService.save(manguan);

                if (null != userActivity && userActivity.getScore() >= 60) {
                    // 查看是否"大满贯得主"
                    UserAchievement isHas1UserAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>().eq("open_id", openId).eq("achievement_id", 7));
                    // 大满贯都还没拥有的话，那昨天肯定就是没完成大满贯的任务 就不用查看昨天的数据了
                    if (null == isHas1UserAchievement) {
                        user.setAllAchievement(user.getAllAchievement() + 5);
                        user.setUpdateDate(new Date());
                        userService.updateById(user);

                        // 添加满贯成就
                        UserAchievement userAchievement = new UserAchievement();
                        userAchievement.setOpenId(openId);
                        userAchievement.setAchievementId(7);
                        userAchievement.setAchievementName("大满贯得主");
                        userAchievement.setCreateTime(new Date());
                        userAchievementService.save(userAchievement);

                        // 更新每日同时完成每日答题、活动赛且都及格的连续次数
                        manguan.setHowNum(1);
                        manguan.setMeiriAnswer(1);
                        manguanService.updateById(manguan);

                        // 弹框提示
                        AlertTips alertTips = new AlertTips();
                        alertTips.setOpenId(openId);
                        alertTips.setAchievementId(7);
                        alertTips.setAchievementName("大满贯得主");
                        alertTips.setType(1);
                        alertTips.setStatus(0);
                        alertTips.setCreateTime(new Date());
                        alertTipsService.save(alertTips);
                    } else {
                        UserAchievement isHas2UserAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>()
                                .eq("open_id", openId).eq("achievement_id", 8));
                        // 超级大满贯为空的话 计算当前完成了多少天数了
                        if (null == isHas2UserAchievement) {
                            if (null == zuoTianManguan) {
                                manguan.setMeiriAnswer(1);
                                manguan.setHowNum(1);
                                manguanService.updateById(manguan);
                            } else {
                                // 如果昨天不等于空
                                if (zuoTianManguan.getHowNum().intValue() != 0) {
                                    manguan.setMeiriAnswer(1);
                                    manguan.setHowNum(zuoTianManguan.getHowNum() + 1);
                                    manguanService.updateById(manguan);

                                    if (manguan.getHowNum() == 7) {
                                        // 已经是完成了超级大满贯得主所需所有要求
                                        user.setAllAchievement(user.getAllAchievement() + 20);
                                        user.setUpdateDate(new Date());
                                        userService.updateById(user);
                                        // 添加满贯成就
                                        UserAchievement userAchievement = new UserAchievement();
                                        userAchievement.setOpenId(openId);
                                        userAchievement.setAchievementId(8);
                                        userAchievement.setAchievementName("超级大满贯得主");
                                        userAchievement.setCreateTime(new Date());
                                        userAchievementService.save(userAchievement);

                                        // 弹框提示
                                        AlertTips alertTips = new AlertTips();
                                        alertTips.setOpenId(openId);
                                        alertTips.setAchievementId(8);
                                        alertTips.setAchievementName("超级大满贯得主");
                                        alertTips.setType(1);
                                        alertTips.setStatus(0);
                                        alertTips.setCreateTime(new Date());
                                        alertTipsService.save(alertTips);
                                    }
                                } else {
                                    manguan.setMeiriAnswer(1);
                                    manguan.setHowNum(1);
                                    manguanService.updateById(manguan);
                                }
                            }
                        } else {
                            // 相当于大满贯和超级大满贯称号都已经拥有了 那就什么操作都不做啦
                        }
                    }
                }
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("score", score);
            map.put("experience", jiangli[0]);
            map.put("coin", jiangli[1]);
            map.put("level", userLevel.getLevelId() - 1);
            return Result.newSuccess(map);
        } catch (Exception e) {
            log.error("单人模式答题出错，错误信息: {}", e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 获取用户部门对应的题库信息
     *
     * @param userBranchList 用户部门集合
     * @return 用户部门对应的题库信息
     */
    public List<Integer> getBranchType(List<UserBranch> userBranchList) {
        List<Integer> branchList = new ArrayList<>();
        if (userBranchList.size() == 1) {
            Integer[] integers = Constants.newBranchTypeMap.get(userBranchList.get(0).getBranchId());
            branchList.add(integers[1]);
        } else {
            for (UserBranch userBranch : userBranchList) {
                Integer[] integers = Constants.newBranchTypeMap.get(userBranch.getBranchId());
                branchList.add(integers[1]);
            }
        }

        return branchList;
    }


    /**
     * 软件维护期间：补全用户所拥有成就信息 @TODO没有修正user.all_achievement成就奖励点数
     *
     * @param startTime 停服时间 （格式：2021-10-27）
     * @return 补全用户拥有成就信息结果
     */
    // @RequestMapping("updateUserAchievement")
    @ResponseBody
    @Transactional
    public Result updateUserAchievement(@RequestParam(value = "startTime", required = true, defaultValue = "") String startTime) {
        if (StringUtils.isEmpty(startTime)) {
            return Result.newFail("开始停服时间不能为空");
        }
        // 获取当前时间
        String date = Constants.SDF_YYYY_MM_DD.format(new Date());
        Date start = DateUtil.parse(startTime, "yyyy-MM-dd");
        Date end = DateUtil.parse(date, "yyyy-MM-dd");
        if (start.getTime() - end.getTime() > 0) {
            return Result.newFail("开始停服时间不能大于当前时间");
        }
        Integer one = Constants.chengJiu_DayWanChengMap.get(1);
        Integer two = Constants.chengJiu_DayWanChengMap.get(2);
        Integer three = Constants.chengJiu_DayWanChengMap.get(3);
        Integer four = Constants.chengJiu_DayWanChengMap.get(4);
        Integer five = Constants.chengJiu_DayWanChengMap.get(5);

        // 获取开始停服时间至当前时间所有签到信息
        List<Sign> signList = signService.list(new QueryWrapper<Sign>().ge("sign_date", startTime).le("sign_date", date));
        if (CollUtil.isNotEmpty(signList)) {
            // 获取所有用户用于成就
            List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<>());
            Map<String, List<UserAchievement>> userAchievementMap = new HashMap<>();
            if (CollUtil.isNotEmpty(userAchievementList)) {
                userAchievementMap = userAchievementList.stream().collect(Collectors.groupingBy(UserAchievement::getOpenId));
            } else {
                return Result.newFail("用户成就信息获取出错");
            }
            ArrayList<UserAchievement> list = new ArrayList<UserAchievement>();
            Map<String, List<Sign>> signMap = signList.stream().collect(Collectors.groupingBy(Sign::getOpenId));
            Iterator<String> iterator = signMap.keySet().iterator();
            while (iterator.hasNext()) {
                String index = iterator.next();
                List<Sign> signs = signMap.get(index);
                List<UserAchievement> userAchievements = userAchievementMap.get(index);
                int min = 0;
                int max = 0;
                if (CollUtil.isNotEmpty(signs)) {
                    min = signs.get(0).getIsAnswerDay();
                    max = signs.get(signs.size() - 1).getIsAnswerDay();
                }
                int achievementId = -1;
                String achievementName = "";
                if (min > five && five < max) {
                    // 应该获取 "纵横天下" 成就
                    achievementId = 5;
                    achievementName = "纵横天下";
                } else if (min <= five && five <= max) {
                    // 应该获取 "纵横天下" 成就
                    achievementId = 5;
                    achievementName = "纵横天下";
                } else if (min <= four && four <= max) {
                    // 应该获取 "一骑绝尘" 成就
                    achievementId = 4;
                    achievementName = "一骑绝尘";
                } else if (min <= three && three <= max) {
                    // 应该获取 "孜孜不倦" 成就
                    achievementId = 3;
                    achievementName = "孜孜不倦";
                } else if (min <= two && two <= max) {
                    // 应该获取 "活跃达人" 成就
                    achievementId = 2;
                    achievementName = "活跃达人";
                } else if (min <= one && one <= max) {
                    // 应该获取 "笨鸟先飞" 成就
                    achievementId = 1;
                    achievementName = "笨鸟先飞";
                } else {
                    // 还没获取任何成就，不做处理
                }

                if (-1 != achievementId) {
                    boolean exit = false;
                    for (UserAchievement userAchievement : userAchievements) {
                        if (achievementId == userAchievement.getAchievementId()) {
                            exit = true;
                            continue;
                        }
                    }
                    if (!exit) {
                        // 如果该用户还没拥有此成就，则添加此成就
                        UserAchievement userAchievement = new UserAchievement();
                        userAchievement.setOpenId(index);
                        userAchievement.setAchievementId(achievementId);
                        userAchievement.setAchievementName(achievementName);
                        userAchievement.setCreateTime(new Date());
                        list.add(userAchievement);
                    }
                }
            }
            // 批量保存操作
            userAchievementService.saveBatch(list);
            // 修正user.all_achievement成就奖励点数
            this.updateUserAllAchievement();
        }

        return Result.newSuccess();
    }


    /**
     * 修正user.all_achievement成就奖励点数
     */
    // @RequestMapping("updateUserAllAchievement")
    @ResponseBody
    @Transactional
    public Result updateUserAllAchievement() {
        StringBuilder data_msg = new StringBuilder();
        //所有用户
        List<User> userList = userService.list(new QueryWrapper<User>());
        for (User user : userList) {
            //该用户成就持久数据
            int all_achievement = user.getAllAchievement();
            int real_all_achievement = 0;
            //该用户所有已获得成就
            List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id", user.getOpenId()));
            for (UserAchievement userAchievement : userAchievementList) {
                Achievement achievement = achievementService.getOne(new QueryWrapper<Achievement>().eq("type", userAchievement.getAchievementId()));
                real_all_achievement += achievement.getNum();
            }
            if (all_achievement != real_all_achievement) {
                user.setAllAchievement(real_all_achievement);
                user.setUpdateDate(new Date());
                userService.updateById(user);
                data_msg.append("需要修正成就奖励点数,用户:" + user.getOpenId() + ",原值:" + all_achievement + ",正确值:" + real_all_achievement + ";    ");
            }
        }
        return Result.newSuccess(data_msg);
    }


}