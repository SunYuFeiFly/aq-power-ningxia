package com.wangchen.service;

import com.wangchen.entity.UserHonor;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户称号表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface UserHonorService extends IService<UserHonor> {

    /**
     * 获取员工最后段位
     */
    UserHonor getLastHonor(String openId);

    /**
     * 每年元旦（1月1日，凌晨、清晨两次）员工段位清零
     */
    void taskResetHonnor();
}
