package com.wangchen.mapper;

import com.wangchen.entity.CompanyRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.vo.AllProvinceRankVo;
import com.wangchen.vo.CompanyRankVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 所有公司的排行榜 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Component
@Mapper
public interface CompanyRankMapper extends BaseMapper<CompanyRank> {
    /**
     * 获取到排序后的排行榜信息
     * @return
     */
    @Select("select u.company_id as companyId,u.company_name as companyName,sum(1) as count," +
            " sum((u.all_experience+ul.now_experience) + (u.all_achievement*10))/sum(1) as compositeScore  " +
            " from aq_user u INNER JOIN \n" +
            " aq_user_level ul on u.open_id = ul.open_id " +
            " GROUP BY u.company_id ORDER BY sum((u.all_experience+ul.now_experience) + (u.all_achievement*10))/sum(1) desc ,ul.update_time asc ")
    List<CompanyRankVo> companyRankList();

    /**
     * 所有公司的排行榜(历史总排行 后台 定时任务)
     */
    @Select("SELECT c.id AS companyId, c.name AS companyName, compositeScore,c.type AS type FROM (SELECT u.company_id AS companyId, sum(u.all_experience + u.all_achievement*10) AS compositeScore"+
            " FROM aq_user u"+
            " GROUP BY u.company_id) AS companyAllexp"+
            " RIGHT JOIN aq_company AS c ON companyAllexp.companyId = c.id")
    List<CompanyRankVo> companyRankListForAll();

    /**
     * 所有公司的排行榜(年内总排行 后台 定时任务)
     */
    @Select("SELECT c.id AS companyId, c.name AS companyName, compositeScore,c.type AS type FROM (SELECT u.company_id AS companyId, sum(u.present_experience + u.all_achievement*10) AS compositeScore"+
            " FROM aq_user u"+
            " GROUP BY u.company_id) AS companyAllexp"+
            " RIGHT JOIN aq_company AS c ON companyAllexp.companyId = c.id")
    List<CompanyRankVo> companyRankListForYear();

    /**
     * 所有公司的排行榜(历史总排行 小程序)
     */
    @Select("SELECT c.id AS companyId, c.name AS companyName, compositeScore,c.type AS type FROM (SELECT u.company_id AS companyId, sum(u.all_experience + u.all_achievement*10) AS compositeScore"+
            " FROM aq_user u WHERE u.type =#{cType}"+
            " GROUP BY u.company_id) AS companyAllexp"+
            " RIGHT JOIN aq_company AS c ON companyAllexp.companyId = c.id WHERE c.type =#{cType}")
    ArrayList<CompanyRankVo> getCompanyRankListForAll(@Param("cType") Integer cType);

    /**
     * 所有公司的排行榜(年内总排行 小程序)
     */
    @Select("SELECT c.id AS companyId, c.name AS companyName, compositeScore,c.type AS type FROM (SELECT u.company_id AS companyId, sum(u.present_experience + u.all_achievement*10) AS compositeScore"+
            " FROM aq_user u WHERE u.type =#{cType}"+
            " GROUP BY u.company_id) AS companyAllexp"+
            " RIGHT JOIN aq_company AS c ON companyAllexp.companyId = c.id WHERE c.type =#{cType}")
    ArrayList<CompanyRankVo> getCompanyRankListForYear(@Param("cType") Integer cType);
}
