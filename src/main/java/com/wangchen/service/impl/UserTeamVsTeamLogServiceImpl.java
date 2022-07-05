package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.UserTeamVsTeamLog;
import com.wangchen.mapper.UserTeamVsTeamLogMapper;
import com.wangchen.service.UserTeamVsTeamLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserTeamVsTeamLogServiceImpl  extends ServiceImpl<UserTeamVsTeamLogMapper, UserTeamVsTeamLog> implements UserTeamVsTeamLogService {

    /**
     * 查询用户当前今天完成团队赛情况（二期用于显示完成情况和经验获得情况）
     * @param openId 用户id
     * @return 用户当前完成团队赛情况
     *
     * 规则：1场 胜者：10经验5塔币 输者：5经验0塔币
     */
    @Override
    @Transactional
    public Result userIsTeamVsTeam(String openId) {
        // 最多能获得的经验；
        Integer allExp = 10;
        // 已经获得的经验；
        Integer nowExp = 0;
        // 最多能获得的塔币；
        Integer allCoin = 5;
        // 已经获得的塔币；
        Integer nowCoin = 0;
        // 今天对战次数；
        Integer vsCount = 0;
        // 都有经验和塔币的对战总次数（服务器可写死为1）；
        Integer vsECNum = 1;
        // 只有经验的对战总次数（服务器可写死为1）
        Integer vsENum = 1;

        List<UserTeamVsTeamLog> userTeamVsTeamLogList = this.list(new QueryWrapper<UserTeamVsTeamLog>()
                .eq("open_id",openId)
                .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));

        //团队赛对战时间正序
        List<UserTeamVsTeamLog> userTeamVsTeamLogs = userTeamVsTeamLogList.stream().sorted(Comparator.comparing(UserTeamVsTeamLog::getCreateTime)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(userTeamVsTeamLogs)){
            for (UserTeamVsTeamLog userTeamVsTeamLog : userTeamVsTeamLogs) {
                vsCount ++;
                // 根据答题次数计算不同的积分塔币
                if (vsCount == 1) {
                    // 只有第一次答题有奖励
                    if (userTeamVsTeamLog.getIsWin() == 1 || userTeamVsTeamLog.getIsWin() == 3)  {
                        nowExp += 10;
                        nowCoin += 5;
                    }
                    if(userTeamVsTeamLog.getIsWin() == 2){
                        nowExp += 5;
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
}
