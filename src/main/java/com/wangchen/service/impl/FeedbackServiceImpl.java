package com.wangchen.service.impl;

import com.wangchen.entity.Feedback;
import com.wangchen.mapper.FeedbackMapper;
import com.wangchen.service.FeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户反馈表信息 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-04
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

}
