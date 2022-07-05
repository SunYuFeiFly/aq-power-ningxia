package com.wangchen.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.StringUtils;
import com.wangchen.vo.AllProvinceRankHouTaiLookVo;
import com.wangchen.vo.CompanyPersonalRankHouTaiLookVo;
import com.wangchen.vo.ThreeVsThreeRankVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 * 挑战排名 - 前端控制器
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/system/rank")
public class RankController {


    @Autowired
    private UserService userService;

    @Autowired
    private AllProvinceRankService allProvinceRankService;

    @Autowired
    private CompanyPersonalRankService companyPersonalRankService;

    @Autowired
    private CompanyRankService companyRankService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserThreeTeamRankService userThreeTeamRankService;


    /**
     * 全省个人排行榜信息页面(二期)
     * @param model 模型数据
     * @param companyType 所属公司类型
     * @return 跳转路径
     */
    @RequiresPermissions("system:rank:view")
    @RequestMapping("/allProvinceList")
    public String allProvinceList(Model model,
                                  @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        ArrayList<String> list = new ArrayList<>();
        list.add("年内总排行");
        list.add("历史总排行");
        model.addAttribute("typeList",list);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "rank/aAllProvinceList";
        } else if (2 == companyType) {
            return "rank/bAllProvinceList";
        } else if (3 == companyType) {
            return "rank/cAllProvinceList";
        }

