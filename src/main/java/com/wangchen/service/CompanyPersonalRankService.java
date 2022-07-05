package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.CompanyPersonalRank;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 公司个人排行榜表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */

public interface CompanyPersonalRankService extends IService<CompanyPersonalRank> {

    /**
     * 公司个人排行榜 （二期 后台）
     */
    Result selectPagesPersonal(@Param("page") int page, @Param("limit") int limit, @Param("time") String time, @Param("companyId") Integer companyId, @Param("type") Integer type, @Param("companyType") Integer companyType);

    /**
     * 查询所属公司总的排行信息 （二期 小程序）
     */
    Map<String, Object> getCompanyPersonalRankList(@Param("openId") String openId, @Param("page") int page, @Param("limit") int limit, @Param("type") Integer type, @Param("cType") Integer cType);
}
