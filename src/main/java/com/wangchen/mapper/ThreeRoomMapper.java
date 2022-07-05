package com.wangchen.mapper;

import com.wangchen.entity.ThreeRoom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
 * @since 2020-07-10
 */
@Mapper
@Component
public interface ThreeRoomMapper extends BaseMapper<ThreeRoom> {

    @Select("select max(room_no) from aq_three_room")
    @ResultType(Long.class)
    Long selectMaxRoomNo();

}
