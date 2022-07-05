package com.wangchen.service.impl;

import com.wangchen.entity.Sign;
import com.wangchen.mapper.SignMapper;
import com.wangchen.service.SignService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户连续签到表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class SignServiceImpl extends ServiceImpl<SignMapper, Sign> implements SignService {

}
