package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.mapper.UserDayGameLogMapper;
import com.wangchen.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.BranchTopicVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 因为每日答题一天只能进行一次,所以这个表在用户获取每日答题的题目的时候就记录下来，这样的话就可以保证一天只玩一次（如果用户答了一半也就是5道题退出了，我们捕捉正常能捕捉到的退出情况，记录下来，如果是什么关机的话， 那就不记录了） 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-22
 */
@Service
public class UserDayGameLogServiceImpl extends ServiceImpl<UserDayGameLogMapper, UserDayGameLog> implements UserDayGameLogService {

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
    private HotLogService hotLogService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;


    /**
     * 获取每日答题题目信息(二期)
     * @param openId 用户id
     * @return 获取每日答题题目集合
     */
    @Override
    @Transactional
    public Result selectTopicInfo(String openId) {
        User user = userService.getUserByOpenId(openId);
        // 获取用户部门，部门多少涉及每日答题抽取相应部门题目数量比例
        List<UserBranch> userBranchList =  userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",openId));
        // 获得用户题库信息(部门type类型集合)
        List<Integer> branchList = getBranchType(userBranchList);
        // 返回的题库信息
        List<BranchTopicVo> branchTopicVoList = new ArrayList<BranchTopicVo>();
        // 首先根据所属公司类型进行分类
        if (1 == user.getType()) {
            // A类公司，每个公司员工最多属于3个部门（宁夏铁塔现阶段为2个）
            if (branchList.contains(0)) {
                // 属于公司领导部门，随机从所属公司分类下题库获取7道题(70%)加入每日答题库
                List<BranchTopic> branchTopicList = branchTopicService.listTopicRandomByCompanyType(7, user.getType());
                // 封装部门题目及答案
                branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
            } else {
                if (1 == branchList.size()) {
                    // 从所属部门题库中随机获取获取7道题(70%)加入每日答题库
                    List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(0),7);
                    // 封装部门题目及答案
                    branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                } else if (2 == branchList.size()) {
                    for (int i = 0; i < branchList.size(); i++) {
                        if (0 == i) {
                            // 从所属部门题库中随机获取获取4道题(40%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),4);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        } else {
                            // 从所属部门题库中随机获取获取3道题(30%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),3);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        }
                    }
                } else if (3 == branchList.size()) {
                    for (int i = 0; i < branchList.size(); i++) {
                        if (0 == i) {
                            // 从所属部门题库中随机获取获取3道题(30%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),3);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        } else if (1 == i) {
                            // 从所属部门题库中随机获取获取2道题(20%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),2);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        } else {
                            // 从所属部门题库中随机获取获取2道题(20%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),2);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        }
                    }
                } else {
                    return Result.newFail("A类公司每个公司员工最多属于3个部门");
                }
            }
        } else if (2 == user.getType()) {
            // B类公司，该类公司只有一个部门
            // 从所属部门题库中随机获取获取7道题(70%)加入每日答题库
            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(0),7);
            // 封装部门题目及答案
            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
        } else if (3 == user.getType()) {
            // C类公司，该类公司下员工目前最多可属于2个部门
            if (branchList.contains(-1)) {
                // 属于'暂无分类'，随机从所属公司分类下题库获取7道题(70%)加入每日答题库
                List<BranchTopic> branchTopicList = branchTopicService.listTopicRandomByCompanyType(7, user.getType());
                // 封装部门题目及答案
                branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
            } else {
                if (1 == branchList.size()) {
                    // 从所属部门题库中随机获取获取7道题(70%)加入每日答题库
                    List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(0),7);
                    // 封装部门题目及答案
                    branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                } else if (2 == branchList.size()) {
                    for (int i = 0; i < branchList.size(); i++) {
                        if (0 == i) {
                            // 从所属部门题库中随机获取获取4道题(40%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),4);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        } else {
                            // 从所属部门题库中随机获取获取3道题(30%)加入每日答题库
                            List<BranchTopic> branchTopicList = branchTopicService.listTopicRandom(branchList.get(i),3);
                            // 封装部门题目及答案
                            branchTopicVoList = dealwithBranchTopicList(branchTopicVoList,branchTopicList);
                        }
                    }
                } else {
                    //@TODO  C类目前暂不考虑属于第三个部门情况（目前BC类公司人员最多只属于两个部门）
                }
            }
        } else {
            return Result.newFail("用户所属公司分类异常");
        }

        // 随机从所属公司分类下必知必会题库获取3道题加入每日答题库（30%）
        List<FeiBranchTopic> feiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(3, user.getType());
        // 封装非专业部门题目及答案
        branchTopicVoList = dealwithFeiBranchTopicList(branchTopicVoList,feiBranchTopicList);

        // 开始每日答题，记录他今天已经参与过
        UserDayGameLog saveUserDayGameLog = new UserDayGameLog();
        saveUserDayGameLog.setOpenId(openId);
        saveUserDayGameLog.setDayGameDate(new Date());
        saveUserDayGameLog.setScore(0);
        saveUserDayGameLog.setCreateTime(new Date());
        saveUserDayGameLog.setDeleted(0);
        userDayGameLogService.save(saveUserDayGameLog);

