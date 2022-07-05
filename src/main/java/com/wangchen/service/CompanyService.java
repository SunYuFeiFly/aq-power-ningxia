package com.wangchen.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangchen.entity.Company;
import com.baomidou.mybatisplus.extension.service.IService;
import io.lettuce.core.dynamic.annotation.Param;

/**
 * <p>
 * 公司表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */

public interface CompanyService extends IService<Company> {

    /**
     * 查询A类公司对应B/C类选定公司id
     */
    Integer getAttachedCompanyByType(@Param("openId") String openId, @Param("cType") Integer cType);
}
