package com.wangchen.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wangchen.common.Result;
import com.wangchen.entity.AllProvinceRank;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.vo.AllProvinceRankHouTaiLookVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 全省排行榜信息 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
public interface AllProvinceRankService extends IService<AllProvinceRank> {

    /**
     * 全省个人排行列表页面数据 （二期、后台）
     */
    Result selectPagesAll(@Param("page") int page, @Param("limit") int limit, @Param("time") String time, @Param("type") Integer type, @Param("companyType") Integer companyType);

    /**
     * 查询全省个人排行榜信息 (二期、小程序 )
     */
    Map<String, Object> getAllProvinceRankList(@Param("openId") String openId, @Param("page") int page, @Param("limit") int limit, @Param("type") Integer type, @Param("cType") Integer cType);

}
