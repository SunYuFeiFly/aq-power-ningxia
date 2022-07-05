package com.wangchen.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.WxConstant;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.WxUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信功能
 */

@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/wx_ready")
public class WxApi_ready {

    @Autowired
    private UserService userService;

    @Autowired
    private SignService signService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserGoodsService userGoodsService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserActivityService userActivityService;

    /**
     * 微信登录获取token
     * @param code 微信身份code
     * @Auth: zhaowu
     * @Date: 2019/8/9 14:38
     */
    @PostMapping("/wxLogin")
    @ResponseBody
    public Result wxLogin(@RequestParam(value = "code",required = false) String code){
        try {
            log.info("wxLogin.code:{}",code);
            String content = WxUtil.doGet(WxConstant.appid, WxConstant.secret, code);
            log.info("WxUtil.doGet.result:{}", content);//打印返回的信息
            JSONObject contentJSON = JSONObject.parseObject(content);
//            String openId = contentJSON.getString("openid");
            String openId = "";
            String sessionKey = contentJSON.getString("session_key");
//            if (StringUtils.isEmpty(openId)){
//                String errCode = contentJSON.getString("errcode");
//                String errMsg = contentJSON.getString("errmsg");
//                return Result.newFail(errCode, errMsg);
//            }
            synchronized (WxApi_ready.class) {
                // 轮流获取openId
                List<User> userList = userService.list(new QueryWrapper<User>().like("name", "测试").orderByAsc("id"));
                if (null != userList) {
                    User tempUser = userList.get(Constants.COUNT % userList.size());
                    Constants.COUNT++;
                    openId = tempUser.getOpenId();
                    // 获取token String token = UUID.randomUUID().toString();
//                    User user = userService.getUserByOpenId(tempUser.getOpenId());
//                    if(null == user){
//                        user = new User();
//                        user.setOpenId(openId);
//                        user.setRegisteredTime(new Date());
//                        // user.setSessionKey(sessionKey);
//                        userService.save(user);
//                    }

                    Sign sign = signService.getOne(new QueryWrapper<Sign>().eq("open_id", openId).eq("sign_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
                    if(null == sign){
                        //签到
                        sign =new Sign();
                        sign.setOpenId(openId);
                        sign.setSignDate(new Date());
                        sign.setIsAnswerDay(0);
                        sign.setCreateTime(new Date());
                        sign.setDeleted(0);
                        signService.save(sign);
                    }
                }
            }

            return returnJSON("openId",openId);
        }catch (Exception e){
            log.error("微信登录获取token出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     * @param json 用户身份json数据
     * @param openId 用户id
     * @Auth: zhaowu
     * @Date: 2019/8/9 14:39
     */
    @RequestMapping("/getUserInfo")
    @ResponseBody
    public Result getUserInfo(@RequestParam(value = "json",required = false) String json,
                              @RequestParam(value = "openId",required = false) String openId) throws ParseException {
        log.info("getUserInfo openId:{},>>> json:{}", openId, json);
        if (org.springframework.util.StringUtils.isEmpty(json)){
            return Result.newFaild("没有获取到用户信息");
        }
        if (org.springframework.util.StringUtils.isEmpty(openId)){
            return Result.newFaild("openId为空");
        }
        User user = userService.getUserByOpenId(openId);
        if (user == null){
            return Result.reLogin();
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(json);
            String nickName = jsonObject.getString("nickName");
            String avatarUrl = jsonObject.getString("avatarUrl");
            user.setNickName(filterEmoji(nickName));
            user.setAvatar(avatarUrl);
            user.setUpdateDate(new Date());
            userService.updateById(user);

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
                    for(int i= 0;i< userBranchList.size();i++){
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
            user.setName(user.getNickName());
            map.put("userInfo",user);
            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("登录获取用户信息出错，错误信息: {}",e);
            return Result.newFaild("登录错误");
        }
    }


    /**
     * @Author:yinguang
     * @Description:将表情转为空
     * @Date:18:02 2019/9/21
     */
    public static String filterEmoji(String source) {
        if (source != null && source.length() > 0) {
            return source.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");
        } else {
            return source;
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
                throw new BusinessException("获取用户签到数据出错！");
            } else {
                // 获取当前用户已经连续多少天完成答题
                Integer isAnswerDay = list.get(0).getIsAnswerDay();
                // 获取对应下一成就所需完成答题天数
                Set<Integer> integers = Constants.dayWanCheng_ChengJiuMap.keySet();
                Integer count = 0;
                for (Integer key : integers) {
                    if (Constants.dayWanCheng_ChengJiuMap.get(key) == achievementId + 1 ) {
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
