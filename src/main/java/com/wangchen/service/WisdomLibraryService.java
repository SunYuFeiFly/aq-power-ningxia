package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.WisdomLibrary;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 铁塔智库表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-18
 */
public interface WisdomLibraryService extends IService<WisdomLibrary> {

    /**
     * 获取审核题目列表页面(二期)
     */
    Result selectPages(@Param("page") int page, @Param("limit") int limit, @Param("status") String status, @Param("type") Integer type, @Param("topicType") String topicType, @Param("companyType") String companyType);

    /**
     * 通过、驳回题目（二期）
     */
    Result passStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("tikuType") Integer tikuType, @Param("companyType") Integer companyType);

    /**
     * 使题目变为废弃状态(二期)
     */
    Result rejectStatus(@Param("id") Integer id, @Param("status") Integer status);
}
