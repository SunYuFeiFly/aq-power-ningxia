package com.wangchen.service.impl;

import com.wangchen.entity.ThreeRoom;
import com.wangchen.mapper.ThreeRoomMapper;
import com.wangchen.service.ThreeRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-10
 */
@Service
public class ThreeRoomServiceImpl extends ServiceImpl<ThreeRoomMapper, ThreeRoom> implements ThreeRoomService {

    @Autowired
    private ThreeRoomMapper threeRoomMapper;

    @Override
    public Long selectMaxRoomNo() {
        return threeRoomMapper.selectMaxRoomNo();
    }

}
