package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 游戏用户信息
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Slf4j
@Controller
@RequestMapping("/system/gameuser")
public class GameUserController {


    @Autowired
    private UserService userService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private CompanyService companyService;


    /**
     * 游戏用户信息页面（二期）
     * @param model 数据模板
     * @param companyType 所属公司分类
     * @return 游戏用户数据集合
     */
    @RequiresPermissions("system:gameuser:view")
    @RequestMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        // 获取所属公司分类下的公司集合
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type", companyType));
        ArrayList<String> list = new ArrayList<>();
        model.addAttribute("companyList",companyList);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "gameuser/aList";
        } else if (2 == companyType) {
            return "gameuser/bList";
        } else if (3 == companyType) {
            return "gameuser/cList";
        }

        return null;
    }


    /**
     * 列表数据（二期）
     * @param name 用户名称，用于用于模糊搜索
     * @param companyName 公司id
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 所属公司类型
     * @return 员工管理-游戏用户集合
     */
    @RequiresPermissions("system:gameuser:view")
    @PostMapping("/selectPages")
    public @ResponseBody
    Result findExchangeList(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                            @RequestParam(value = "companyName", required = false, defaultValue = "") String companyName,
                            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                            @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        try {
            Result result = userService.selectPages01(name, companyName, page, limit, companyType);
            return result;
        }catch (Exception e){
            log.error("获取员工列表信息出错，错误信息: {}",e);
            return Result.newFaild("获取员工列表信息出错");
        }
    }


    /**
     * 查看详情
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/7/13
     */
    @RequiresPermissions("system:gameuser:view")
    @GetMapping("/edit")
    public String editExchange(Model model,
                               @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        User gameUser = userService.getById(id);
        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",gameUser.getOpenId()));
        List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",gameUser.getOpenId()));
        for(int i =1;i<=userBranchList.size(); i++){
            model.addAttribute("branch"+ i,userBranchList.get(i-1));
        }
        List<Branch> branchList = branchService.list(new QueryWrapper<Branch>());
        model.addAttribute("branchList",branchList);
        model.addAttribute("gameUser",gameUser);
        model.addAttribute("isEdit",true);
        if (1 == gameUser.getType()) {
            return "gameuser/aEdit";
        } else if (2 == gameUser.getType()) {
            return "gameuser/bEdit";
        } else if (3 == gameUser.getType()) {
            return "gameuser/cEdit";
        }

        return null;
    }


    /**
     * 修改游戏用户信息 （二期）
     * @param id 修改游戏用户资料室用户在列表id
     * @param openId 用户id
     * @param branch1 所属部门一
     * @param branch2 所属部门二
     * @param branch3 所属部门三
     * @return 修改成功与否
     */
    @RequiresPermissions("system:gameuser:view")
    @RequestMapping("/editGameUser")
    @ResponseBody
    public Result editGameUser(
            @RequestParam(value = "id", required = false, defaultValue = "") Long id,
            @RequestParam(value = "openId", required = false, defaultValue = "") String openId,
            @RequestParam(value = "branch1", required = false, defaultValue = "") Integer branch1,
            @RequestParam(value = "branch2", required = false, defaultValue = "") Integer branch2,
            @RequestParam(value = "branch3", required = false, defaultValue = "") Integer branch3) {
        try {
            User user = userService.getUserByOpenId(openId);
            if (user == null) {
                throw new BusinessException("用户openId不存在!");
            }
            if(null == id || 0 == id || org.apache.commons.lang.StringUtils.isBlank(openId)){
                return Result.newFail(BusinessErrorMsg.PARAM_IS_NULL);
            }

            // 修改游戏用户信息
            Result result = userBranchService.editGameUser(openId, branch1, branch2, branch3, user.getType());
            return result;
        } catch (Exception e) {
            log.error("修改游戏用户信息出错，错误信息: {}",e);
            return Result.newFaild("修改游戏用户信息出错");
        }
    }

}

