package com.wangchen;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.mapper.AllProvinceRankMapper;
import com.wangchen.mapper.CompanyPersonalRankMapper;
import com.wangchen.mapper.CompanyRankMapper;
import com.wangchen.mapper.UserMapper;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.AllProvinceRankVo;
import com.wangchen.vo.CompanyPersonalRankVo;
import com.wangchen.vo.CompanyRankVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台功能（二期）：定时更新历史总排行、年内总排行记录（全省个人排行榜信息、公司个人排行榜信息、所有公司的排行榜）
 * @Description: zhangcheng
 * @Date: 2020/5/17 16:10
 */

@Slf4j
@Configuration
@EnableScheduling
@RequestMapping("/system/rankTask")
public class RankTask {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Environment env;

    @Autowired
    private AllProvinceRankMapper allProvinceRankMapper;

    @Autowired
    private AllProvinceRankService allProvinceRankService;

    @Autowired
    private CompanyPersonalRankMapper companyPersonalRankMapper;

    @Autowired
    private CompanyPersonalRankService companyPersonalRankService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRankMapper companyRankMapper;

    @Autowired
    private CompanyRankService companyRankService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserBranchService userBranchService;


    /**
     * 后台管理：全省个人排行榜信息（二期 历史总排行 每天凌晨00:01:00 更新）
     */
    @Scheduled(cron = "0 1 0 * * ? ")
    // @RequestMapping("allProvinceRankTaskForAll")
    public String allProvinceRankTaskForAll() {
        try {
            Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
            ArrayList<AllProvinceRank> allProvinceRanks = new ArrayList<AllProvinceRank>();
            List<AllProvinceRankVo> allProvinceRankListMap = allProvinceRankMapper.allProvinceRankForAll();
            if (CollUtil.isNotEmpty(allProvinceRankListMap)) {
                // 根据用户类型分组
                Map<Integer, List<AllProvinceRankVo>> allProvinceRankVos = allProvinceRankListMap.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(AllProvinceRankVo::getType));
                Iterator<Integer> iterator = allProvinceRankVos.keySet().iterator();
                while (iterator.hasNext()) {
                    int index = (int) iterator.next();
                    List<AllProvinceRankVo> allProvinceRankVoList = allProvinceRankVos.get(index);
                    if (CollUtil.isNotEmpty(allProvinceRankVoList)) {
                        // 遍历
                        for (int i = 1; i <= allProvinceRankVoList.size(); i++) {
                            AllProvinceRank allProvinceRank = new AllProvinceRank();
                            allProvinceRank.setRankNo(i);
                            allProvinceRank.setLevelNo(allProvinceRankVoList.get(i-1).getLevelNo());
                            allProvinceRank.setOpenId(allProvinceRankVoList.get(i-1).getOpenId());
                            allProvinceRank.setName(allProvinceRankVoList.get(i-1).getName());
                            allProvinceRank.setAvatar(allProvinceRankVoList.get(i-1).getAvatar());
                            allProvinceRank.setCompanyName(allProvinceRankVoList.get(i-1).getCompanyName());
                            allProvinceRank.setRankDate(date);
                            allProvinceRank.setAllAch(allProvinceRankVoList.get(i-1).getAllAch());
                            allProvinceRank.setAllExp(allProvinceRankVoList.get(i-1).getAllExp());
                            allProvinceRank.setCompositeScore(allProvinceRankVoList.get(i-1).getCompositeScore());
                            allProvinceRank.setCreateTime(new Date());
                            // 查询用户拥有那些称号
                            List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>()
                                    .eq("open_id",allProvinceRankVoList.get(i-1).getOpenId()).orderByDesc("create_time"));
                            if(CollectionUtils.isNotEmpty(userHonorList)){
                                allProvinceRank.setHonorNo(userHonorList.get(0).getHonorId());
                                allProvinceRank.setHonorName(userHonorList.get(0).getHonorName());
                            }else{
                                allProvinceRank.setHonorNo(0);
                                allProvinceRank.setHonorName("暂未获得段位");
                            }
                            // 查询用户属于那些部门
                            List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>()
                                    .eq("open_id",allProvinceRankVoList.get(i-1).getOpenId()));
                            if(CollectionUtils.isNotEmpty(userBranchList)){
                                allProvinceRank.setBranchName(userBranchList.get(0).getBranchName());
                            }else{
                                allProvinceRank.setBranchName("暂未拥有部门");
                            }
                            // 设置用户所属公司类型
                            allProvinceRank.setCompanyType(index);
                            // 设置排行榜类型 1:历史总排行 2:年内总排行
                            allProvinceRank.setType(1);

                            // allProvinceRankMapper.insert(allProvinceRank);
                            // 修改为批量插入
                            allProvinceRanks.add(allProvinceRank);
                        }
                    }
                }

