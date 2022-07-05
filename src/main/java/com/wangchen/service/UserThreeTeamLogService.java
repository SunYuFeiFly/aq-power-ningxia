package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.UserThreeTeamLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-13
 */
public interface UserThreeTeamLogService extends IService<UserThreeTeamLog> {

    // 用户当前完成3人对战情况
    Result userIsThreeTeam(@Param("openId") String openId);
}
