package com.wangchen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.common.Result;
import com.wangchen.entity.UserTeamVsTeamLog;
import org.apache.ibatis.annotations.Param;

public interface UserTeamVsTeamLogService extends IService<UserTeamVsTeamLog> {

    /**
     * 用户当前完成团队赛情况 (二期)
     */
    Result userIsTeamVsTeam(@Param("openId") String openId);

}