                // 批量插入
                allProvinceRankService.saveBatch(allProvinceRanks);
            }

        } catch (Exception e){
            e.printStackTrace();
            log.error("后台管理：全省排行榜信息出错、 错误信息: {}",e);
        }

        return "/rank/allProvinceList";
    }


    /**
     * 后台管理：全省个人排行榜信息（二期 年内总排行 每天凌晨00:02:30 更新）
     */
    @Scheduled(cron = "30 2 0 * * ? ")
    // @RequestMapping("allProvinceRankTaskForYear")
    public String allProvinceRankTaskForYear() {
        try {
            Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
            ArrayList<AllProvinceRank> allProvinceRanks = new ArrayList<AllProvinceRank>();
            List<AllProvinceRankVo> allProvinceRankVoListMap = allProvinceRankMapper.allProvinceRankForYear();
            if (CollUtil.isNotEmpty(allProvinceRankVoListMap)) {
                // 根据用户类型分组
                Map<Integer, List<AllProvinceRankVo>> allProvinceRankVos = allProvinceRankVoListMap.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(AllProvinceRankVo::getType));
                Iterator<Integer> iterator = allProvinceRankVos.keySet().iterator();
                while (iterator.hasNext()) {
                    int index = (int) iterator.next();
                    List<AllProvinceRankVo> allProvinceRankVoList = allProvinceRankVos.get(index);
                    if (CollUtil.isNotEmpty(allProvinceRankVoList)) {
                        // 遍历
                        for (int i = 1; i <= allProvinceRankVoList.size(); i++) {
                            AllProvinceRank allProvinceRank = new AllProvinceRank();
                            allProvinceRank.setRankNo(i);
                            allProvinceRank.setLevelNo(allProvinceRankVoList.get(i-1).getLevelNo());
                            allProvinceRank.setOpenId(allProvinceRankVoList.get(i-1).getOpenId());
                            allProvinceRank.setName(allProvinceRankVoList.get(i-1).getName());
                            allProvinceRank.setAvatar(allProvinceRankVoList.get(i-1).getAvatar());
                            allProvinceRank.setCompanyName(allProvinceRankVoList.get(i-1).getCompanyName());
                            allProvinceRank.setRankDate(date);
                            allProvinceRank.setAllAch(allProvinceRankVoList.get(i-1).getAllAch());
                            allProvinceRank.setAllExp(allProvinceRankVoList.get(i-1).getAllExp());
                            allProvinceRank.setCompositeScore(allProvinceRankVoList.get(i-1).getCompositeScore());
                            allProvinceRank.setCreateTime(new Date());
                            // 查询用户拥有那些称号
                            List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>()
                                    .eq("open_id",allProvinceRankVoList.get(i-1).getOpenId()).orderByDesc("create_time"));
                            if(CollectionUtils.isNotEmpty(userHonorList)){
                                allProvinceRank.setHonorNo(userHonorList.get(0).getHonorId());
                                allProvinceRank.setHonorName(userHonorList.get(0).getHonorName());
                            }else{
                                allProvinceRank.setHonorNo(0);
                                allProvinceRank.setHonorName("暂未获得段位");
                            }
                            // 查询用户属于那些部门
                            List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>()
                                    .eq("open_id",allProvinceRankVoList.get(i-1).getOpenId()));
                            if(CollectionUtils.isNotEmpty(userBranchList)){
                                allProvinceRank.setBranchName(userBranchList.get(0).getBranchName());
                            }else{
                                allProvinceRank.setBranchName("暂未拥有部门");
                            }
                            // 设置用户所属公司类型
                            allProvinceRank.setCompanyType(index);
                            // 设置排行榜类型 1:历史总排行 2:年内总排行
                            allProvinceRank.setType(2);

                            // allProvinceRankMapper.insert(allProvinceRank);
                            // 修改为批量插入
                            allProvinceRanks.add(allProvinceRank);
                        }
                    }
                }
            }

            // 批量插入
            allProvinceRankService.saveBatch(allProvinceRanks);
        } catch (Exception e){
            e.printStackTrace();
            log.error("后台管理：年内排行榜信息出错、 错误信息: {}",e);
        }

        return "/rank/allProvinceList";
    }


    /**
     * 后台管理：公司个人排行榜信息(二期 历史总排行 每天凌晨00:04:00 更新)
     */
    @Scheduled(cron = "0 4 0 * * ? ")
    // @RequestMapping("companyPersonalRankTaskForAll")
    public String companyPersonalRankTaskForAll() {
        try{
            // 单独根据公司id去查，前后查询次数过多，现采用一次查询，后再公司类型分组、分公司分组操作
            List<CompanyPersonalRankVo> companyPersonalRankVoList = companyPersonalRankMapper.companyPersonalRankForAll();
            if (CollUtil.isNotEmpty(companyPersonalRankVoList)) {
                Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
                // 首先根据公司类型进行分组
                Map<Integer, List<CompanyPersonalRankVo>> collect = companyPersonalRankVoList.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(CompanyPersonalRankVo::getType));
                // 不同公司类型基础上在进行分组
                Iterator<Integer> iterator = collect.keySet().iterator();
                while (iterator.hasNext()) {
                    int index = iterator.next();
                    List<CompanyPersonalRankVo> companyPersonalRankVos = collect.get(index);
                    if (CollUtil.isNotEmpty(companyPersonalRankVos)) {
                        // 不同公司再进行分组
                        Map<String, List<CompanyPersonalRankVo>> collect1 = companyPersonalRankVos.stream().filter(item -> item.getCompanyName() != null).collect(Collectors.groupingBy(CompanyPersonalRankVo::getCompanyName));
                        Iterator<String> iterator1 = collect1.keySet().iterator();
                        while (iterator1.hasNext()) {
                            String companyName = iterator1.next();
                            List<CompanyPersonalRankVo> companyPersonalRankVos1 = collect1.get(companyName);
                            if (CollUtil.isNotEmpty(companyPersonalRankVos1)) {
                                Integer companyId = companyPersonalRankVos1.get(0).getCompanyId();
                                List<CompanyPersonalRank> companyPersonalRanks = new ArrayList<>();
                                if (CollUtil.isNotEmpty(companyPersonalRankVos1)) {
                                    for(int i = 1; i <= companyPersonalRankVos1.size(); i++) {
                                        // 遍历赋值
                                        CompanyPersonalRank companyPersonalRank = new CompanyPersonalRank();
                                        companyPersonalRank.setCompanyId(companyId);
                                        companyPersonalRank.setRankNo(i);
                                        companyPersonalRank.setLevelNo(companyPersonalRankVos1.get(i-1).getLevelNo());
                                        companyPersonalRank.setOpenId(companyPersonalRankVos1.get(i-1).getOpenId());
                                        companyPersonalRank.setName(companyPersonalRankVos1.get(i-1).getName());
                                        companyPersonalRank.setAvatar(companyPersonalRankVos1.get(i-1).getAvatar());
                                        companyPersonalRank.setCompanyName(companyPersonalRankVos1.get(i-1).getCompanyName());
                                        companyPersonalRank.setRankDate(date);
                                        companyPersonalRank.setCompositeScore(companyPersonalRankVos1.get(i-1).getCompositeScore());
                                        companyPersonalRank.setCreateTime(new Date());
                                        companyPersonalRank.setAllAch(companyPersonalRankVos1.get(i-1).getAllAch());
                                        companyPersonalRank.setAllExp(companyPersonalRankVos1.get(i-1).getAllExp());
                                        // 用户获取过的称号集合
                                        List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>()
                                                .eq("open_id",companyPersonalRankVoList.get(i-1).getOpenId()).orderByDesc("create_time"));
                                        if(CollectionUtils.isNotEmpty(userHonorList)){
                                            companyPersonalRank.setHonorNo(userHonorList.get(0).getHonorId());
                                            companyPersonalRank.setHonorName(userHonorList.get(0).getHonorName());
                                        }else{
                                            companyPersonalRank.setHonorNo(0);
                                            companyPersonalRank.setHonorName("暂未获得段位");
                                        }
                                        // 用户所属部门集合
                                        List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>()
                                                .eq("open_id",companyPersonalRankVoList.get(i-1).getOpenId()));
                                        if(CollectionUtils.isNotEmpty(userBranchList)){
                                            companyPersonalRank.setBranchName(userBranchList.get(0).getBranchName());
                                        }else{
                                            companyPersonalRank.setBranchName("暂未拥有部门");
                                        }
                                        // 设置公司所属类型
                                        companyPersonalRank.setCompanyType(companyPersonalRankVos1.get(i-1).getType());
                                        // 设置排行榜类型 1:历史总排行 2:年内总排行
                                        companyPersonalRank.setType(1);
                                        companyPersonalRanks.add(companyPersonalRank);
                                    }
                                }

                                // 批量插入
                                companyPersonalRankService.saveBatch(companyPersonalRanks);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("历史公司个人排行榜信息出错、 错误信息: {}",e.getMessage());
        }

        return "/rank/companyPersonalList";
    }


    /**
     * 后台管理：公司个人排行榜信息(二期 年内总排行 每天凌晨00:05:30 更新)
     */
    @Scheduled(cron = "30 5 0 * * ? ")
    // @RequestMapping("companyPersonalRankTaskForYear")
    public String companyPersonalRankTaskForYear() {
        try{
            // 单独根据公司id去查，前后查询次数过多，现采用一次查询，后再公司类型分组、分公司分组操作
            List<CompanyPersonalRankVo> companyPersonalRankVoList = companyPersonalRankMapper.companyPersonalRankForYear();
            if (CollUtil.isNotEmpty(companyPersonalRankVoList)) {
                Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
                // 首先根据公司类型进行分组
                Map<Integer, List<CompanyPersonalRankVo>> collect = companyPersonalRankVoList.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(CompanyPersonalRankVo::getType));
                // 不同公司类型基础上在进行分组
                Iterator<Integer> iterator = collect.keySet().iterator();
                while (iterator.hasNext()) {
                    int index = iterator.next();
                    List<CompanyPersonalRankVo> companyPersonalRankVos = collect.get(index);
                    if (CollUtil.isNotEmpty(companyPersonalRankVos)) {
                        // 不同公司再进行分组
                        Map<String, List<CompanyPersonalRankVo>> collect1 = companyPersonalRankVos.stream().filter(item -> item.getCompanyName() != null).collect(Collectors.groupingBy(CompanyPersonalRankVo::getCompanyName));
                        Iterator<String> iterator1 = collect1.keySet().iterator();
                        while (iterator1.hasNext()) {
                            String companyName = iterator1.next();
                            List<CompanyPersonalRankVo> companyPersonalRankVos1 = collect1.get(companyName);
                            if (CollUtil.isNotEmpty(companyPersonalRankVos1)) {
                                Integer companyId = companyPersonalRankVos1.get(0).getCompanyId();
                                List<CompanyPersonalRank> companyPersonalRanks = new ArrayList<>();
                                if (CollUtil.isNotEmpty(companyPersonalRankVos1)) {
                                    for(int i = 1; i <= companyPersonalRankVos1.size(); i++) {
                                        // 遍历赋值
                                        CompanyPersonalRank companyPersonalRank = new CompanyPersonalRank();
                                        companyPersonalRank.setCompanyId(companyId);
                                        companyPersonalRank.setRankNo(i);
                                        companyPersonalRank.setLevelNo(companyPersonalRankVos1.get(i-1).getLevelNo());
                                        companyPersonalRank.setOpenId(companyPersonalRankVos1.get(i-1).getOpenId());
                                        companyPersonalRank.setName(companyPersonalRankVos1.get(i-1).getName());
                                        companyPersonalRank.setAvatar(companyPersonalRankVos1.get(i-1).getAvatar());
                                        companyPersonalRank.setCompanyName(companyPersonalRankVos1.get(i-1).getCompanyName());
                                        companyPersonalRank.setRankDate(date);
                                        companyPersonalRank.setCompositeScore(companyPersonalRankVos1.get(i-1).getCompositeScore());
                                        companyPersonalRank.setCreateTime(new Date());
                                        companyPersonalRank.setAllAch(companyPersonalRankVos1.get(i-1).getAllAch());
                                        companyPersonalRank.setAllExp(companyPersonalRankVos1.get(i-1).getAllExp());
                                        // 用户获取过的称号集合
                                        List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>()
                                                .eq("open_id",companyPersonalRankVoList.get(i-1).getOpenId()).orderByDesc("create_time"));
                                        if(CollectionUtils.isNotEmpty(userHonorList)){
                                            companyPersonalRank.setHonorNo(userHonorList.get(0).getHonorId());
                                            companyPersonalRank.setHonorName(userHonorList.get(0).getHonorName());
                                        }else{
                                            companyPersonalRank.setHonorNo(0);
                                            companyPersonalRank.setHonorName("暂未获得段位");
                                        }
                                        // 用户所属部门集合
                                        List<UserBranch> userBranchList = userBranchService.list(new QueryWrapper<UserBranch>()
                                                .eq("open_id",companyPersonalRankVoList.get(i-1).getOpenId()));
                                        if(CollectionUtils.isNotEmpty(userBranchList)){
                                            companyPersonalRank.setBranchName(userBranchList.get(0).getBranchName());
                                        }else{
                                            companyPersonalRank.setBranchName("暂未拥有部门");
                                        }
                                        // 设置公司所属类型
                                        companyPersonalRank.setCompanyType(companyPersonalRankVos1.get(i-1).getType());
                                        // 设置排行榜类型 1:历史总排行 2:年内总排行
                                        companyPersonalRank.setType(2);
                                        companyPersonalRanks.add(companyPersonalRank);
                                    }
                                }

                                // 批量插入
                                companyPersonalRankService.saveBatch(companyPersonalRanks);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("年内公司个人排行榜信息出错、 错误信息: {}",e);
        }

        return "/rank/companyPersonalList";
    }


    /**
     * 后台管理：所有公司的排行榜(二期 历史总排行 每天凌晨00:07:00 更新)
     */
    @Scheduled(cron = "0 7 0 * * ? ")
    // @RequestMapping("companyRankTaskForAll")
    public String companyRankTaskForAll() {
        try{
            List<CompanyRankVo> companyRankVoList = companyRankMapper.companyRankListForAll();
            if (CollUtil.isNotEmpty(companyRankVoList)) {
                Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
                // 根据所属公司类型进行分类(主要对数据进行处理)
                Map<Integer, List<CompanyRankVo>> map = companyRankVoList.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(CompanyRankVo::getType));
                Iterator<Integer> iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    int key = iterator.next();
                    List<CompanyRankVo> companyRankVos = map.get(key);
                    ArrayList<CompanyRank> companyRankList = new ArrayList<>();
                    for (int i = 0; i < companyRankVos.size(); i++) {
                        // 查询公司下游戏用户
                        List<User> users = userService.list(new QueryWrapper<User>().eq("company_id", companyRankVos.get(i).getCompanyId()).eq("deleted",0));
                        CompanyRank companyRank = new CompanyRank();
                        companyRank.setCompanyId(companyRankVoList.get(i).getCompanyId());
                        companyRank.setCompanyName(companyRankVoList.get(i).getCompanyName());
                        companyRank.setRankDate(date);
                        companyRank.setCount(users.size());
                        companyRank.setRankNo(i + 1);
                        companyRank.setCreateTime(new Date());
                        companyRank.setCompanyType(key);
                        companyRank.setType(1);
                        if (CollUtil.isNotEmpty(users)) {
                            companyRank.setCompositeScore(companyRankVoList.get(i).getCompositeScore()/users.size());
                        } else {
                            companyRank.setCompositeScore(0.00);
                        }

                        companyRankList.add(companyRank);
                    }
                    // 对公司排名
                    List<CompanyRank> newCompanyRankVos = companyRankList.stream().sorted(Comparator.comparing(CompanyRank::getCompositeScore)).collect(Collectors.toList());
                    for (int j = 0; j < newCompanyRankVos.size(); j++) {
                        newCompanyRankVos.get(j).setRankNo(newCompanyRankVos.size() - j);
                    }

                    // 批量插入
                    companyRankService.saveBatch(newCompanyRankVos);
                }
            }
        }catch (Exception e){
            log.error("所有公司的排行榜出错、 错误信息: {}",e);
        }

        return "/rank/companyList";
    }


    /**
     * 后台管理：所有公司的排行榜(二期 年内总排行 每天凌晨00:08:30 更新)
     */
    @Scheduled(cron = "30 8 0 * * ? ")
    // @RequestMapping("companyRankTaskForYear")
    public String companyRankTaskForYear() {
        try{
            List<CompanyRankVo> companyRankVoList = companyRankMapper.companyRankListForYear();
            if (CollUtil.isNotEmpty(companyRankVoList)) {
                Date date = Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay());
                // 根据所属公司类型进行分类(主要对数据进行处理)
                Map<Integer, List<CompanyRankVo>> map = companyRankVoList.stream().filter(item -> item.getType() != null).collect(Collectors.groupingBy(CompanyRankVo::getType));
                Iterator<Integer> iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    int key = iterator.next();
                    List<CompanyRankVo> companyRankVos = map.get(key);
                    companyRankVos = companyRankVos.stream().sorted(Comparator.comparing(CompanyRankVo::getCompositeScore).reversed()).collect(Collectors.toList());
                    ArrayList<CompanyRank> companyRankList = new ArrayList<>();
                    for (int i = 0; i < companyRankVos.size(); i++) {
                        // 查询公司下游戏用户
                        List<User> users = userService.list(new QueryWrapper<User>().eq("company_id", companyRankVos.get(i).getCompanyId()));
                        CompanyRank companyRank = new CompanyRank();
                        companyRank.setCompanyId(companyRankVoList.get(i).getCompanyId());
                        companyRank.setCompanyName(companyRankVoList.get(i).getCompanyName());
                        companyRank.setRankDate(date);
                        companyRank.setCount(users.size());
                        companyRank.setRankNo(i + 1);
                        companyRank.setCreateTime(new Date());
                        companyRank.setCompanyType(key);
                        companyRank.setType(2);
                        if (CollUtil.isNotEmpty(users)) {
                            companyRank.setCompositeScore(companyRankVoList.get(i).getCompositeScore()/users.size());
                        } else {
                            companyRank.setCompositeScore(0.00);
                        }

                        companyRankList.add(companyRank);
                    }
                    // 对公司排名
                    List<CompanyRank> newCompanyRankVos = companyRankList.stream().sorted(Comparator.comparing(CompanyRank::getCompositeScore)).collect(Collectors.toList());
                    // 公司排名重排
                    for (int j = 0; j < newCompanyRankVos.size(); j++) {
                        newCompanyRankVos.get(j).setRankNo(newCompanyRankVos.size() - j);
                    }

                    // 批量插入
                    companyRankService.saveBatch(newCompanyRankVos);
                }
            }
        }catch (Exception e){
            log.error("所有公司的排行榜出错、 错误信息: {}",e);
        }

        return "/rank/companyList";
    }

}