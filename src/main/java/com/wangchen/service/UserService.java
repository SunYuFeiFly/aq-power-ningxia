package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface UserService extends IService<User> {

    User getUserByOpenId(@Param("openId") String openId);

    /**
     * 更新游戏用户截至年底所拥有总积分
     */
    Result updateLastYearExperience(@Param("userList") List<User> userList);

    /**
     * 员工管理 - 公司员工列表数据（二期 后天管理  ** 返回的是公司活跃度集合）
     */
    Result selectPages(@Param("name") String name, @Param("time") String time, @Param("page") Integer page, @Param("limit") Integer limit, @Param("companyType") Integer companyType);

    /**
     * 详情页面数据 （二期，小程序）
     */
    Result selectUserList(@Param("id") Long id, @Param("page") int page, @Param("limit") int limit, @Param("name") String name, @Param("time") String time);

    /**
     * 员工管理 - 游戏用户集合
     */
    Result selectPages01(@Param("name") String name, @Param("companyName") String companyName, @Param("page") Integer page, @Param("limit") Integer limit, @Param("companyType") Integer companyType);

    /**
     * 定时清除体验账户
     */
    void deleteExperienceUser();
}
