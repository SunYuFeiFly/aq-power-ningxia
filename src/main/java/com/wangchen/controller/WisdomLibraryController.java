package com.wangchen.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.*;
import com.wangchen.mapper.WisdomLibraryMapper;
import com.wangchen.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */

@Slf4j
@Controller
@RequestMapping("/system/audit")
public class WisdomLibraryController {

    @Autowired
    private WisdomLibraryMapper wisdomLibraryMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    @Autowired
    private WisdomLibraryService wisdomLibraryService;

    @Autowired
    private CompanyService companyService;


    /**
     * 进入页面 (二期)
     * @param model 数据模型
     * @param companyType 所属公司类型
     * @return
     */
    @RequiresPermissions("system:audit:view")
    @RequestMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        // 查询未处理的
        List<WisdomLibrary> wisdomLibraries01 = wisdomLibraryMapper.selectList(new QueryWrapper<WisdomLibrary>().eq("company_type", companyType).eq("status", 0));
        // 查询通过的
        List<WisdomLibrary> wisdomLibraries02 = wisdomLibraryMapper.selectList(new QueryWrapper<WisdomLibrary>().eq("company_type", companyType).eq("status", 1));
        // 查询驳回的
        List<WisdomLibrary> wisdomLibraries03 = wisdomLibraryMapper.selectList(new QueryWrapper<WisdomLibrary>().eq("company_type", companyType).eq("status", 2));
        // 查询所属公司分类下公司集合
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type", companyType));
        model.addAttribute("count0", wisdomLibraries01.size());
        model.addAttribute("count1", wisdomLibraries02.size());
        model.addAttribute("count2", wisdomLibraries03.size());
        model.addAttribute("companyType",companyType);
        model.addAttribute("companyList",companyList);
        if (1 == companyType) {
            return "audit/aList";
        }
        // 目前BC类不具有审题功能
//        if (2 == companyType) {
//            return "audit/bList";
//        }
//        if (3 == companyType) {
//            return "audit/cList";
//        }

        return null;
    }


    /**
     * 待审题目编辑页面 (二期)
     * @param model 模型数据
     * @param id 题目id
     * @param companyType 所属公司类型
     * @return
     */
    @RequiresPermissions("system:audit:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id,
                       @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        WisdomLibrary wisdomLibrary = wisdomLibraryService.getOne(new QueryWrapper<WisdomLibrary>().eq("id", id).eq("company_type", companyType));
        model.addAttribute("isEdit", id != null);
        model.addAttribute("companyType",companyType);
        model.addAttribute("id",id);
        if(wisdomLibrary.getTopicType().equals(1)){
            if(!StringUtils.isEmpty(wisdomLibrary.getContext2())){
                wisdomLibrary.setContext1(wisdomLibrary.getContext1()+","+wisdomLibrary.getContext2());
            }
        }
        model.addAttribute("wisdomLibrary", wisdomLibrary);
        if (1 == companyType) {
            return "audit/aEdit";
        } else if (2 == companyType) {
            return "audit/bEdit";
        } else if (2 == companyType) {
            return "audit/cEdit";
        }

        return null;
    }


    /**
     * 获取审核题目列表页面(二期)
     * @param model 数据模型
     * @param page 页码
     * @param limit 每页数据量
     * @param status 题目状态
     * @param type 题目所属部门id
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @return 审核题目列表数据
     */
    @RequiresPermissions("system:audit:view")
    @PostMapping("selectPages")
    public @ResponseBody
    Result selectPages(Model model,
                       @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                       @RequestParam(value = "status", required = false, defaultValue = "") String status,
                       @RequestParam(value = "type", required = false, defaultValue = "") Integer type,
                       @RequestParam(value = "topicType", required = false, defaultValue = "") String topicType,
                       @RequestParam(value = "companyType", required = false, defaultValue = "") String companyType) {
        try {
            Result result = wisdomLibraryService.selectPages(page, limit, status, type, topicType, companyType);
            return result;
        } catch (Exception e) {
            log.error("获取审核题目列表数据出错，错误信息: {}",e);
            return Result.newFaild("获取审核题目列表数据出错");
        }
    }


    /**
     * 通过、驳回题目（二期）
     * @param id 题目id
     * @param status 题目审核状态
     * @param tikuType 归属题库
     * @param companyType 题目划分所属公司分类
     * @return 题目审核结果
     */
    @RequiresPermissions("system:audit:view")
    @PostMapping("/passStatus")
    @ResponseBody
    public Result passStatus(@RequestParam(value = "id", required = false, defaultValue = "") Integer id,
                             @RequestParam(value = "status", required = false, defaultValue = "1") Integer status,
                             @RequestParam(value = "tikuType", required = false) Integer tikuType,
                             @RequestParam(value = "companyType", required = false) Integer companyType) {
        try {
            if(null == tikuType){
                return Result.newFail("选择题目专业后在审核！");
            }
            // 通过、驳回题目
            Result result = wisdomLibraryService.passStatus(id, status, tikuType, companyType);
            return result;
        } catch (Exception e) {
            log.error("通过、驳回题目出错，错误信息: {}",e);
            return Result.newFail("通过、驳回题目出错");
        }
    }


    /**
     * 使题目变为废弃状态(二期)
     * @param id 待审题目id
     * @param status 题目审核状态
     * @return 题目废弃实现情况
     */
    @RequiresPermissions("system:audit:view")
    @PostMapping("/rejectStatus")
    public @ResponseBody
    Result rejectStatus(@RequestParam(value = "id", required = false, defaultValue = "") Integer id,
                        @RequestParam(value = "status", required = false, defaultValue = "2") Integer status) {
        try {
            Result result = wisdomLibraryService.rejectStatus(id, status);
            return result;
        } catch (Exception e) {
            log.error("修改题目状态为废弃状态出错，错误信息: {}",e);
            return Result.newFail("修改题目状态为废弃状态出错");
        }
    }

}

