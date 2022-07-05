package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.entity.Notice;
import com.wangchen.mapper.NoticeMapper;
import com.wangchen.service.NoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 查询公告信息（二期）
     * @param companyType 所属公司分类
     * @return 所属公司分类下公告信息集合
     */
    @Override
    public List<Notice> getAlertTipsList(Integer companyType) {
        List<Notice> notices = noticeMapper.selectList(new QueryWrapper<Notice>().eq("company_type", companyType).eq("status", 0));
        return notices;
    }
}
