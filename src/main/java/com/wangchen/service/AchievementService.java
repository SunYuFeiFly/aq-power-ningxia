package com.wangchen.service;

import com.wangchen.entity.Achievement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.vo.AchievementVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-04
 */
public interface AchievementService extends IService<Achievement> {

    /**
     * 获取用户成就信息（二期）
     */
    List<AchievementVo> achievementService(@Param("openId") String openId);
}
