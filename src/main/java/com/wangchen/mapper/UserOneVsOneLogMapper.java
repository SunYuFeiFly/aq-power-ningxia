package com.wangchen.mapper;

import com.wangchen.entity.UserOneVsOneLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 1v1对战记录表 Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2020-07-16
 */
@Component
@Mapper
public interface UserOneVsOneLogMapper extends BaseMapper<UserOneVsOneLog> {

    @Select("SELECT * FROM aq_user_one_vs_one_log where create_date =#{dateStr}" +
            " and ((room_open_id = #{roomOpenId} and friend_open_id = #{friendOpenId} ) " +
            " or (room_open_id = #{friendOpenId} and friend_open_id = #{roomOpenId} )) ")
    List<UserOneVsOneLog> getFriendGameLog(@Param("dateStr")String dateStr, @Param("roomOpenId")String roomOpenId,@Param("friendOpenId") String friendOpenId);

    @Select("SELECT COUNT(1) FROM aq_user_one_vs_one_log where create_date =#{dateStr}" +
            " and (room_open_id = #{openId} or friend_open_id = #{openId} )")
    Integer getGameLogNumByOpenId(@Param("dateStr")String dateStr, @Param("openId")String openId);

    @Select("SELECT * FROM aq_user_one_vs_one_log where create_date =#{dateStr}" +
            " and (room_open_id = #{openId} or friend_open_id = #{openId} )")
    List<UserOneVsOneLog> getGameLogByOpenId(@Param("dateStr")String dateStr, @Param("openId")String openId);

    @Select("SELECT * from (" +
            " select room_open_id as open_id from aq_user_one_vs_one_log where  create_date =#{dateStr}\n" +
            " UNION ALL\n" +
            " select friend_open_id as open_id from aq_user_one_vs_one_log where  create_date =#{dateStr}" +
            " ) as a GROUP BY a.open_id\n" +
            " HAVING count(a.open_id) > 9")
    List<String> getGameGtTenNumUser(@Param("dateStr")String dateStr);
}
