package com.wangchen.mapper;

import com.wangchen.entity.CompanyPersonalRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.vo.AllProvinceRankVo;
import com.wangchen.vo.CompanyPersonalRankVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 公司个人排行榜表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Mapper
@Component
public interface CompanyPersonalRankMapper extends BaseMapper<CompanyPersonalRank> {

    /**
     * 根据分公司id去查询排行榜信息
     *
     * @return
     */
    @Select("select u.open_id as openId,u.`name` as name,u.company_name as companyName,u.avatar as avatar,ul.level_id-1 as levelNo,  " +
            " u.all_experience+ul.now_experience as allExp,u.all_achievement as allAch,  " +
            "(u.all_experience+ul.now_experience)+(u.all_achievement*10) as compositeScore  from aq_user u INNER JOIN " +
            " aq_user_level ul on u.open_id = ul.open_id where company_id = #{companyId} \n" +
            " ORDER BY (u.all_experience+ul.now_experience)+(u.all_achievement*10) desc ,ul.update_time asc ")
    List<CompanyPersonalRankVo> companyPersonalRank(@Param("companyId") Integer companyId);

    /**
     * 公司个人排行榜 (二期 历史总排行 后台 定时任务)
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id-1 as levelNo, u.company_name as companyName, (u.all_experience + u.all_achievement*10) as compositeScore," +
            " u.avatar as avatar, u.all_experience as allExp, u.all_achievement as allAch, u.type as type, u.company_id as companyId" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id AND u.deleted = 0" +
            " ORDER BY (u.all_experience + u.all_achievement*10) desc ,ul.update_time asc")
    List<CompanyPersonalRankVo> companyPersonalRankForAll();

    /**
     * 公司个人排行榜 (二期 年内总排行 后台 定时任务)
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id-1 as levelNo, u.company_name as companyName, (u.present_experience + u.all_achievement*10) as compositeScore," +
            " u.avatar as avatar, u.all_experience as allExp, u.all_achievement as allAch, u.type as type, u.company_id as companyId" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id AND u.deleted = 0" +
            " ORDER BY (u.present_experience + u.all_achievement*10) desc ,ul.update_time asc")
    List<CompanyPersonalRankVo> companyPersonalRankForYear();

    /**
     * 公司个人排行榜（二期 历史总排行 小程序）
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id-1 as levelNo, u.company_name as companyName, (u.all_experience + u.all_achievement*10) as compositeScore," +
            " u.avatar as avatar, u.all_experience as allExp, u.all_achievement as allAch, u.type as type, u.company_id as companyId" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id AND u.deleted = 0 AND u.company_id = #{companyId}" +
            " ORDER BY (u.all_experience + u.all_achievement*10) desc ,ul.update_time asc")
    List<CompanyPersonalRankVo> getCompanyPersonalRankListForAll(@Param("companyId") Integer companyId);

    /**
     * 公司个人排行榜（二期 年内总排行 小程序）
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id-1 as levelNo, u.company_name as companyName, (u.present_experience + u.all_achievement*10) as compositeScore," +
            " u.avatar as avatar, u.all_experience as allExp, u.all_achievement as allAch, u.type as type, u.company_id as companyId" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id AND u.deleted = 0 AND u.company_id = #{companyId}" +
            " ORDER BY (u.present_experience + u.all_achievement*10) desc ,ul.update_time asc")
    List<CompanyPersonalRankVo> getCompanyPersonalRankListForYear(@Param("companyId") Integer companyId);
}
