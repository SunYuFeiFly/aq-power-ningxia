package com.wangchen.service;

import com.wangchen.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 获取所属公司分类下公告信息集合
     */
    List<Notice> getAlertTipsList(@Param("companyType") Integer companyType);
}
