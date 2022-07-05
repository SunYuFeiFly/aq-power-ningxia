package com.wangchen.service;

import com.wangchen.entity.UserLevel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户等级表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface UserLevelService extends IService<UserLevel> {

    /**
     * 每年元旦（1月1日，凌晨、清晨两次）员工等级清零
     */
    void taskResetLevel();
}
