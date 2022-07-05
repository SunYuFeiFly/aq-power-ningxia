package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.Result;
import com.wangchen.entity.UserThreeTeamLog;
import com.wangchen.mapper.UserThreeTeamLogMapper;
import com.wangchen.service.UserThreeTeamLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-13
 */
@Service
public class UserThreeTeamLogServiceImpl extends ServiceImpl<UserThreeTeamLogMapper, UserThreeTeamLog> implements UserThreeTeamLogService {

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    /**
     * 查询用户当前今天完成3人团战情况
     * @param openId 用户id
     * @return 用户当前完成3人团战情况
     *
     */
    @Override
    @Transactional
    public Result userIsThreeTeam(String openId) {
            List<UserThreeTeamLog> userThreeTeamLogList = userThreeTeamLogService.list(new QueryWrapper<UserThreeTeamLog>()
                    .eq("open_id",openId)
                    .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));

            if(CollectionUtils.isNotEmpty(userThreeTeamLogList)){
                return Result.newSuccess(1);
            }
            return Result.newSuccess(0);

    }
}
