package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.UserOneVsOneLog;
import com.wangchen.mapper.UserOneVsOneLogMapper;
import com.wangchen.service.UserOneVsOneLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 1v1对战记录表 服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-16
 */
@Service
public class UserOneVsOneLogServiceImpl extends ServiceImpl<UserOneVsOneLogMapper, UserOneVsOneLog> implements UserOneVsOneLogService {

    @Autowired
    private UserOneVsOneLogMapper userOneVsOneLogMapper;

    @Override
    public boolean getFriendGameLog(String dateStr, String roomOpenId, String friendOpenId) {
        List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogMapper.getFriendGameLog(dateStr,roomOpenId,friendOpenId);
        if(CollectionUtils.isEmpty(userOneVsOneLogList)){
            return false;
        }
        return true;
    }


    @Override
    public Integer getGameLogNumByOpenId(String dateStr, String openId) {
        Integer num = userOneVsOneLogMapper.getGameLogNumByOpenId(dateStr,openId);
        return num;
    }


    @Override
    public List<UserOneVsOneLog> getGameLogByOpenId(String dateStr, String openId) {
        return userOneVsOneLogMapper.getGameLogByOpenId(dateStr,openId);
    }


    @Override
    public List<String> getGameGtTenNumUser(String dateStr) {
        return userOneVsOneLogMapper.getGameGtTenNumUser(dateStr);
    }


    /**
     * 查询用户当前今天完成1v1情况（二期用于显示完成情况和经验获得情况）
     * @param openId 用户id
     * @return 用户当前完成1v1情况
     *
     * 规则：前五场 胜者：2经验2塔币 输者：1经验0塔币   后5场：胜者：2经验0塔币 输者：1经验0塔币
     */
    @Override
    @Transactional
    public Result userIsOneVsOne(String openId) {
        // 最多能获得的经验；
        Integer allExp = 2*10;
        // 已经获得的经验；
        Integer nowExp = 0;
        // 最多能获得的塔币；
        Integer allCoin = 2*5;
        // 已经获得的塔币；
        Integer nowCoin = 0;
        // 今天对战次数；
        Integer vsCount = 0;
        // 都有经验和塔币的对战总次数（服务器可写死为5）；
        Integer vsECNum = 10;//@TODO暂时改为10
        // 只有经验的对战总次数（服务器可写死为10）
        Integer vsENum = 10;

        List<UserOneVsOneLog> userOneVsOneLogList = this.getGameLogByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),openId);
        // 1v1对战情况对战时间正序
        List<UserOneVsOneLog> userOneVsOneLogs = userOneVsOneLogList.stream().sorted(Comparator.comparing(UserOneVsOneLog::getCreateTime)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(userOneVsOneLogs)){
            for (UserOneVsOneLog userOneVsOneLog : userOneVsOneLogs) {
                vsCount ++;
                // 根据答题次数计算不同的积分塔币
                if (vsCount < 6) {
                    if (openId.equals(userOneVsOneLog.getRoomOpenId())) {
                        // 用户是房主
                        if (userOneVsOneLog.getIsWin() != 2)  {
                            nowExp += 2;
                            nowCoin += 2;
                        } else {
                            nowExp += 1;
                        }
                    } else if (openId.equals(userOneVsOneLog.getFriendOpenId())){
                        // 用户是受邀人员
                        if (userOneVsOneLog.getIsWin() != 1)  {
                            nowExp += 2;
                            nowCoin += 2;
                        } else {
                            nowExp += 1;
                        }
                    }
                } else if(vsCount < 11){
                    if (openId.equals(userOneVsOneLog.getRoomOpenId())) {
                        // 用户是房主
                        if (userOneVsOneLog.getIsWin() != 2)  {
                            nowExp += 2;
                        } else {
                            nowExp += 1;
                        }
                    } else if (openId.equals(userOneVsOneLog.getFriendOpenId())){
                        // 用户是受邀人员
                        if (userOneVsOneLog.getIsWin() != 1)  {
                            nowExp += 2;
                        } else {
                            nowExp += 1;
                        }
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
