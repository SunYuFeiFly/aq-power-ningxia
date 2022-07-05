package com.wangchen.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangchen.common.Result;
import com.wangchen.entity.CompanyRank;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.text.ParseException;
import java.util.Map;

/**
 * <p>
 * 所有公司的排行榜 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
public interface CompanyRankService extends IService<CompanyRank> {

    /**
     * 公司排行榜 (二期 后台)
     */
    Result selectPages(@Param("page") int page, @Param("limit") int limit, @Param("time") String time, @Param("companyType") Integer companyType, @Param("type") Integer type);

    /**
     * 公司排行榜(二期 小程序)
     */
    Map<String, Object> getCompanyRankList(@Param("openId") String openId, @Param("page") int page, @Param("limit") int limit, @Param("type") Integer type, @Param("cType") Integer cType) throws ParseException;
}
