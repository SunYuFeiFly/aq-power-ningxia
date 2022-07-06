package com.wangchen.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.Experience;
import com.wangchen.entity.User;
import com.wangchen.mapper.ExperienceMapper;
import com.wangchen.service.ExperienceService;
import com.wangchen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户经验明细表
 * </p>
 *
 * @author LiJian
 * @since 2021-09-09
 */

@Slf4j
@Service
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, Experience> implements ExperienceService {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperienceMapper experienceMapper;

    /**
     * 每天定时更新用户积分
     */
    @Override
    @Transactional(rollbackFor = {})
    public void updateEveryDayExperience() {
        // 获取当前时间在当月中天数
        int day = DateUtil.date().dayOfMonth();
        // 获取当前时间所属月份、年份
        Date date = DateUtil.date();
        // 获得月份，从0开始计数
        int month = DateUtil.month(date) + 1;
        // 获得年的部分
        int year = DateUtil.year(date);
        ArrayList<Experience> experiencesList = new ArrayList<>();
        // 查询所有用户集合
        List<User> users = userService.list(new QueryWrapper<User>().eq("deleted", 0).orderByDesc("id"));
        Map<String, Integer> collect = users.stream().collect(Collectors.toMap(User::getOpenId, User::getAllExperience));
        if (CollUtil.isNotEmpty(users)) {
            // 获取所有用户经验集合
            List<Experience> list = new ArrayList<>();
            if (1 == day) {
                if (1 == month) {
                    list = this.list(new QueryWrapper<Experience>().eq("part_month",12).eq("part_year",year-1).orderByDesc("id"));
                } else {
                    list = this.list(new QueryWrapper<Experience>().eq("part_month",month-1).eq("part_year",year).orderByDesc("id"));
                }
                // 1号需创建本月用户经验记录
                batchInsertExperience(users, month, year);
            } else {
                list = this.list(new QueryWrapper<Experience>().eq("part_month",month).eq("part_year",year).orderByDesc("id"));
            }
            if (CollUtil.isNotEmpty(list)) {
                Date updateTime = list.get(0).getUpdateTime();
                if (DateUtil.date(updateTime).dayOfMonth() == day) {
                    // 第一次已经更新成功
                } else {
                    for (Experience experience : list) {
                        if (null != experience) {
                            experience.setUpdateTime(new Date());
                            if (1 == day) {
                                experience.setMonthExperience(collect.get(experience.getOpenId()));
                            }
                            if (2 == day) {
                                // 不做处理
                            } else {
                                experience.setDayExperience(experience.getDayExperience() + "," + collect.get(experience.getOpenId()));
                            }
                            experiencesList.add(experience);
                        }
                    }
                }
            }
        }

        // 批量更新用户经验（前一天积分、月底积分）
        if (CollUtil.isNotEmpty(experiencesList)) {
            this.updateBatchById(experiencesList);
        }
    }

    /**
     * 月初创建用户对应的经验对象
     */
    private void batchInsertExperience(List<User> users, int month, int year) {
        ArrayList<Experience> experiences = new ArrayList<Experience>();
        if (null != users && !users.isEmpty()) {
            for (User user : users) {
                Experience experience = new Experience();
                experience.setOpenId(user.getOpenId());
                experience.setDayExperience(user.getAllExperience()+"");
                experience.setMonthExperience(null);
                experience.setCreateTime(new Date());
                experience.setUpdateTime(new Date());
                experience.setPartMonth(month);
                experience.setPartYear(year);
                experiences.add(experience);
            }
        } else {
            throw new BusinessException("游戏用户不能为空！");
        }

        // 批量创建经验对象
        this.saveBatch(experiences);
    }


    /**
     * 更新用户积分（前一天积分、月底积分）
     */
    private Experience updateExperience(Experience experience, User user, int day) {
        // 获取前一日用户总经验
        Integer allExperience = user.getAllExperience();
        // 获取用户当月每天的经验记录
        String dayExperience = experience.getDayExperience();
        // 上一天是月末，需设置积分记录积月底积分
        if (1 == day) {
            experience.setMonthExperience(allExperience);
        }
        // 将上一天用户总积分累加至积分记录最后
        if (day == 2) {
            dayExperience = dayExperience + "" + allExperience;
        } else {
            dayExperience = dayExperience + "," + allExperience;
        }
        experience.setDayExperience(dayExperience);

        return experience;
    }


    /**
     * 新年凌晨对人员前一年所拥有的年内经验清零
     */
    @Override
    @Transactional(rollbackFor = {})
    public void taskResetPresentExperience() {
        // 查询所有游戏用户集合
        List<User> userList = userService.list(new QueryWrapper<User>().eq("deleted", 0).orderByDesc("id"));
        // 判断第一次清零是否完全成功
        User user01 = userList.get(userList.size() - 1);
        User user02 = userList.get(userList.size() - 2);
        // 获取最后两名用户更新时间，用来确认第一次更新是否成功
        Date updateDate01 = user01.getUpdateDate();
        Date updateDate02 = user02.getUpdateDate();
        // 获取当前时间在当月中天数
        int day = DateUtil.date().dayOfMonth();
        if (DateUtil.date(updateDate01).dayOfMonth() == day && DateUtil.date(updateDate02).dayOfMonth() == day) {
            // 第一次已经更新成功

        } else {
            // 更新操作
            // 遍历将当前年内总积分置零、设置当前历史总积分为上一年底总积分
            List<User> users = userService.list(new QueryWrapper<User>().eq("deleted", 0));
            for (User user : users) {
                user.setUpdateDate(new Date());
                user.setLastYearExperience(user.getAllExperience());
                user.setPresentExperience(0);
            }

            // 批量更新操作
            userService.updateBatchById(users);
        }
    }

}
