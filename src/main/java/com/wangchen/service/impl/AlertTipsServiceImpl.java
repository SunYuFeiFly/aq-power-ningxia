package com.wangchen.service.impl;

import com.wangchen.entity.AlertTips;
import com.wangchen.mapper.AlertTipsMapper;
import com.wangchen.service.AlertTipsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户弹框提示(称号、成就获得提示  团队赛和活动赛获得提示) 服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@Service
public class AlertTipsServiceImpl extends ServiceImpl<AlertTipsMapper, AlertTips> implements AlertTipsService {

}