        return null;
    }


    /**
     * 公司个人排行榜页面（二期）
     * @param model 模型数据
     * @param companyType 所属公司类型
     * @return 跳转路径
     */
    @RequiresPermissions("system:rank:view")
    @RequestMapping("/companyPersonalList")
    public String companyPersonalList(Model model,
                                      @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {

        List<Company> companyList =  companyService.list(new QueryWrapper<Company>().eq("type",companyType));
        ArrayList<String> list = new ArrayList<>();
        list.add("年内总排行");
        list.add("历史总排行");
        model.addAttribute("typeList",list);
        model.addAttribute("companyList",companyList);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "rank/aCompanyPersonalList";
        } else if (2 == companyType) {
            return "rank/bCompanyPersonalList";
        } else if (3 == companyType) {
            return "rank/cCompanyPersonalList";
        }

        return null;
    }


    /**
     * 公司排行榜页面(二期)
     * @param model 模型数据
     * @param companyType 所属公司类型
     * @return 跳转路径
     */
    @RequiresPermissions("system:rank:view")
    @RequestMapping("/companyList")
    public String companyList(Model model,
                              @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        ArrayList<String> list = new ArrayList<>();
        list.add("年内总排行");
        list.add("历史总排行");
        model.addAttribute("typeList",list);
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "rank/aCompanyList";
        } else if (2 == companyType) {
            return "rank/bCompanyList";
        } else if (3 == companyType) {
            return "rank/cCompanyList";
        }

        return null;
    }


    /**
     * 全省个人排行列表页面数据 （二期 后台）
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param companyType 所属公司排行类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @return 全省个人排行榜数据集合
     */
    @RequiresPermissions("system:rank:view")
    @PostMapping("selectPagesAll")
    @ResponseBody
    public Result selectPagesAll(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                          @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                          @RequestParam(value = "time", required = false, defaultValue = "") String time,
                          @RequestParam(value = "type", required = true, defaultValue = "1") Integer type,
                          @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        try {
            if(StringUtils.isEmpty(time)){
                // 省个人排行数据是由定时任务完成数据梳理，所以只能查看昨天及以前的排行记录
                time = DateUtils.getZuoTianDay();
            }
            if (time.equals(DateUtil.today())) {
                throw new BusinessException("无法查看当日的全省个人排行列数据，数据还未统计！");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2!");
            }
            Result result = allProvinceRankService.selectPagesAll(page, limit, time, type, companyType);
            return result;
        }catch (Exception e){
            log.error("查询全省个人排行榜信息出错，错误信息: {}",e);
            return Result.newFaild("查询全省个人排行榜息出错");
        }
    }


    /**
     * 公司个人排行榜 （二期 后台）
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param companyId 公司id
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param companyType 所属公司类型
     * @return 公司个人排行榜数据集合
     */
    @RequiresPermissions("system:rank:view")
    @PostMapping("selectPagesPersonal")
    @ResponseBody
    public Result selectPagesPersonal(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                      @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                      @RequestParam(value = "time", required = false, defaultValue = "") String time,
                                      @RequestParam(value = "companyId", required = false, defaultValue = "1") Integer companyId,
                                      @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                                      @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        try {
            if(StringUtils.isEmpty(time)){
                time = DateUtils.getZuoTianDay();
            }
            if (time.equals(DateUtil.today())) {
                throw new BusinessException("无法查看当日的公司个人排行列数据，数据还未统计！");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2!");
            }
            Result result = companyPersonalRankService.selectPagesPersonal(page, limit, time, companyId, type, companyType);
            return result;
        }catch (Exception e){
            log.error("查询公司个人排行榜信息出错，错误信息: {}",e);
            return Result.newFaild("查询公司个人排行榜信息出错");
        }
    }


    /**
     * 公司排行榜 (二期)
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param companyType 所属公司类型： 1:自有公司，2代维公司，3设计和监理公司
     * @param type 排行类型：1：历史排行  2：年内排行
     * @return 公司排行榜数据集合
     */
    @RequiresPermissions("system:rank:view")
    @PostMapping("selectPages")
    @ResponseBody
    public Result selectPages(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                              @RequestParam(value = "time", required = false, defaultValue = "") String time,
                              @RequestParam(value = "companyType", required = false) Integer companyType,
                              @RequestParam(value = "type", required = false, defaultValue = "1") Integer type) {
        try {
            if(StringUtils.isEmpty(time)){
                time = DateUtils.getZuoTianDay();
            }
            if (time.equals(DateUtil.today())) {
                throw new BusinessException("无法查看当日的公司排行列数据，数据还未统计！");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2!");
            }

            Result result = companyRankService.selectPages(page, limit, time, companyType, type);
            return result;
        }catch (Exception e){
            log.error("查询公司排行榜信息出错，错误信息: {}",e);
            return Result.newFaild("查询公司排行榜信息出错");
        }
    }


    /**
     * 团队赛排行榜
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:19 2020/7/13
     */
    @RequiresPermissions("system:rank:view")
    @PostMapping("selectPagesThreeVsThree")
    public @ResponseBody
    Result selectPagesThreeVsThree(Model model,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                               @RequestParam(value = "time", required = false, defaultValue = "") String time) {
        try {
            if(StringUtils.isEmpty(time)){
                time = DateUtils.getZuoTianDay();
            }
            IPage<UserThreeTeamRank> userThreeTeamRankIPage =
                    userThreeTeamRankService.page(new Page<UserThreeTeamRank>(page, limit),
                            new QueryWrapper<UserThreeTeamRank>().eq("rank_date", time).orderByAsc("id"));

            List<ThreeVsThreeRankVo> threeVsThreeRankVoList = new ArrayList<ThreeVsThreeRankVo>();

            for(int i = 1; i<= userThreeTeamRankIPage.getRecords().size();i++){
                if(org.apache.commons.lang.StringUtils.isNotEmpty(userThreeTeamRankIPage.getRecords().get(i-1).getContext())){
                    ThreeVsThreeRankVo vsThreeRankVo = JSONObject.parseObject(userThreeTeamRankIPage.getRecords().get(i-1).getContext(),ThreeVsThreeRankVo.class);
                    vsThreeRankVo.setRankNo(userThreeTeamRankIPage.getRecords().get(i-1).getRankNo());
                    vsThreeRankVo.setScore(userThreeTeamRankIPage.getRecords().get(i-1).getScore());
                    threeVsThreeRankVoList.add(vsThreeRankVo);
                }
            }

            return ResultLayuiTable.newSuccess(userThreeTeamRankIPage.getTotal(), threeVsThreeRankVoList);

        }catch (Exception e){
            log.error("查询团队赛排行榜错误:{}",e);
        }
        return null;
    }


    @PostMapping("test")
    public void test() {
        List<CompanyRank> companyRankList = companyRankService.list(new QueryWrapper<CompanyRank>().eq("company_type", 1).eq("type", 2));
        for (CompanyRank companyRank : companyRankList) {
            companyRank.setRankNo(19-companyRank.getRankNo());
            companyRankService.updateById(companyRank);
        }

    }
}