package com.wangchen.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.entity.Room;
import com.wangchen.mapper.RoomMapper;
import com.wangchen.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2019-08-23
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public void addRoom(Room room) {
        roomMapper.insert(room);
    }

    @Override
    public Long selectMaxRoomNo() {
        return roomMapper.selectMaxRoomNo();
    }
}
