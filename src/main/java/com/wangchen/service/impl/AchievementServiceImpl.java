package com.wangchen.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.mapper.AchievementMapper;
import com.wangchen.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.AchievementVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-04
 */
@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private SignService signService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private ManguanService manguanService;

    /**
     * 获取用户成就信息（二期）
     * @param openId 用户id
     * @return 用户成就信息
     */
    @Override
    @Transactional
    public List<AchievementVo> achievementService(String openId) {
        //所有成就
        List<Achievement> achievementList = this.list(new QueryWrapper<Achievement>());
        //vo里面加了3个值
        List<AchievementVo> achievementVoList = new ArrayList<AchievementVo>();
        List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id",openId));
        Set<Integer> userAchievementSet = new HashSet<Integer>();
        for(UserAchievement userAchievement : userAchievementList){
            userAchievementSet.add(userAchievement.getAchievementId());
        }
        for (int i = 0; i < achievementList.size(); i++) {
            AchievementVo achievementVo = new AchievementVo();
            Achievement achievement = achievementList.get(i);
            BeanUtils.copyProperties(achievement,achievementVo);
            achievementVo.setBackImageUrl(Constants.ACHIEVEMENT_IMAGE_ADDRESS + "backImage_0"+ (i+1) +".png");
            if(userAchievementSet.contains(achievement.getId())){
                achievementVo.setIsHas(1);
                achievementVo.setImageUrl(Constants.ACHIEVEMENT_IMAGE_ADDRESS + "image_0"+ (i+1) +".png");
                if(achievement.getId().intValue() == userAchievementList.get(userAchievementList.size()-1).getAchievementId().intValue()){
                    achievementVo.setIsZuiHou(1);
                }else{
                    achievementVo.setIsZuiHou(0);
                }
                achievementVo.setCondition("已达成");
            }else{
                achievementVo.setIsHas(0);
                achievementVo.setIsZuiHou(0);
                achievementVo.setImageUrl(null);
            }

            achievementVoList.add(achievementVo);
        }
        //获得condition
        //成就1-5
        Integer a_id = 0;
        for(;a_id<achievementVoList.size()&&a_id<5;a_id++){
            if(achievementVoList.get(a_id).getIsHas()==0){
                Sign zuotianSign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",openId).eq("sign_date", DateUtils.getZuoTianDay()));
                Integer isAnswerDay = 0;
                if(null != zuotianSign){
                    isAnswerDay = zuotianSign.getIsAnswerDay();
                };
                Sign jintianSign = signService.getOne(new QueryWrapper<Sign>().eq("open_id",openId).eq("sign_date",Constants.SDF_YYYY_MM_DD.format(new Date())));
                if(null != jintianSign){
                    if(jintianSign.getIsAnswerDay().intValue() != 0){
                        isAnswerDay = jintianSign.getIsAnswerDay().intValue();
                    };
                };
                for(;a_id<achievementVoList.size()&&a_id<5;a_id++){
                    achievementVoList.get(a_id).setCondition("还需连续登陆平台"+
                            (Constants.chengJiu_DayWanChengMap.get(a_id+1)-isAnswerDay)
                            +"天，且完成每天的每日答题");
                }
            }
        }
        //成就6
        a_id = 5;
        if(achievementVoList.get(a_id).getIsHas()==0){
            // 查询 1v1赛情况
            List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.getGameLogByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),openId);
            // 不同对手连续获胜场次
            Integer winCount = 0;
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
            achievementVoList.get(a_id).setCondition("还需连胜"+ (10 - winCount) +"场");
        };
        //成就7-8（建议不用考虑活动赛不及格、当前没有活动赛的情况）
        //这里可以查一下今天的活动赛是否及格了，活动赛每天只能答一次，没及格就从明天从头算
        a_id = 6;
        if(achievementVoList.get(a_id).getIsHas()==0){
            achievementVoList.get(a_id).setCondition("还需1天");
            achievementVoList.get(a_id+1).setCondition("还需7天");
        }else if(achievementVoList.get(a_id+1).getIsHas()==0){
            Integer count = 0;
            Manguan zuoTianManguan = manguanService.getOne(new QueryWrapper<Manguan>().eq("open_id",openId)
                    .eq("answer_date", DateUtils.getZuoTianDay()));
            if(null!= zuoTianManguan){
                count = zuoTianManguan.getHowNum();
            }else{
                count = 0;
            }
            Manguan jinTianManguan = manguanService.getOne(new QueryWrapper<Manguan>().eq("open_id",openId)
                    .eq("answer_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
            if(null!= jinTianManguan){
                if(jinTianManguan.getHowNum()>0){
                    count = jinTianManguan.getHowNum();
                };
            };
            achievementVoList.get(a_id+1).setCondition("还需"+(7-count)+"天");
        };

        return achievementVoList;
    }
}