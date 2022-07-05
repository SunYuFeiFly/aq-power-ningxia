package com.wangchen.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 用户成就分数计算
 * @ Package: com.wangchen.utils
 * @ ClassName: computeUtils
 * @ Author: 2
 * @ Description: ${description}
 * @ Date: 2020/7/16 10:16
 * @ Version: 1.0

 */
@Component
@Slf4j
public class ComputeUtils {

    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserService userService;
    @Autowired
    private HotLogService hotLogService;
    @Autowired
    private HonorService honorService;
    @Autowired
    private UserHonorService userHonorService;
    @Autowired
    private AlertTipsService alertTipsService;

    /**
     * 活动赛 计算成就以及经验值添加
     * @ param openId
     * @ param score
     * @ param user
     */
    public void computeGame(String openId, Integer score, User user){
        log.debug("活动赛 计算成就以及经验值添加..............*********************************");
        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",openId));
        //用户当前等级经验峰值
        Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);
        //获取分数对应的经验值和塔币
//        Integer[] jiangli = com.wangchen.common.constant.Constants.socreMap.get(score.intValue());
        Integer[] jiangli = com.wangchen.common.constant.Constants.newActivitySocreMap.get(score);
        //当前分数加本次应该加的分数还小于本等级的峰值， 那就不用升级
        if(value > (user.getPresentExperience() + jiangli[0])){
            //更新最后添加经验时间
            userLevel.setUpdateTime(new Date());
            userLevelService.updateById(userLevel);

            //塔币增加
            user.addAllExperience(jiangli[0]);
            user.setAllCoin(user.getAllCoin()+ jiangli[1]);
            userService.updateById(user);

        }else{
            // 二期，坐高等级由100级变更为120级
            if(userLevel.getLevelId() != 121){

                //塔币增加
                user.setAllCoin(user.getAllCoin()+ jiangli[1]);
                //经验增加
                user.addAllExperience(jiangli[0]);
                userService.updateById(user);

                //先更改等级
                userLevel.setLevelId(userLevel.getLevelId()+1);
                userLevel.setLevelName((userLevel.getLevelId()-1)+"级");
                userLevel.setNowExperience(0);
                userLevel.setUpdateTime(new Date());
                userLevelService.updateById(userLevel);

                //每日公告上面的恭喜
                HotLog hotLog = new HotLog();
                hotLog.setOpenId(openId);
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

                //塔币增加
                user.setAllCoin(user.getAllCoin()+ jiangli[1]);
                //经验增加
                user.addAllExperience(jiangli[0]);
                user.setUpdateDate(new Date());
                userService.updateById(user);
            }
        }
    }

    /**
     * 单人赛和团队赛 经验值 等级 塔币 增加
     * @ param openId
     * @ param experience 本次加的经验值
     * @ param coin 本次加的塔币
     * @ param user 用户信息
     */
    public void computeGame2(String openId,int experience, int coin, User user){
        log.debug("单人赛和团队赛 经验值 塔币 增加...");
        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",openId));
        //用户当前等级经验峰值
        Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);
        //获取分数对应的经验值和塔币

        //当前分数加本次应该加的分数还小于本等级的峰值， 那就不用升级
        if(value > (user.getPresentExperience() +experience)){
            //塔币增加
            user.setAllCoin(user.getAllCoin()+coin);
            //经验增加
            user.addAllExperience(experience);
            user.setUpdateDate(new Date());
            userService.updateById(user);
        }else{
            // 二期，坐高等级由100级变更为120级
            if(userLevel.getLevelId() != 121){

                //塔币增加
                user.setAllCoin(user.getAllCoin()+coin);
                //经验增加
                user.addAllExperience(experience);
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
                hotLog.setOpenId(openId);
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
                //塔币增加
                user.setAllCoin(user.getAllCoin()+coin);
                //经验增加
                user.addAllExperience(experience);
                user.setUpdateDate(new Date());
                userService.updateById(user);

                //更新最后添加经验时间
                userLevel.setUpdateTime(new Date());
                userLevelService.updateById(userLevel);


            }
        }
    }


    /**
     * 答题交战（活动赛），经验值、塔币更新，等级
     */
    public void computeGame02(String openId, Integer score, User user) {
        log.debug("活动赛 计算成就以及经验值添加..............*********************************");
        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",openId));
        //用户当前等级经验峰值
        Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);
        //获取分数对应的经验值和塔币
        Integer[] jiangli = com.wangchen.common.constant.Constants.newActivitySocreMap.get(score);
        //当前分数加本次应该加的分数还小于本等级的峰值， 那就不用升级
        if(value > (user.getPresentExperience() + jiangli[0])){
            //更新最后添加经验时间
            userLevel.setUpdateTime(new Date());
            userLevelService.updateById(userLevel);

            // 经验值、塔币更新
            user.addAllExperience(jiangli[0]);
            user.setAllCoin(user.getAllCoin()+ jiangli[1]);
            user.setUpdateDate(new Date());
            userService.updateById(user);

        }else{
            // 二期，坐高等级由100级变更为120级
            if(userLevel.getLevelId() != 121){
                //塔币增加
                user.setAllCoin(user.getAllCoin()+ jiangli[1]);
                //经验增加
                user.addAllExperience(jiangli[0]);
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
                hotLog.setOpenId(openId);
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
                // 已经满级
                //更新最后添加经验时间
                userLevel.setUpdateTime(new Date());
                userLevelService.updateById(userLevel);

                //经验增加
                user.addAllExperience(jiangli[0]);
                //塔币增加
                user.setAllCoin(user.getAllCoin()+ jiangli[1]);
                userService.updateById(user);
            }
        }
    }
}