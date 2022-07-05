package com.wangchen.mapper;

import com.wangchen.entity.AllProvinceRank;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.vo.AllProvinceRankQianDuanLookVo;
import com.wangchen.vo.AllProvinceRankVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 全省排行榜信息 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Component
@Mapper
public interface AllProvinceRankMapper extends BaseMapper<AllProvinceRank> {

    /**
     * 获取到排序后的排行榜信息
     *
     * @return
     */
    @Select("select u.open_id as openId,u.`name` as name,u.company_name as companyName,u.avatar as avatar,ul.level_id-1 as levelNo," +
            " u.all_experience+ul.now_experience as allExp ,u.all_achievement as allAch" +
            " ,(u.all_experience+ul.now_experience)+(u.all_achievement*10) as compositeScore  from aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id\n" +
            " ORDER BY (u.all_experience+ul.now_experience)+(u.all_achievement*10) desc , ul.update_time asc")
    List<AllProvinceRankVo> allProvinceRank();

    /**
     * 全省个人排行榜信息（二期 历史总排行 后台 定时任务）
     */
    @Select("select u.open_id as openId, u.`name` as name, ul.level_id -1 as levelNo, u.company_name as companyName," +
            " u.all_experience as allExp ,u.all_achievement as allAch, (u.all_experience + u.all_achievement*10) as compositeScore, u.avatar as avatar, u.type as type" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id And u.deleted = 0" +
            " ORDER BY (u.all_experience + u.all_achievement*10) desc , ul.update_time asc")
    List<AllProvinceRankVo> allProvinceRankForAll();

    /**
     * 全省个人排行榜信息（二期 年内总排行 后台 定时任务）
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id -1 as levelNo, u.company_name as companyName," +
            " u.all_experience as allExp ,u.all_achievement as allAch, (u.present_experience + u.all_achievement*10) as compositeScore, u.avatar as avatar, u.type as type" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id And u.deleted = 0" +
            " ORDER BY (u.present_experience + u.all_achievement*10) desc , ul.update_time asc")
    List<AllProvinceRankVo> allProvinceRankForYear();

    /**
     * 查询全省个人排行榜信息(二期 历史总排行 小程序)
     */
    @Select("select u.open_id as openId, u.`name` as name, ul.level_id -1 as levelNo, u.company_name as companyName," +
            " u.all_experience as allExp ,u.all_achievement as allAch, (u.all_experience + u.all_achievement*10), u.avatar as avatar, u.type as type" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id And u.deleted = 0 AND u.type = #{companyType}" +
            " ORDER BY (u.all_experience + u.all_achievement*10) desc , ul.update_time asc")
    List<AllProvinceRankVo> getAllProvinceRankListForAll(@Param("companyType") Integer companyType);

    /**
     * 查询全省个人排行榜信息(二期 历史总排行 小程序)
     */
    @Select("select u.open_id as openId, u.name as name, ul.level_id -1 as levelNo, u.company_name as companyName," +
            " u.all_experience as allExp ,u.all_achievement as allAch, (u.present_experience + u.all_achievement*10) as compositeScore, u.avatar as avatar, u.type as type" +
            " FROM aq_user u INNER JOIN aq_user_level ul on u.open_id = ul.open_id And u.deleted = 0 AND u.type = #{companyType}" +
            " ORDER BY (u.present_experience + u.all_achievement*10) desc , ul.update_time asc")
    List<AllProvinceRankVo> getAllProvinceRankListForYear(@Param("companyType") Integer companyType);
}
