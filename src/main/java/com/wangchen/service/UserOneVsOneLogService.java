package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.UserOneVsOneLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 1v1对战记录表 服务类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-16
 */
public interface UserOneVsOneLogService extends IService<UserOneVsOneLog> {

    boolean getFriendGameLog(@Param("dateStr") String dateStr, @Param("roomOpenId") String roomOpenId, @Param("friendOpenId") String friendOpenId);

    Integer getGameLogNumByOpenId(@Param("dateStr") String dateStr, @Param("openId") String openId);

    List<UserOneVsOneLog> getGameLogByOpenId(@Param("dateStr") String dateStr, @Param("openId") String openId);

    List<String> getGameGtTenNumUser(@Param("dateStr") String dateStr);

    /**
     * 用户当前完成1v1情况
     */
    Result userIsOneVsOne(@Param("openId") String openId);
}
