package com.wangchen.service;

import com.wangchen.entity.ThreeRoom;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yinguang
 * @since 2020-07-10
 */
public interface ThreeRoomService extends IService<ThreeRoom> {

    Long selectMaxRoomNo();
}
