package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.entity.UserHonor;
import com.wangchen.mapper.UserHonorMapper;
import com.wangchen.service.UserHonorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户称号表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class UserHonorServiceImpl extends ServiceImpl<UserHonorMapper, UserHonor> implements UserHonorService {

    @Autowired
    private UserHonorMapper userHonorMapper;

    @Override
    public UserHonor getLastHonor(String openId) {
        return userHonorMapper.getLastHonor(openId);
    }

    @Override
    @Transactional
    public void taskResetHonnor() {
        // 查询所有用户段位对象
        List<UserHonor> userHonors = userHonorMapper.selectList(new QueryWrapper<UserHonor>());
        ArrayList<Integer> ids = new ArrayList<>();
        if (null != userHonors && !userHonors.isEmpty()) {
            // 用户段位还没有完全清除
            for (UserHonor userHonor : userHonors) {
                ids.add(userHonor.getId());
            }
        }
        
        if (null != ids && !ids.isEmpty()) {
            // 批量删除用户段位对象数据
            userHonorMapper.deleteBatchIds(ids);
        }

    }
}
