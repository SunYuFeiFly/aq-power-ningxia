package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.CompanyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * <p>
 * 基本用户表 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Slf4j
@Controller
@RequestMapping("/system/baseuser")
public class BaseUserController {

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserRoleService userRoleService;

    @Autowired
    private SysRoleService roleService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserGoodsService userGoodsService;

    @Autowired
    private UserGoodsAddressService userGoodsAddressService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private SignService signService;

    @Autowired
    private SevenSignService sevenSignService;

    @Autowired
    private ManguanService manguanService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;

    @Autowired
    private ExperienceService experienceService;

    /**
     * 页面
     */
    @RequiresPermissions("system:base:view")
    @RequestMapping("/list01")
    public String list01(Model model) {
        List<Company> companyList = companyService.list(new QueryWrapper<>());
        model.addAttribute("companyList",companyList);
        return "baseuser/list";
    }


    /**
     * 基础员工信息页面(二期)
     * @param model 数据模型
     * @param companyType 所属公司类型
     * @return 跳转路径
     */
    @RequiresPermissions("system:base:view")
    @RequestMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type",companyType));
        model.addAttribute("companyList",companyList);
        if (1 == companyType) {
            return "baseuser/aList";
        } else if (2 == companyType) {
            return "baseuser/bList";
        } else if (3 == companyType) {
            return "baseuser/cList";
        }

