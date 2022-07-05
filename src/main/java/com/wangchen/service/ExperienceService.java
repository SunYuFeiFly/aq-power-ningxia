package com.wangchen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.entity.Experience;

/**
 * <p>
 * 用户经验明细表
 * </p>
 *
 * @author LiJian
 * @since 2021-09-09
 */

public interface ExperienceService extends IService<Experience> {

    /**
     * 每天定时更新用户积分
     */
    void updateEveryDayExperience();

    /**
     * 新年凌晨对人员前一年所拥有的年内经验清零
     */
    void taskResetPresentExperience();
}
