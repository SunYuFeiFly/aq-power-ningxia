package com.wangchen.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.CompanyUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.wangchen.common.constant.Constants.dayWanCheng_ChengJiuMap;

/**
 * 用户信息
 */

@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/user")
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserGoodsService userGoodsService;

    @Autowired
    private ExperienceService experienceService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserGoodsAddressService userGoodsAddressService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private SignService signService;

    @Autowired
    private SevenSignService sevenSignService;

    @Autowired
    private ManguanService manguanService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;


    /**
     * 获取用户信息(二期)
     * @param openId 用户id
     */
    @PostMapping("/getUserInfo")
    @ResponseBody
    public Result getUserInfo(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            Map<Object,Object> map =new HashMap<Object,Object>();
            if(StringUtils.isNotEmpty(user.getIdCard())){
                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",openId));
                // 当前等级
                map.put("level",userLevel.getLevelId() - 1);
                // 二期：升至下一等级所需经验
                Level level = levelService.getOne(new QueryWrapper<Level>().eq("level", userLevel.getLevelId()));
                user.setNextLevelNeedExperience(level.getValue() - user.getPresentExperience());
                // 二期：下一等所占总经验
                user.setNextLevelExperience(level.getValue());
                // 所有成就总共兑换的积分
                user.setAllIntegral(user.getAllAchievement() * 10);

                // 成就
                List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id",openId).orderByDesc("create_time"));
                if(CollectionUtils.isEmpty(userAchievementList)){
                    map.put("achievement","暂未获取成就");
                    map.put("achievementUrl", null);
                }else{
                    map.put("achievement",userAchievementList.get(0).getAchievementName());
                    int index = userAchievementList.get(0).getAchievementId();
                    String achievementUrl = Constants.ACHIEVEMENT_IMAGE_ADDRESS + "image_0"+ index +".png";
                    map.put("achievementUrl", achievementUrl);
                }
                // 称号
                List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",openId).orderByDesc("create_time"));
                if(CollectionUtils.isEmpty(userHonorList)){
                    UserHonor userHonor = new UserHonor();
                    userHonor.setHonorId(0);
                    userHonor.setHonorName("暂未获得段位");
                    map.put("honorInfo",userHonor);
                }else{
                    map.put("honorInfo",userHonorList.get(0));
                }
                // 查询皮肤使用情况
                List<UserGoods> userGoodsList = userGoodsService.list(new QueryWrapper<UserGoods>().eq("open_id", openId).eq("goods_type", 0).eq("is_flag", 1).orderByDesc("create_time"));
                if (CollUtil.isNotEmpty(userGoodsList)) {
                    Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",userGoodsList.get(0).getGoodsId()));
                    map.put("baseSkin",goods.getUrl());
                    map.put("baseSkinId",goods.getId());
                    for (int i = 0; i < userGoodsList.size(); i++) {
                        UserGoods userGoods = userGoodsList.get(i);
                        userGoods.setIsFlag(0);
                        if (0 == i) {
                            userGoods.setIsFlag(1);
                        }
                        userGoodsService.updateById(userGoods);
                    }
                } else {
                    map.put("baseSkin",null);
                    map.put("baseSkinId",null);
                }
                // 查询头像使用情况
                List<UserGoods> userGoodss = userGoodsService.list(new QueryWrapper<UserGoods>().eq("open_id", openId).eq("goods_type", 1).eq("is_flag", 1).orderByDesc("create_time"));
                if (CollUtil.isNotEmpty(userGoodss)) {
                    Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",userGoodss.get(0).getGoodsId()));
                    map.put("baseHeadUrl",goods.getUrl());
                    for (int i = 0; i < userGoodss.size(); i++) {
                        UserGoods userGoods = userGoodss.get(i);
                        userGoods.setIsFlag(0);
                        if (0 == i) {
                            userGoods.setIsFlag(1);
                        }
                        userGoodsService.updateById(userGoods);
                    }
                } else {
                    map.put("baseHeadUrl",null);
                }
                List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",openId));
                if(CollectionUtils.isNotEmpty(userBranchList)){
                    List<String> userBranchs= new ArrayList<String>();
                    // 前端展示我的部门是最靠后部门，现将返回数据反转
                    for (int i = userBranchList.size() - 1; i >= 0; i--) {
                        userBranchs.add(userBranchList.get(i).getBranchName());
                    }
                    map.put("userBranchs",userBranchs);
                }else{
                    map.put("userBranchs",new ArrayList<>());
                }
            }else{
                //称号
                //成就
                List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id",openId).orderByDesc("create_time"));
                if(CollectionUtils.isEmpty(userAchievementList)){
                    map.put("achievement","暂未获取成就");
                }else{
                    map.put("achievement",userAchievementList.get(0).getAchievementName());
                }
                List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",openId).orderByDesc("create_time"));
                if(CollectionUtils.isEmpty(userHonorList)){
                    UserHonor userHonor = new UserHonor();
                    userHonor.setHonorId(0);
                    userHonor.setHonorName("暂未获得段位");
                    map.put("honorInfo",userHonor);
                }else{
                    map.put("honorInfo",userHonorList.get(0));
                }
                map.put("baseSkin",null);
                map.put("baseSkinId",null);
                map.put("baseHeadUrl",null);
                map.put("userBranchs",new String[]{});
            }
            map.put("type",user.getType());
            map.put("userInfo",user);

            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("获取用户信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 保存用户信息（二期）
     * @param openId 用户id
     * @param phone 用户手机号码
     * @return 保存用户信息结果
     */
    @PostMapping("/saveUser")
    @ResponseBody
    public Result saveUser(@RequestParam(value = "openId",required = false) String openId,
                           @RequestParam(value = "phone",required = false) String phone){
        try {
            log.info("save user: openId:{},phone:{}",openId,phone);
            BaseUser baseUser = baseUserService.getOne(new QueryWrapper<BaseUser>().eq("phone",phone));
            if(null == baseUser){
                return Result.newFail("0001","您不是该企业员工");
            }
            User user = userService.getOne(new QueryWrapper<User>().eq("open_id",openId));
            if(StringUtils.isNotEmpty(user.getMobile())){
                return Result.newFail("0003","当前员工已存在");
            }
            User isPhoneUser = userService.getOne(new QueryWrapper<User>().eq("mobile",phone));
            if(null != isPhoneUser){
                return Result.newFail("0002","该手机号已经注册了");
            }

            // 返回参数
            Map<Object,Object> map =new HashMap<Object,Object>();
            user = userService.getOne(new QueryWrapper<User>().eq("open_id",openId));
            user.setIdCard(baseUser.getIdCard());
            user.setName(baseUser.getName());
            user.setMobile(baseUser.getPhone());
            if(0== baseUser.getSex()){
                user.setSex(0);
            }else{
                user.setSex(1);
            }
            user.setCompanyId(CompanyUtils.getCompanyMap(baseUser.getCompany()));
            user.setCompanyName(baseUser.getCompany());
            user.setAllExperience(0);
            user.setAllCoin(0);
            user.setAllAchievement(0);
            user.setPrizeCount(0);
            user.setDeleted(0);
            user.setType(baseUser.getType());
            user.setUpdateDate(new Date());
            userService.updateById(user);
            // 添加默认皮肤
            List<Goods> goodsList = goodsService.list(new QueryWrapper<Goods>().eq("is_mo_ren",1).eq("sex",user.getSex()));
            for(Goods goods :goodsList){
                UserGoods userGoods = new UserGoods();
                userGoods.setOpenId(openId);
                userGoods.setGoodsId(goods.getId());
                userGoods.setGoodsType(goods.getType());
                userGoods.setGoodsName(goods.getName());
                userGoods.setGoodsUrl(goods.getUrl());
                userGoods.setScoreNow(goods.getScore());
                userGoods.setIsFlag(1);
                userGoods.setCreateTime(new Date());
                userGoods.setDeleted(0);
                userGoodsService.save(userGoods);
                map.put("baseSkin",goods.getUrl());
                map.put("baseSkinId",goods.getId());
            }

            List<UserGoods> touXiangUserGoodsList = userGoodsService.list(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_type",1).eq("is_flag",1));
            if(CollectionUtils.isNotEmpty(touXiangUserGoodsList)){
                map.put("baseHeadUrl",touXiangUserGoodsList.get(0).getGoodsUrl());
            }else{
                map.put("baseHeadUrl",null);
            }
            // 绑定部门一
            if(StringUtils.isNotEmpty(baseUser.getBranchOne())){
                UserBranch userBranch = new UserBranch();
                userBranch.setBranchId(CompanyUtils.getBranchMap(baseUser.getBranchOne()));
                userBranch.setBranchName(baseUser.getBranchOne());
                userBranch.setOpenId(openId);
                userBranch.setCreateDate(new Date());
                userBranchService.save(userBranch);
            }
            // 存在部门二则绑定
            if(StringUtils.isNotEmpty(baseUser.getBranchTwo())){
                UserBranch userBranch = new UserBranch();
                userBranch.setBranchId(CompanyUtils.getBranchMap(baseUser.getBranchTwo()));
                userBranch.setBranchName(baseUser.getBranchTwo());
                userBranch.setOpenId(openId);
                userBranch.setCreateDate(new Date());
                userBranchService.save(userBranch);
            }
            // 存在部门三则绑定
            if(StringUtils.isNotEmpty(baseUser.getBranchThree())){
                UserBranch userBranch = new UserBranch();
                userBranch.setBranchId(CompanyUtils.getBranchMap(baseUser.getBranchThree()));
                userBranch.setBranchName(baseUser.getBranchThree());
                userBranch.setOpenId(openId);
                userBranch.setCreateDate(new Date());
                userBranchService.save(userBranch);
            }
            // 等级
            UserLevel userLevel = new UserLevel();
            userLevel.setOpenId(openId);
            userLevel.setLevelName(0+"级");
            userLevel.setLevelId(1);
            userLevel.setCraeateTime(new Date());
            userLevelService.save(userLevel);

            map.put("userInfo",user);
            map.put("level",userLevel.getLevelId()-1);
            // 成就
            List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id",openId).orderByDesc("create_time"));
            if(CollectionUtils.isEmpty(userAchievementList)){
                map.put("achievement","未获成就");
            }else{
                map.put("achievement",userAchievementList.get(0).getAchievementName());
            }
            // 用户称号
            List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",openId).orderByDesc("create_time"));
            if(CollectionUtils.isEmpty(userHonorList)){
                UserHonor userHonor = new UserHonor();
                userHonor.setHonorId(0);
                userHonor.setHonorName("暂未获得段位");
                map.put("honorInfo",userHonor);
            }else{
                map.put("honorInfo",userHonorList.get(0));
            }
            // 用户部门
            List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",openId));
            if(CollectionUtils.isNotEmpty(userBranchList)){
                List<String> userBranchs= new ArrayList<String>();
                for(int i= 0;i< userBranchList.size();i++){
                    userBranchs.add(userBranchList.get(i).getBranchName());
                }
                map.put("userBranchs",userBranchs);
            }else{
                map.put("userBranchs",new ArrayList<>());
            }
            // 添加用户关联的积分表
            // 获取当前时间所属月份、年份
            Date date = DateUtil.date();
            // 获取日期
            int day = DateUtil.dayOfMonth(date);
            //获得月份，从0开始计数
            int month = DateUtil.month(date) + 1;
            //获得年的部分
            int year = DateUtil.year(date);
            Experience experience = new Experience();
            experience.setOpenId(openId);
            experience.setMonthExperience(null);
            experience.setCreateTime(new Date());
            experience.setUpdateTime(new Date());
            experience.setPartMonth(month);
            experience.setPartYear(year);
            String dayExperience = "0";
            if (day != 1) {
                for (int i = 1; i < day-1; i++) {
                    dayExperience += ",0";
                }
            }
            experience.setDayExperience(dayExperience);
            experienceService.save(experience);

            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("保存用户信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 返回json对象
     * @param key
     * @param value
     * @Auth: zhaowu
     * @Date: 2019/8/9 15:43
     */
    private Result returnJSON(String key, String value){
        JSONObject res = new JSONObject();
        res.put(key, value);
        return Result.newSuccess(res);
    }


    /**
     * 用户等级
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/saveLevel")
    @ResponseBody
    public Result saveUser(){
        try {
            for(int i=0;i<101;i++){
                if(i == 0){
                    Level level = new Level();
                    level.setValue(99);
                    level.setName("0级");
                    level.setCraeateTime(new Date());
                    levelService.save(level);
                    continue;
                }

                int num = 0;

                int j = 0+i-1;
                int x = j*i/2;
                int o = i*100;
                int k = x+o;
                Level level = new Level();
                level.setLevel(i);
                level.setValue(k);
                level.setName(i+"级");
                level.setCraeateTime(new Date());
                levelService.save(level);
                continue;

            }
            return Result.newSuccess();
        }catch (Exception e){
            log.error("wxLogin error:{}", e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 用户等级（二期）
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    // @PostMapping("/saveLevel01")
    @ResponseBody
    public Result saveUser01(){
        try {
            for(int i = 0;i < 201; i++){
                if(i == 0){
                    Level level = new Level();
                    level.setValue(0);
                    level.setName("0级");
                    level.setCraeateTime(new Date());
                    levelService.save(level);
                    continue;
                }

                int num = 0;

                int j = 0+i-1;
                int x = j*i/2;
                int o = i*100;
                int k = x+o;
                Level level = new Level();
                level.setLevel(i);
                level.setValue(k);
                level.setName(i+"级");
                level.setCraeateTime(new Date());
                levelService.save(level);
                continue;
            }

            return Result.newSuccess();
        }catch (Exception e){
            log.error("创建用户等级、经验值信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 删除基本用户信息
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/deleteUser")
    @ResponseBody
    @Transactional
    public Result deleteUser(@RequestParam(value = "openId",required = false) String openId){
        try {
            if(StringUtils.isEmpty(openId)){
                return Result.newFaild("未获取到参数信息");
            }
            User user = userService.getOne(new QueryWrapper<User>().eq("open_id", openId));
            if (null != user) {
                baseUserService.remove(new QueryWrapper<BaseUser>().eq("phone", user.getMobile()));
                userService.remove(new QueryWrapper<User>().eq("open_id", user.getOpenId()));
                userAchievementService.remove(new QueryWrapper<UserAchievement>().eq("open_id", user.getOpenId()));
                userBranchService.remove(new QueryWrapper<UserBranch>().eq("open_id", user.getOpenId()));
                userActivityService.remove(new QueryWrapper<UserActivity>().eq("open_id", user.getOpenId()));
                userDayGameLogService.remove(new QueryWrapper<UserDayGameLog>().eq("open_id", user.getOpenId()));
                userGoodsService.remove(new QueryWrapper<UserGoods>().eq("open_id", user.getOpenId()));
                userGoodsAddressService.remove(new QueryWrapper<UserGoodsAddress>().eq("open_id", user.getOpenId()));
                userHonorService.remove(new QueryWrapper<UserHonor>().eq("open_id", user.getOpenId()));
                userLevelService.remove(new QueryWrapper<UserLevel>().eq("open_id", user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("friend_open_id", user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("room_open_id", user.getOpenId()));

                userTeamVsTeamLogService.remove(new QueryWrapper<UserTeamVsTeamLog>().eq("open_id", user.getOpenId()));
                signService.remove(new QueryWrapper<Sign>().eq("open_id", user.getOpenId()));
                sevenSignService.remove(new QueryWrapper<SevenSign>().eq("open_id", user.getOpenId()));
                manguanService.remove(new QueryWrapper<Manguan>().eq("open_id", user.getOpenId()));
                hotLogService.remove(new QueryWrapper<HotLog>().eq("open_id", user.getOpenId()));
                feedbackService.remove(new QueryWrapper<Feedback>().eq("open_id", user.getOpenId()));
                alertTipsService.remove(new QueryWrapper<AlertTips>().eq("open_id", user.getOpenId()));
                experienceService.remove(new QueryWrapper<Experience>().eq("open_id", user.getOpenId()));
            }

            return Result.newSuccess(null);
        }catch (Exception e){
            log.error("删除基本用户信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 删除游戏用户信息
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/deleteGameUser")
    @ResponseBody
    @Transactional
    public Result deleteGameUser(@RequestParam(value = "openId",required = false) String openId){
        try {
            if(StringUtils.isEmpty(openId)){
                return Result.newFaild("未获取到参数信息");
            }
            User user = userService.getUserByOpenId(openId);
            if(null != user) {
                userService.remove(new QueryWrapper<User>().eq("open_id", user.getOpenId()));
                userAchievementService.remove(new QueryWrapper<UserAchievement>().eq("open_id", user.getOpenId()));
                userBranchService.remove(new QueryWrapper<UserBranch>().eq("open_id", user.getOpenId()));
                userActivityService.remove(new QueryWrapper<UserActivity>().eq("open_id", user.getOpenId()));
                userDayGameLogService.remove(new QueryWrapper<UserDayGameLog>().eq("open_id", user.getOpenId()));
                userGoodsService.remove(new QueryWrapper<UserGoods>().eq("open_id", user.getOpenId()));
                userGoodsAddressService.remove(new QueryWrapper<UserGoodsAddress>().eq("open_id", user.getOpenId()));
                userHonorService.remove(new QueryWrapper<UserHonor>().eq("open_id", user.getOpenId()));
                userLevelService.remove(new QueryWrapper<UserLevel>().eq("open_id", user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("friend_open_id", user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("room_open_id", user.getOpenId()));

                // userThreeTeamLogService.remove(new QueryWrapper<UserThreeTeamLog>().eq("open_id", user.getOpenId()));
                userTeamVsTeamLogService.remove(new QueryWrapper<UserTeamVsTeamLog>().eq("open_id", user.getOpenId()));
                signService.remove(new QueryWrapper<Sign>().eq("open_id", user.getOpenId()));
                sevenSignService.remove(new QueryWrapper<SevenSign>().eq("open_id", user.getOpenId()));
                manguanService.remove(new QueryWrapper<Manguan>().eq("open_id", user.getOpenId()));
                hotLogService.remove(new QueryWrapper<HotLog>().eq("open_id", user.getOpenId()));
                feedbackService.remove(new QueryWrapper<Feedback>().eq("open_id", user.getOpenId()));
                alertTipsService.remove(new QueryWrapper<AlertTips>().eq("open_id", user.getOpenId()));
                experienceService.remove(new QueryWrapper<Experience>().eq("open_id", user.getOpenId()));
            }

            return Result.newSuccess(null);
        }catch (Exception e){
            log.error("删除游戏用户信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 获取用户升至下一成就所需条件
     *
     * 1 笨鸟先飞  连续登陆平台1个星期，且完成每天的每日答题
     * 2 活跃达人  连续登陆平台1个月，且完成每天的每日答题
     * 3 孜孜不倦  连续登陆平台3个月，且完成每天的每日答题
     * 4 一骑绝尘  连续登陆平台6个月，且完成每天的每日答题
     * 5 纵横天下  连续登陆平台1年，且完成每天的每日答题
     * 6 单挑之王  1v1赛中一天内与不同的人比赛连续获胜10场
     * 7 大满贯得主  一天中同时完成每日答题和答题挑战，且分数都及格（100分制）
     * 8 超级大满贯得主 连续7天同时完成每日答题和答题挑战，且分数都及格（100分制）
     */
    private String queryNextAchievementCondition(String openId, Integer achievementId) {
        // 根据当前用户所获取成就情况分类判断
        if (achievementId >= 1 && achievementId <= 4) {
            //查询登录记录
            List<Sign> list = signService.list(new QueryWrapper<Sign>().eq("open_id", openId).orderByDesc("sign_date"));
            if (list.size() == 0) {
                // 用户已获得过成就，说明用户至少签到过
                throw new BusinessException("获取用户签到数据出错");
            } else {
                // 获取当前用户已经连续多少天完成答题
                Integer isAnswerDay = list.get(0).getIsAnswerDay();
                // 获取对应下一成就所需完成答题天数
                Set<Integer> integers = dayWanCheng_ChengJiuMap.keySet();
                Integer count = 0;
                for (Integer key : integers) {
                    if (dayWanCheng_ChengJiuMap.get(key) == achievementId + 1 ) {
                        count = key;
                    }
                }
                String condition = "您还需连续登陆平台"+ (count - isAnswerDay) +"天，且完成每天的每日答题";
                return condition;
            }
        } else if (achievementId == 5) {
            // 查询 1v1赛情况
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = sdf.format(new Date());
            List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.getGameLogByOpenId(dateString,openId);
            // 不同对手连续获胜厂数
            Integer winCount = 0;
            List<Achievement> AchievementList = achievementService.list(null);
            if (userOneVsOneLogList.size() == 0) {
                // 直接按相应成就描述返回
                return AchievementList.get(achievementId).getRemarks();
            } else {
                // 遍历1v1赛记录
                for (UserOneVsOneLog userOneVsOneLog : userOneVsOneLogList) {
                    if (openId.equals(userOneVsOneLog.getRoomOpenId())) {
                        // 本人是房主
                        if (userOneVsOneLog.getIsWin() == 1 || userOneVsOneLog.getIsWin() == 3) {
                            // 本人获得胜利、平局，连胜场次+1
                            winCount ++;
                        } else if (userOneVsOneLog.getIsWin() == 2) {
                            // 本人本场比赛失败,连续胜利场次归零
                            winCount = 0;
                        }
                    } else if (openId.equals(userOneVsOneLog.getFriendOpenId())) {
                        // 本人是受邀人员
                        if (userOneVsOneLog.getIsWin() == 2 || userOneVsOneLog.getIsWin() == 3) {
                            // 本人获得胜利、平局，连胜场次+1
                            winCount ++;
                        } else {
                            // 本人本场比赛失败,连续胜利场次归零
                            winCount = 0;
                        }

                    }
                }
                String condition = "您还需在1v1赛中一天内与其他不同的人比赛连续获胜"+ (10 - winCount) +"场";
                return condition;
            }

        } else if (achievementId == 6) {
            // 获取下一成就还需完成内容
            String content = this.dayGameLogFinish(openId, new Date());
            if ("当日任务已完成".equals(content)) {
                throw new BusinessException("现处成就不可能在同一天中同时完成每日答题和答题挑战，且分数都及格现象");
            } else {
                return content;
            }
        } else if (achievementId ==7) {
            // 获取当前时间
            Date date = DateUtil.date();
            // 连续完成相应任务天数
            Integer continuousFinishDay = 0;
            for (int i = 0; i < 7; i++) {
                // 当前往前推7天时间,并将时间往前推i天
                DateTime dateTime = DateUtil.offsetDay(date, -i);
                String content = this.dayGameLogFinish(openId,dateTime);
                if ("当日任务已完成".equals(content)) {
                    continuousFinishDay ++;
                }
                if (continuousFinishDay < i+1) {
                    return "您还需连续" +(7-continuousFinishDay)+ "天同时完成每日答题和答题挑战，且分数都及格";
                }
            }
        } else {
            throw new BusinessException("成就称号id传输错误！");
        }
        return null;
    }


    /**
     * 获取给定时间当天 每日答题和答题挑战同时完成（均及格）情况
     */
    public String dayGameLogFinish(String openId,Date date) {
        // 时间处理
        // 给定日期的开始时间  2021.08.12  00:00:00
        Date dayBeginDate = DateUtil.beginOfDay(date);
        // 给定日期的结束时间  2021.08.12  23:59:59
        Date dayEndDate = DateUtil.endOfDay(date);
        // 给定时间日期 2021.08.12
        String formatDate = DateUtil.formatDate(date);
        date = DateUtil.parse(formatDate,"yyyy-MM-dd");

        // 查询给定时间当日'每日答题'情况
        List<UserDayGameLog> userDayGameLogs = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().eq("open_id",openId).eq("day_game_date", date));
        // 查询给定时间当日'答题挑战'情况（活动赛）
        UserActivity userActivity = userActivityService.getOne(new QueryWrapper<UserActivity>().eq("open_id", openId).gt("create_time", dayBeginDate).lt("create_time",dayEndDate));
        // 判断给定时间当天所有每日答题是否存在及格情况
        Boolean dayGameFinish = false;
        for (UserDayGameLog userDayGameLog : userDayGameLogs) {
            if (userDayGameLog.getScore() >= 60) {
                dayGameFinish = true;
                break;
            }
        }
        // 当天还需完成内容
        String content = "";
        if (dayGameFinish) {
            if (userActivity.getScore() >= 60) {
                // 逻辑上执行不到此步，如果同一天，每日答题与答题挑战均及格，那等级已经为'大满贯得主',而非现在的'单挑之王'
                throw new BusinessException("当日任务已完成");
            } else {
                content = "您还需在当天中完成'答题挑战'且及格";
            }
        } else {
            if (userActivity.getScore() >= 60) {
                content = "您还需在当天中完成'每日答题'且及格";
            } else {
                content = "您还需一天中同时完成每日答题和答题挑战，且分数都及格";
            }
        }

        return content;
    }

}
