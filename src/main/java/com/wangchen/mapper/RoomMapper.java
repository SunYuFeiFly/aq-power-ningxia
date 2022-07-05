package com.wangchen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yinguang
 * @since 2019-08-23
 */
@Component
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    @Select("select max(room_no) from aq_room")
    @ResultType(Long.class)
    Long selectMaxRoomNo();
}
