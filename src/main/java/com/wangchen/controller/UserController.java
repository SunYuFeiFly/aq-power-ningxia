package com.wangchen.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.StringUtils;
import com.wangchen.vo.CompanyUserInfoDetailVo;
import com.wangchen.vo.ExportCompanyCsvVo;
import com.wangchen.vo.ExportUserGameCsvVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  游戏用户 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Slf4j
@Controller
@RequestMapping("/system/user")
public class UserController {

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Autowired
    private SevenSignService sevenSignService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private ThreeRoomService threeRoomService;

    @Autowired
    private UserAchievementService userAchievementService;


    /**
     * 公司员工列表页面（二期）
     * @param model 数据模型
     * @param companyType 所属公司类型
     * @return 公司员工数据集合
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        // 查询该公司分类下公司集合
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type", companyType));
        model.addAttribute("companyList",companyList);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "user/aList";
        } else if (2 == companyType) {
            return "user/bList";
        } else if (3 == companyType) {
            return "user/cList";
        }

        return null;
    }


    /**
     * 员工管理 - 公司员工列表数据（二期）
     * @param name 公司名称
     * @param time 查询时间
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 查询所属分类公司
     * @return 公司活跃数据集合
     */
    @RequiresPermissions("system:user:view")
    @PostMapping("/selectPages")
    @ResponseBody
    public Result findExchangeList(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                            @RequestParam(value = "time", required = false, defaultValue = "") String time,
                            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                            @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        try {
            if(StringUtils.isEmpty(time)){
                time = Constants.SDF_YYYY_MM_DD.format(new Date());
            }
            Result result = userService.selectPages(name, time, page, limit, companyType);
            return result;
        } catch (Exception e) {
            log.error("获取公司员工列表数据出错，错误信息: {}",e);
            return Result.newFaild("获公司员工列表数据为空");
        }
    }


    /**
     * 查看公司员工详情（二期）
     * @param model 模型数据
     * @param id 用户id
     * @param companyType 所属公司类型
     * @return
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping("/edit")
    public String editExchange(Model model,
                               @RequestParam(value = "id", required = false, defaultValue = "") Long id,
                               @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        model.addAttribute("id",id);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "user/aEdit";
        } else if (2 == companyType) {
            return "user/bEdit";
        } else if (3 == companyType) {
            return "user/cEdit";
        }
        return null;
    }


    /**
     * 详情页面数据 （二期，后台管理）
     * @param id 公司id
     * @param name 员工姓名
     * @param time 时间 （2021-10-12）
     * @param page 页码
     * @param limit 每页数据量
     * @return 特定时间公司员工数据集合
     */
    @RequiresPermissions("system:user:view")
    @PostMapping("selectUserList")
    public @ResponseBody
    Result selectExchangeByGoodsId(@RequestParam(value = "id", required = false, defaultValue = "") Long id,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                   @RequestParam(value = "name", required = false, defaultValue = "") String name,
                                   @RequestParam(value = "time", required = false, defaultValue = "") String time) {
        try {
            // 今天的开始时间（如：2021-10-12 00:00:00）
            Date todayBegin = DateUtil.beginOfDay(DateUtil.date());
            if (StringUtils.isEmpty(time)) {
                // 时间为空，默认查询当天的数据
                time = DateUtil.format(todayBegin, "yyyy-MM-dd");
            } else {
                // 判断开始时间是否在10月8日之后
                Date date = DateUtil.parse("2021-10-08 00:00:00");
                if (DateUtil.parse(time).getTime() - date.getTime() > 0) {
                    // 判断开始时间是否大于今天
                    if (DateUtil.parse(time).getTime() - todayBegin.getTime() > 0) {
                        time = DateUtil.format(todayBegin, "yyyy-MM-dd");
                    }
                } else {
                    // 小于10月8日按10月8日开始
                    time ="2021-10-08";
                }
            }

            Result result = userService.selectUserList(id, page, limit, name, time);
            return result;
        } catch (Exception e) {
            log.error("查询员工详情页面数据：{}", e);
        }

        return null;
    }


    /**
     * 查看用户拥有成就信息
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/7/13
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping("/editAchievement")
    public String editAchievement(Model model,
                               @RequestParam(value = "openId", required = false, defaultValue = "") String openId) {
        model.addAttribute("openId",openId);
        return "user/hasachievement";
    }


    /**
     * 详情页面数据
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:19 2020/7/13
     */
    @RequiresPermissions("system:user:view")
    @PostMapping("selectUserHasAchievementList")
    public @ResponseBody
    Result selectUserHasAchievementList(Model model,
                                   @RequestParam(value = "openId", required = false, defaultValue = "") String openId,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        IPage<UserAchievement> userAchievementIPage = userAchievementService.page(new Page<>(page,limit),new QueryWrapper<UserAchievement>()
                .eq("open_id",openId));
        return ResultLayuiTable.newSuccess(userAchievementIPage.getTotal(), userAchievementIPage.getRecords());
    }


    /**
     * 更新用户截至年底所获得的总经验
     */
    @RequiresPermissions("system:user:view")
    @PostMapping("/updateLastYearExperience")
    @ResponseBody
    public Result updateLastYearExperience() {
        // 获取所有游戏用户集合
        List<User> userList = userService.list(new QueryWrapper<User>());
        if (null != userList && !userList.isEmpty()) {
            // 更新游戏用户截至年底所拥有总积分
            try {
                log.info("执行了游戏用户年度积分更新 定时任务");
                return userService.updateLastYearExperience(userList);
            } catch (Exception e) {
                log.info("游戏用户年度积分更新出错！");
                e.printStackTrace();
            }
        }
        return Result.newFaild("没有游戏用户年度积分更新！");
    }


}

