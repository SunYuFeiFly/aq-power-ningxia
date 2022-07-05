package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.UserBranch;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户部门表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface UserBranchService extends IService<UserBranch> {

    /**
     * 修改游戏用户信息(二期)
     */
    Result editGameUser(@Param("openId") String openId, @Param("branch1") Integer branch1, @Param("branch2") Integer branch2, @Param("branch3") Integer branch3, @Param("type") Integer type);
}
