package com.wangchen.api;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.Constants;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.UserLevelUtils;
import com.wangchen.vo.SevenSignVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 签到接口
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/signapi")
public class SignApi {

    @Autowired
    private UserService userService;

    @Autowired
    private SevenSignService sevenSignService;

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
    private SignService signService;

    /**
     * 查看今天用户是否签到 0未签到 1已签到
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/queryUserIsSevenSign")
    @ResponseBody
    public Result queryUserIsSevenSign(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为：" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            SevenSign sevenSign = sevenSignService.getOne(new QueryWrapper<SevenSign>().eq("open_id",openId).eq("sign_date",Constants.sdf.format(new Date())));
            if(null == sevenSign){
                return Result.newSuccess(0);
            }
            return Result.newSuccess(1);
        }catch (Exception e){
            log.error("获取用户今天是否签到信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }

    /**
     * 清理签到
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/clearSevenSign")
    @ResponseBody
    public Result clearSevenSign(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为：" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            sevenSignService.remove(new QueryWrapper<SevenSign>().eq("open_id",openId));
            return Result.newSuccess("清理成功");
        }catch (Exception e){
            log.error("清理签到出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 用户签到
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/userSevenSign")
    @ResponseBody
    public Result userSevenSign(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为：" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            SevenSign sevenSign = sevenSignService.getOne(new QueryWrapper<SevenSign>().eq("open_id",openId).eq("sign_date",Constants.sdf.format(new Date())));
            if(null == sevenSign){
                sevenSign = new SevenSign();
                sevenSign.setOpenId(openId);
                sevenSign.setSignDate(new Date());
                sevenSign.setCreateTime(new Date());
                sevenSignService.save(sevenSign);

                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",openId));
                // 今天之前是连续第几天登录签到
                Integer continuousLoginDays = 0;
                DateTime newTime = DateUtil.date();
                // 判断给定时间当天是否登录（签到）
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    newTime = DateUtil.offsetDay(newTime, - 1);
                    String date = DateUtil.formatDate(newTime);
                    SevenSign sevenSign01 = sevenSignService.getOne(new QueryWrapper<SevenSign>().eq("open_id", openId).eq("sign_date", date));
                    if (sevenSign01 != null) {
                        continuousLoginDays ++;
                    }
                    if (continuousLoginDays == i) {
                        break;
                    }
                }
                // 获取当前用户签到经验值
                int experience = 0;
                if (0 == continuousLoginDays) {
                    experience = 1;
                } else {
                    experience = (continuousLoginDays + 1) % 7 == 0 ? 7 : (continuousLoginDays + 1) % 7;
                }
                //用户当前等级经验峰值
                Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);

                //如果经验值相加 小于峰值 那就是还不用升级
                if(value < (user.getPresentExperience().intValue() + experience)){
                    // 二期，对应最高等级由100级变更为120级
                    if(userLevel.getLevelId().intValue() != 121){
                        //先更改等级
                        userLevel.setLevelId(userLevel.getLevelId() + 1);
                        userLevel.setLevelName((userLevel.getLevelId()-1) + "级");
                        userLevel.setNowExperience(0);
                        userLevel.setUpdateTime(new Date());
                        userLevelService.updateById(userLevel);

                        user.addAllExperience(experience);
                        user.setAllCoin(user.getAllCoin() + 1);
                        user.setUpdateDate(new Date());
                        userService.updateById(user);

                        //每日公告上面的恭喜
                        HotLog hotLog = new HotLog();
                        hotLog.setOpenId(openId);
                        hotLog.setRemarks("恭喜"+user.getName()+"升到"+ userLevel.getLevelName());
                        hotLog.setCreateDate(new Date());
                        hotLogService.save(hotLog);
                    }else{
                        // 满级用户
                        userLevel.setUpdateTime(new Date());
                        userLevelService.updateById(userLevel);

                        user.setAllCoin(user.getAllCoin() + 1);
                        user.addAllExperience(experience);
                        user.setUpdateDate(new Date());
                        userService.updateById(user);
                    }
                    Integer honorId = com.wangchen.common.constant.Constants.LEVLE_HONOR_MAP.get(userLevel.getLevelId()-1);
                    //可获得称号
                    if(null != honorId){
                        //添加称号信息
                        Honor honor = honorService.getOne(new QueryWrapper<Honor>().eq("id",honorId));
                        UserHonor userHonor = new UserHonor();
                        userHonor.setHonorId(honorId);
                        userHonor.setHonorName(honor.getName());
                        userHonor.setOpenId(openId);
                        userHonor.setCreateTime(new Date());
                        userHonorService.save(userHonor);

                        //每日公告上面的恭喜
                        HotLog hotLog1 = new HotLog();
                        hotLog1.setOpenId(openId);
                        hotLog1.setRemarks("恭喜"+user.getName()+"获得"+honor.getName()+"称号");
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
                }else{
                    //更新最后添加经验时间
                    userLevel.setUpdateTime(new Date());
                    userLevelService.updateById(userLevel);

                    user.addAllExperience(2);
                    user.setAllCoin(user.getAllCoin() + 1);
                    user.setUpdateDate(new Date());
                    userService.updateById(user);
                }
            }

            return Result.newSuccess("签到成功");
        }catch (Exception e){
            log.error("获取用户签到信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 展示用户7天签到 (二期)
     * @param openId 用户id
     * @return 用户7天签到信息
     */
    @PostMapping("/querySevenSign")
    @ResponseBody
    public Result querySevenSign(@RequestParam(value = "openId",required = false) String openId) {
        try {
            //今天之前是连续第几天登录签到
            Integer continuousLoginDays = 0;
            DateTime newTime = DateUtil.date();
            Boolean isSign = false;
            // 判断给定时间当天是否登录（签到）
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                newTime = DateUtil.offsetDay(newTime, - 1);
                String date = DateUtil.formatDate(newTime);
                SevenSign sevenSign = sevenSignService.getOne(new QueryWrapper<SevenSign>().eq("open_id", openId).eq("sign_date", date));
                if (sevenSign != null) {
                    continuousLoginDays ++;
                    isSign = true;
                }
                if (continuousLoginDays == i) {
                    break;
                }
            }
            if (isSign) {
                continuousLoginDays = continuousLoginDays % 7;
            }

            List<SevenSignVo> sevenSignVos = new ArrayList<>();
            if (continuousLoginDays == 0) {
                for (int i = 1; i <= 7; i++) {
                    SevenSignVo sevenSignVo = new SevenSignVo();
                    sevenSignVo.setCoin(1);
                    sevenSignVo.setExperience(i);
                    sevenSignVo.setIsFlag(2);
                    if (i == 1) {
                        sevenSignVo.setIsNow(0);
                    } else {
                        sevenSignVo.setIsNow(1);
                    }
                    sevenSignVos.add(sevenSignVo);

                }
            } else {
                for (int i = 1; i <= 7; i++) {
                    SevenSignVo sevenSignVo = new SevenSignVo();
                    sevenSignVo.setCoin(1);
                    sevenSignVo.setExperience(i);
                    if (continuousLoginDays >= i) {
                        sevenSignVo.setIsNow(1);
                        sevenSignVo.setIsFlag(1);
                    } else if (continuousLoginDays == i-1) {
                        sevenSignVo.setIsNow(0);
                        sevenSignVo.setIsFlag(2);
                    } else {
                        sevenSignVo.setIsNow(1);
                        sevenSignVo.setIsFlag(2);
                    }
                    sevenSignVos.add(sevenSignVo);
                }
            }

            return Result.newSuccess(sevenSignVos);
        } catch (Exception e) {
            log.error("获取用户7天签到信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }


    /**
     * 获取用户今天是第几天登录
     */
    public Integer queryContinuousLoginDays(String openId, String time) {
        //今天之前是连续第几天登录
        Integer continuousLoginDays = 0;
        Date newTime = DateUtil.parse(time);
        // 判断给定时间当天是否登录（签到）
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            newTime = DateUtil.offsetDay(newTime, -i);
            Sign sign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",openId).eq("sign_date", newTime));
            if (sign != null) {
                continuousLoginDays ++;
            }
            if (continuousLoginDays == i) {
                break;
            }
        }
        continuousLoginDays = continuousLoginDays % 7 == 0 ? 7 :  continuousLoginDays % 7;
        return continuousLoginDays;
    }


    /**
     * 软件维护期间：员工签到、连续答题补全完善
     * @param startData 开始补签时间 （格式2021-10-01）
     */
    // @PostMapping("complementSign")
    @ResponseBody
    public Result complementSign(@RequestParam(value = "startData",required = false,defaultValue = "") String startData) {
        if (StringUtils.isEmpty(startData)) {
            return Result.newFaild("签到等相关信息补全开始时间不能为空");
        }
        Date startTime = DateUtil.beginOfDay(DateUtil.parse(startData));
        Date endTime = DateUtil.endOfDay(DateUtil.parse(startData));
        Date now = DateUtil.beginOfDay(DateUtil.date());
        Date end = DateUtil.endOfDay(DateUtil.date());
        if (startTime.getTime() > end.getTime()) {
            return Result.newFaild("签到等相关信息补全开始时间不能在当天之后");
        }
        // 先清除给定时间签到、七天签到表
        sevenSignService.remove(new QueryWrapper<SevenSign>().gt("create_time",startTime));
        signService.remove(new QueryWrapper<Sign>().gt("create_time",startTime));
        // 获取所有游戏用户
        List<User> userList = userService.list(new QueryWrapper<User>().eq("deleted",0));
        // 补全签到、七天签到表（至当前时间所属当日）
        long betweenDay = DateUtil.between(startTime,now, DateUnit.DAY);
        startTime = DateUtil.offsetDay(DateUtil.beginOfDay(DateUtil.parse(startData)),-1);
        endTime = DateUtil.endOfDay(startTime);

        Map<String,Integer> map = new HashMap<String,Integer>();
        for (int i = 0; i < userList.size(); i++) {
            if (!StringUtils.isEmpty(userList.get(i).getMobile())) {
                User user = userList.get(i);
                Sign sign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",user.getOpenId()).gt("create_time",startTime).lt("create_time",endTime));
                if (null == sign) {
                    map.put(userList.get(i).getOpenId(),0);
                } else {
                    map.put(userList.get(i).getOpenId(),sign.getIsAnswerDay());
                }
            }
        }
        for (int i = 0; i < betweenDay +1; i++) {
            startTime = DateUtil.offsetDay(DateUtil.parse(startData),i -1);
            endTime = DateUtil.endOfDay(startTime);
            // 补全签到
            for (int j = 0; j < userList.size(); j++) {
                if (!StringUtils.isEmpty(userList.get(j).getMobile())) {
                    User user = userList.get(j);
                    Sign newSign = new Sign();
                    newSign.setIsAnswerDay(map.get(user.getOpenId()) +i +1);
                    newSign.setOpenId(user.getOpenId());
                    newSign.setCreateTime(DateUtil.offsetDay(DateUtil.parseDate(startData),i));
                    newSign.setSignDate(DateUtil.offsetDay(DateUtil.parseDate(startData),i));
                    newSign.setDeleted(0);
                    signService.save(newSign);

                    SevenSign sevenSign = new SevenSign();
                    sevenSign.setOpenId(user.getOpenId());
                    sevenSign.setCreateTime(DateUtil.offsetDay(DateUtil.parseDate(startData),i));
                    sevenSign.setSignDate(DateUtil.offsetDay(DateUtil.parseDate(startData),i));
                    sevenSignService.save(sevenSign);
                }
            }
        }

        return Result.newSuccess();
    }

}
