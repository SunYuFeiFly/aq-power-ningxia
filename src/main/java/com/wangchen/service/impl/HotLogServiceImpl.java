package com.wangchen.service.impl;

import com.wangchen.entity.HotLog;
import com.wangchen.mapper.HotLogMapper;
import com.wangchen.service.HotLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户完成公告表 比如:(某某完成了每日答题)、某某获得了什么成就等等 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class HotLogServiceImpl extends ServiceImpl<HotLogMapper, HotLog> implements HotLogService {

}