        return null;
    }


    /**
     * 员工管理-基本员工信息列表数据（二期）
     * @param name 用户名称（用于迷糊搜索）
     * @param companyName 公司名称
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 所属公司类型
     * @return 基本员工信息列表数据
     */
    @RequiresPermissions("system:base:view")
    @PostMapping("/selectPages")
    @ResponseBody
    public Result findExchangeList(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                                   @RequestParam(value = "companyName", required = false, defaultValue = "") String companyName,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
                                   @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        try {
            Result result = baseUserService.selectPages(name, companyName, page, limit, companyType);
            return result;
        }catch (Exception e){
            log.error("查询员工管理-基本员工信息列表数据出错，错误信息: {}",e);
            return Result.newFaild("查询员工管理-基本员工信息列表数据出错");
        }
    }


    /**
     * 新增或者编辑 基础员工数据（二期）
     * @param model
     * @param id 基本员工id
     * @param companyType 所属公司类型
     * @return 公司集合及部门集合
     */
    @RequiresPermissions("system:base:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id,
                       @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        BaseUser baseUser = baseUserService.getById(id);
        List<Branch> branchList = branchService.list(new QueryWrapper<Branch>().eq("company_type",companyType));
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type",companyType));
        model.addAttribute("companyType",companyType);
        model.addAttribute("branchList",branchList);
        model.addAttribute("companyList",companyList);
        model.addAttribute("isEdit", id != null);
        model.addAttribute("baseUser", baseUser);
        if (1 == companyType) {
            return "baseuser/aEdit";
        } else if (2 == companyType) {
            return "baseuser/bEdit";
        } else if (3 == companyType) {
            return "baseuser/cEdit";
        }

        return null;
    }


    /**
     * 新增修改基本员工数据 (二期)
     * @param info 基本用户信息
     * @return 添加或修改结果
     */
    @RequiresPermissions("system:base:view")
    @RequestMapping("/editBaseUser")
    @ResponseBody
    public Result editBaseUser(BaseUser info) {
        try {
            if(null == info){
                return Result.newFail("未获取到参数信息");
            }
            if(StringUtils.isEmpty(info.getIdCard()) || StringUtils.isEmpty(info.getPhone())
                    || null == info.getSex() || StringUtils.isEmpty(info.getName()) || StringUtils.isEmpty(info.getCompany())){
                return Result.newFail("必填参数不能为空");
            }
            // A类员工前端可以选择部门，需对部门数据进行处理
            if (1 == info.getType()) {
                // 部门去重
                LinkedHashSet<String> set = new LinkedHashSet<>();
                if (!StringUtils.isEmpty(info.getBranchOne())) {
                    set.add(info.getBranchOne());
                }
                if (!StringUtils.isEmpty(info.getBranchTwo())) {
                    set.add(info.getBranchTwo());
                }
                if (!StringUtils.isEmpty(info.getBranchThree())) {
                    set.add(info.getBranchThree());
                }
                if (set.size() < 1) {
                    return Result.newFail("所属部门不能为空！");
                }
                // 重新赋值
                Iterator<String> iterator = set.iterator();
                int index = 1;
                while (iterator.hasNext()) {
                    if (1 == index) {
                        info.setBranchOne(iterator.next());
                        info.setBranchTwo(null);
                        info.setBranchThree(null);
                    }
                    if (2 == index) {
                        info.setBranchTwo(iterator.next());
                    }
                    if (3 == index) {
                        info.setBranchThree(iterator.next());
                    }
                    index++;
                }
            } else if (2 == info.getType() || 3 == info.getType()) {
                // 根据公司名称获取对应部门名称
                String[] branchNames = Constants.companyBuMenKu.get(info.getCompany());
                for (int i = 0; i < branchNames.length; i++) {
                    if (0 == i) {
                        info.setBranchOne(branchNames[i]);
                    }
                    if (0 == i) {
                        info.setBranchTwo(branchNames[i]);
                    }
                }
            } else {
                return Result.newFail("员工所属公司分类只能为1/2/3");
            }

            // 新增修改员工数据
            Result result = baseUserService.editBaseUser(info);
            return result;
        } catch (Exception e) {
            log.error("新增修改基本员工数据出错，错误信息: {}",e);
            return Result.newFail(null == info.getId() ? "新增基本员工数据出错" : "修改基本员工数据出错");
        }
    }


    /**
     * 删除基本用户信息
     * @param id 用户id
     * @return
     */
    @RequiresPermissions("system:base:view")
    @PostMapping("/delBaseUser")
    public @ResponseBody
    Result delBaseUser(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        try {
            if(null == id || 0 == id){
                return Result.newFail("未获取到参数信息");
            }
            BaseUser baseUser = baseUserService.getById(id);
            baseUserService.removeById(id);
            User user = userService.getOne(new QueryWrapper<User>().eq("mobile",baseUser.getPhone()));
            if(null != user){
                userService.remove(new QueryWrapper<User>().eq("open_id",user.getOpenId()));
                userAchievementService.remove(new QueryWrapper<UserAchievement>().eq("open_id",user.getOpenId()));
                userBranchService.remove(new QueryWrapper<UserBranch>().eq("open_id",user.getOpenId()));
                userActivityService.remove(new QueryWrapper<UserActivity>().eq("open_id",user.getOpenId()));
                userDayGameLogService.remove(new QueryWrapper<UserDayGameLog>().eq("open_id",user.getOpenId()));
                userGoodsService.remove(new QueryWrapper<UserGoods>().eq("open_id",user.getOpenId()));
                userGoodsAddressService.remove(new QueryWrapper<UserGoodsAddress>().eq("open_id",user.getOpenId()));
                userHonorService.remove(new QueryWrapper<UserHonor>().eq("open_id",user.getOpenId()));
                userLevelService.remove(new QueryWrapper<UserLevel>().eq("open_id",user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("friend_open_id",user.getOpenId()));
                // userOneVsOneLogService.remove(new QueryWrapper<UserOneVsOneLog>().eq("room_open_id",user.getOpenId()));

                // userThreeTeamLogService.remove(new QueryWrapper<UserThreeTeamLog>().eq("open_id",user.getOpenId()));
                userTeamVsTeamLogService.remove(new QueryWrapper<UserTeamVsTeamLog>().eq("open_id", user.getOpenId()));
                signService.remove(new QueryWrapper<Sign>().eq("open_id",user.getOpenId()));
                sevenSignService.remove(new QueryWrapper<SevenSign>().eq("open_id",user.getOpenId()));
                manguanService.remove(new QueryWrapper<Manguan>().eq("open_id",user.getOpenId()));
                hotLogService.remove(new QueryWrapper<HotLog>().eq("open_id",user.getOpenId()));
                feedbackService.remove(new QueryWrapper<Feedback>().eq("open_id",user.getOpenId()));
                alertTipsService.remove(new QueryWrapper<AlertTips>().eq("open_id",user.getOpenId()));
                experienceService.remove(new QueryWrapper<Experience>().eq("open_id",user.getOpenId()));
            }
        } catch (Exception e) {
            log.error("删除基本用户信息出错，错误信息: {}",e);
            return Result.newFail("删除基本用户信息出错");
        }
        return Result.newSuccess();
    }


    /**
     * 批量删除基本员工信息
     */
    @RequiresPermissions("system:base:view")
    @PostMapping("deleteBaseUsers")
    @ResponseBody
    public Result deleteBaseUsers(@RequestParam(value = "ids") String ids,
                                  @RequestParam(value = "id",required = false,defaultValue = "1") Long id) {
        int[] newIds = null;
        if (!"".equals(ids.trim()) && ids.trim() != null) {
            newIds = Arrays.stream(ids.trim().split(",")).mapToInt(Integer::parseInt).toArray();
        } else {
            return Result.newFaild("请选择需要批量删除基本用户！");
        }
        // 检查是否具有操作权限
        SysUserRole userRole = null;
        if (id == 0) {
            return Result.newFaild("缺少批量删除操作人员id");
        } else {
            userRole = userRoleService.getOne(new QueryWrapper<SysUserRole>().eq("user_id",id));
        }
        if (userRole != null) {
            SysRole sysRole = roleService.getById(userRole.getRoleId());
            if (sysRole == null || !sysRole.getRoleKey().equals("admin")) {
                return Result.newFaild("没有权限进行删除操作！");
            }
        } else {
            return Result.newFaild("没有权限进行删除操作！");
        }

        // 正式进行批量删除操作
        try {
            baseUserService.deleteBaseUsers(newIds);
        } catch (Exception e) {
            log.error("批量删除基本员工信息出错，错误信息: {}",e);
            return Result.newFaild("批量删除基本员工信息出错！");
        }

        return Result.newSuccess("批量删除基本员工信息成功");
    }


    /**
     * 批量导入基本员工信息（excel 二期）
     * @param file 上传基本员工数据excel文件
     * @param companyType 所属公司分类
     * @return
     */
    @RequiresPermissions("system:base:view")
    @PostMapping("/inputBaseUsersFromExcel")
    @ResponseBody
    public Result inputBaseUsersFromExcel(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "companyType", required = true,defaultValue = "1") Integer companyType) {
        String topicError = "";
        try {
            if (file == null) {
                return Result.newFaild("请选择批量上传基本员工信息 Excel文件！");
            }
            // 正式批量上传基本用户信息
            topicError = baseUserService.inputBaseUsersFromExcel(file,companyType);
        } catch (Exception e) {
            // 导入信息错误提示
            log.error("批量导入基本员工信息出错，错误信息: {}",e);
            return Result.newFail(e.getMessage());
        }

        return Result.newSuccess(topicError);
    }

}

