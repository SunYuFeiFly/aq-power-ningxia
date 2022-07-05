package com.wangchen.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.UserLevel;
import com.wangchen.mapper.UserLevelMapper;
import com.wangchen.service.UserLevelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户等级表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevel> implements UserLevelService {

    @Autowired
    private UserLevelMapper userLevelMapper;


    /**
     * 每年元旦（1月1日，凌晨两次）员工等级清零
     */
    @Override
    @Transactional
    public void taskResetLevel() {
        // 获取所有用户等级对象集合
        List<UserLevel> userLevels = userLevelMapper.selectList(new QueryWrapper<UserLevel>());
        if (null != userLevels && !userLevels.isEmpty()) {
            UserLevel userLevel01 = userLevels.get(userLevels.size() - 1);
            UserLevel userLevel02 = userLevels.get(userLevels.size() - 2);
            // 获取当日是月中第几天
            int day = DateUtil.date().dayOfMonth();
            if (DateUtil.date(userLevel01.getUpdateTime()).dayOfMonth() == day && DateUtil.date(userLevel02.getUpdateTime()).dayOfMonth() == day) {
                // 第一次已经更新成功了！
            } else {
                // 员工等级清零
                for (UserLevel userLevel : userLevels) {
                    userLevel.setLevelId(1);
                    userLevel.setLevelName("0级");
                    userLevel.setUpdateTime(new Date());
                }

                // 更新员工等级信息
                this.updateBatchById(userLevels);
            }
        } else {
            throw new BusinessException("没有用户等级需要清零！");
        }
    }



}