        // 签到成就逻辑
        Sign sign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",openId).eq("sign_date",Constants.SDF_YYYY_MM_DD.format(new Date())));
        if (null == sign) {
            //签到
            sign =new Sign();
            sign.setOpenId(openId);
            sign.setSignDate(new Date());
            sign.setIsAnswerDay(0);
            sign.setCreateTime(new Date());
            sign.setDeleted(0);
            signService.save(sign);
        }
        // 连续同一天完成每日签到、每日答题数为0
        if (0 == sign.getIsAnswerDay().intValue()) {
            // 昨天的是否完成签到
            Sign zuotianSign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",openId).eq("sign_date", DateUtils.getZuoTianDay()));
            // 如果昨天没完成，那就算重置今天算第一天完成每日答题
            if (null == zuotianSign) {
                sign.setIsAnswerDay(1);
                signService.updateById(sign);
            } else {
                if (zuotianSign.getIsAnswerDay() == 0) {
                    sign.setIsAnswerDay(1);
                    signService.updateById(sign);
                } else {
                    Integer chengjiu = Constants.dayWanCheng_ChengJiuMap.get(zuotianSign.getIsAnswerDay()+1);
                    // 如果没有对应的成就就什么只需要把签到表的每日答题天数加1
                    if (null == chengjiu) {
                        sign.setIsAnswerDay(zuotianSign.getIsAnswerDay()+1);
                        signService.updateById(sign);
                    } else {
                        // 如果有对应的成就
                        UserAchievement userAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>()
                                .eq("open_id",openId).eq("achievement_id",chengjiu));
                        // 如果用户还没获取过这个成就，就给他加这个成就
                        if (null == userAchievement) {
                            Achievement achievement = achievementService.getOne(new QueryWrapper<Achievement>().eq("id",chengjiu));
                            // 用户总成就值添加
                            user.setAllAchievement(user.getAllAchievement()+achievement.getNum());
                            user.setUpdateDate(new Date());
                            userService.updateById(user);
                            // 用户成就表里加数据
                            userAchievement = new UserAchievement();
                            userAchievement.setOpenId(openId);
                            userAchievement.setAchievementId(achievement.getId());
                            userAchievement.setAchievementName(achievement.getName());
                            userAchievement.setCreateTime(new Date());
                            userAchievementService.save(userAchievement);
                            // 每日公告上面的恭喜
                            HotLog hotLog1 = new HotLog();
                            hotLog1.setOpenId(openId);
                            hotLog1.setRemarks("恭喜"+user.getName()+"获得了"+achievement.getName()+"成就");
                            hotLog1.setCreateDate(new Date());
                            hotLogService.save(hotLog1);
                            // 弹框提示
                            AlertTips alertTips = new AlertTips();
                            alertTips.setOpenId(openId);
                            alertTips.setAchievementId(achievement.getId());
                            alertTips.setAchievementName(achievement.getName());
                            alertTips.setType(1);
                            alertTips.setStatus(0);
                            alertTips.setCreateTime(new Date());
                            alertTipsService.save(alertTips);
                        }
                        // 如果获取过成就了 那就只天数加1
                        sign.setIsAnswerDay(zuotianSign.getIsAnswerDay()+1);
                        signService.updateById(sign);
                    }
                }

            }
        }

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("gameId",saveUserDayGameLog.getId());
        map.put("branchTopicVoList",branchTopicVoList);
        return Result.newSuccess(map);
    }


    /**
     * 二期，每日答题，个人赛、团队赛随意进行，已经不需要查询是否完成每日答题，只需返回每日答题相关数据即可、
     * @param openId 用户id
     * @return 每日答题相关数据
     * 规则：每天仅第1次有奖 10题全对5经验 5塔币  6-9题3经验3塔币 1-5题1经验0塔币
     */
    @Override
    @Transactional
    public Result userIsDayGame(String openId) {
        // 最多能获得的经验；
        Integer allExp = 1*5;
        // 已经获得的经验；
        Integer nowExp = 0;
        // 最多能获得的塔币；
        Integer allCoin = 1*5;
        // 已经获得的塔币；
        Integer nowCoin = 0;
        // 今天对战次数；
        Integer vsCount = 0;
        // 都有经验和塔币的对战总次数（服务器可写死为1）；
        Integer vsECNum = 1;
        // 只有经验的对战总次数（服务器可写死为1）
        Integer vsENum = 1;

        List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().eq("open_id",openId)
                .eq("day_game_date",Constants.SDF_YYYY_MM_DD.format(new Date())));
        if(CollectionUtils.isNotEmpty(userDayGameLogList)){
            // 单人答题时间正序
            for (UserDayGameLog userDayGameLog : userDayGameLogList) {
                vsCount++;
                // 根据第一次答题分数计算不同的积分塔币
                if(vsCount==1){
                    if (userDayGameLog.getScore() == 100) {
                        nowExp = 5;
                        nowCoin = 5;
                    } else if (userDayGameLog.getScore() >= 60) {
                        nowExp = 3;
                        nowCoin = 3;
                    } else if (userDayGameLog.getScore() >= 10) {
                        nowExp = 1;
                        nowCoin = 0;
                    }
                }
            }
        }
        HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put("allexp",allExp);
        stringIntegerHashMap.put("nowExp",nowExp);
        stringIntegerHashMap.put("allCoin",allCoin);
        stringIntegerHashMap.put("nowCoin",nowCoin);
        stringIntegerHashMap.put("vsCount",vsCount);
        stringIntegerHashMap.put("vsECNum",vsECNum);
        stringIntegerHashMap.put("vsENum",vsENum);

        return Result.newSuccess(stringIntegerHashMap);
    }


    /**
     * 封装非部门题目及答案
     * @param branchTopicVoList 返回的题库信息
     * @param feiBranchTopicList 需要添加进题库题目集合
     * @return 题库信息
     */
    private List<BranchTopicVo> dealwithFeiBranchTopicList(List<BranchTopicVo> branchTopicVoList, List<FeiBranchTopic> feiBranchTopicList) {
        for(FeiBranchTopic feiBranchTopic : feiBranchTopicList){
            BranchTopicVo branchTopicVo = new BranchTopicVo();
            BeanUtils.copyProperties(feiBranchTopic,branchTopicVo);
            branchTopicVo.setImageUrl(feiBranchTopic.getImageUrl());
            branchTopicVo.setVideoUrl(feiBranchTopic.getVideoUrl());
            List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(
                    new QueryWrapper<FeiBranchOption>().eq("topic_id",feiBranchTopic.getId()));
            List<BranchOption> branchOptionList = new ArrayList<BranchOption>();
            for(FeiBranchOption feiBranchOption : feiBranchOptionList){
                BranchOption branchOption = new BranchOption();
                BeanUtils.copyProperties(feiBranchOption,branchOption);
                // 对每日答题判断题返回"正确"、"错误"信息处理
                if (2 == feiBranchTopic.getTopicType()) {
                    if ("对".equals(feiBranchOption.getContent())) {
                        branchOption.setContent( "正确");
                    } else {
                        branchOption.setContent( "错误");
                    }
                }
                branchOptionList.add(branchOption);
            }
            branchTopicVo.setBranchOptionList(branchOptionList);
            branchTopicVoList.add(branchTopicVo);
        }

        return branchTopicVoList;
    }


    /**
     * 封装部门题目及答案
     * @param branchTopicVoList 返回的题库信息
     * @param branchTopicList 需要添加进题库题目集合
     * @return 题库信息
     */
    private List<BranchTopicVo> dealwithBranchTopicList(List<BranchTopicVo> branchTopicVoList, List<BranchTopic> branchTopicList) {
        for(BranchTopic branchTopic : branchTopicList){
            BranchTopicVo branchTopicVo = new BranchTopicVo();
            BeanUtils.copyProperties(branchTopic,branchTopicVo);
            branchTopicVo.setTypeName(Constants.newBuMenKu.get(branchTopicVo.getType())[1]);
            branchTopicVo.setImageUrl(branchTopic.getImageUrl());
            branchTopicVo.setVideoUrl(branchTopic.getVideoUrl());
            List<BranchOption> branchOptionList = new ArrayList<BranchOption>();
            List<BranchOption> currBranchOptionList = branchOptionService.list(
                    new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId()));
            for (BranchOption branchOption : currBranchOptionList) {
                if (2 == branchTopic.getTopicType()) {
                    BranchOption currBranchOption = new BranchOption();
                    BeanUtils.copyProperties(branchOption, currBranchOption);
                    String currStr = branchOption.getContent();
                    if ("对".equals(currStr)) {
                        currBranchOption.setContent("正确");
                    } else {
                        currBranchOption.setContent("错误");
                    }
                    branchOptionList.add(currBranchOption);
                } else {
                    branchOptionList.add(branchOption);
                }
            }
            branchTopicVo.setBranchOptionList(branchOptionList);
            branchTopicVoList.add(branchTopicVo);
        }

        return branchTopicVoList;
    }


    /**
     * 获取用户部门对应的题库信息
     * @param userBranchList
     * @return
     */
    public List<Integer> getBranchType(List<UserBranch> userBranchList){
        List<Integer> branchList = new ArrayList<>();

        if(userBranchList.size() == 1){
            Integer[] integers = Constants.newBranchTypeMap.get(userBranchList.get(0).getBranchId());
            branchList.add(integers[1]);
        }else{
            for(UserBranch userBranch : userBranchList){
                Integer[] integers = Constants.newBranchTypeMap.get(userBranch.getBranchId());
                branchList.add(integers[1]);
            }
        }

        return branchList;
    }
}
