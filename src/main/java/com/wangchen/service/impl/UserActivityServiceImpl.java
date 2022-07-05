package com.wangchen.service.impl;

import com.wangchen.entity.UserActivity;
import com.wangchen.mapper.UserActivityMapper;
import com.wangchen.service.UserActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户活动赛 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
@Service
public class UserActivityServiceImpl extends ServiceImpl<UserActivityMapper, UserActivity> implements UserActivityService {

}
