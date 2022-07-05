package com.wangchen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.entity.Room;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yinguang
 * @since 2019-08-23
 */
public interface RoomService extends IService<Room> {

    void addRoom(@Param("room") Room room);

    Long selectMaxRoomNo();
}
