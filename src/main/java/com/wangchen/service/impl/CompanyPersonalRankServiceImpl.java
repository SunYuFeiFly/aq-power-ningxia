package com.wangchen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.CompanyPersonalRank;
import com.wangchen.entity.User;
import com.wangchen.entity.UserBranch;
import com.wangchen.entity.UserHonor;
import com.wangchen.mapper.CompanyPersonalRankMapper;
import com.wangchen.service.*;
import com.wangchen.vo.CompanyPersonalRankHouTaiLookVo;
import com.wangchen.vo.CompanyPersonalRankQianDuanLookVo;
import com.wangchen.vo.CompanyPersonalRankVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 公司个人排行榜表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Slf4j
@Service
public class CompanyPersonalRankServiceImpl extends ServiceImpl<CompanyPersonalRankMapper, CompanyPersonalRank> implements CompanyPersonalRankService {

    @Autowired
    private CompanyPersonalRankMapper companyPersonalRankMapper;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    /**
     * 公司个人排行榜 （二期 后台）
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param companyId 公司id
     * @param type 排行类型：1：历史排行  2：年内排行
     * @return 公司个人排行榜数据集合
     */
    @Override
    @Transactional
    public Result selectPagesPersonal(int page, int limit, String time, Integer companyId, Integer type, Integer companyType) {
        IPage<CompanyPersonalRank> companyPersonalRankIPage = companyPersonalRankMapper.selectPage(new Page<CompanyPersonalRank>(page, limit), new QueryWrapper<CompanyPersonalRank>()
                .eq("rank_date", time).eq(null != companyId,"company_id", companyId).eq(null != type,"type", type)
                .eq(null != companyType,"company_type", companyType).orderByAsc("rank_no"));

        List<CompanyPersonalRank> companyPersonalRankList = companyPersonalRankIPage.getRecords();
        List<CompanyPersonalRankHouTaiLookVo> companyPersonalRankHouTaiLookVoList = new ArrayList<CompanyPersonalRankHouTaiLookVo>();
        if (CollUtil.isNotEmpty(companyPersonalRankList)) {
            for(CompanyPersonalRank companyPersonalRank : companyPersonalRankList){
                CompanyPersonalRankHouTaiLookVo vo = new CompanyPersonalRankHouTaiLookVo();
                vo.setRankNo(companyPersonalRank.getRankNo());
                vo.setOpenId(companyPersonalRank.getOpenId());
                vo.setName(companyPersonalRank.getName());
                vo.setAllAch(companyPersonalRank.getAllAch());
                vo.setAllExp(companyPersonalRank.getAllExp());
                vo.setCompositeScore(companyPersonalRank.getCompositeScore());
                vo.setBranchName(companyPersonalRank.getBranchName());
                vo.setCompanyName(companyPersonalRank.getCompanyName());
                vo.setHonorName(companyPersonalRank.getHonorName());
                companyPersonalRankHouTaiLookVoList.add(vo);
            }
        }
        if (CollUtil.isNotEmpty(companyPersonalRankHouTaiLookVoList)) {
            return ResultLayuiTable.newSuccess(companyPersonalRankIPage.getTotal(), companyPersonalRankHouTaiLookVoList);
        } else {
            return ResultLayuiTable.newFaild("公司个人排行榜数据为空");
        }
    }


    /**
     * 查询公司个人排行榜信息 （二期 小程序）
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 公司个人排行榜数据集合
     */
    @Override
    @Transactional
    public Map<String, Object> getCompanyPersonalRankList(String openId, int page, int limit, Integer type, Integer cType) {
        Map<String,Object> map = new HashMap<String,Object>();
        List<CompanyPersonalRankVo> companyPersonalRankVos = new ArrayList<>();
        User user = userService.getUserByOpenId(openId);
        Integer companyId = user.getCompanyId();
        if (!cType.equals(user.getType())) {
            throw new BusinessException("不能查询非自己公司的公司个人榜");
        }
        if (1 == type) {
            // 全省个人排行榜（历史总排行）
            companyPersonalRankVos = companyPersonalRankMapper.getCompanyPersonalRankListForAll(companyId);
        } else if (2 == type){
            // 全省个人排行榜（年内总排行）
            companyPersonalRankVos = companyPersonalRankMapper.getCompanyPersonalRankListForYear(companyId);
        } else {
            throw new BusinessException("选择排行类型数据错误，只能为1/2");
        }

        // 参与排名名次
        Integer ranking = 1;
        // 个人排名数据是否在本次查询区间中
        Boolean isExit = false;
        // 数据封装
        List<CompanyPersonalRankQianDuanLookVo> companyPersonalRankQianDuanLookVos = new ArrayList<>();
        if (CollUtil.isNotEmpty(companyPersonalRankVos)) {
            // 查询页码不大于数据页，才进行数据遍历疯转
            if (Math.ceil((double) companyPersonalRankVos.size() / (double)limit) >= page) {
                Integer start = (page-1) * limit;
                Integer end = page * limit > companyPersonalRankVos.size() ? companyPersonalRankVos.size() : page * limit;
                for (int i = start; i < end; i++) {
                    CompanyPersonalRankQianDuanLookVo companyPersonalRankQianDuanLookVo = new CompanyPersonalRankQianDuanLookVo();
                    BeanUtil.copyProperties(companyPersonalRankVos.get(i),companyPersonalRankQianDuanLookVo);
                    companyPersonalRankQianDuanLookVo.setRankNo(i + 1);
                    companyPersonalRankQianDuanLookVo.setRankDate(new Date());
                    companyPersonalRankQianDuanLookVo.setCreateTime(new Date());
                    // 完善称号id、称号名称（返回最新拥有那个）
                    // 查询用户称号集合
                    List<UserHonor> userHonors = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",companyPersonalRankQianDuanLookVo.getOpenId()).orderByDesc("create_time"));
                    if (CollUtil.isNotEmpty(userHonors)) {
                        companyPersonalRankQianDuanLookVo.setHonorNo(userHonors.get(0).getHonorId());
                        companyPersonalRankQianDuanLookVo.setHonorName(userHonors.get(0).getHonorName());
                    } else {
                        companyPersonalRankQianDuanLookVo.setHonorNo(0);
                        companyPersonalRankQianDuanLookVo.setHonorName(null);
                    }
                    // 完善部门名称（如果有多个，返回id最小那个）
                    // 查询用户部门集合
                    List<UserBranch> userBranchs = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",companyPersonalRankQianDuanLookVo.getOpenId()).orderByAsc("id"));
                    if (CollUtil.isNotEmpty(userBranchs)) {
                        companyPersonalRankQianDuanLookVo.setBranchName(userBranchs.get(0).getBranchName());
                    } else {
                        log.debug("用户相关所属部门不能为空、open_id ：{}",companyPersonalRankQianDuanLookVo.getOpenId());
                        throw new BusinessException("用户相关所属部门不能为空！");
                    }
                    // 判断个人排名数据是否在本次查询区间中
                    if (!isExit && openId.equals(companyPersonalRankVos.get(i).getOpenId())) {
                        isExit = true;
                        ranking = i + 1;
                    }

                    companyPersonalRankQianDuanLookVos.add(companyPersonalRankQianDuanLookVo);
                }
                map.put("rankList",companyPersonalRankQianDuanLookVos);
            } else {
                map.put("rankList",null);
            }
        }

        // 是否通过全面遍历查询到用户排名
        Boolean isSuccess = false;
        if (!isExit) {
            if (CollUtil.isNotEmpty(companyPersonalRankVos)) {
                for (int i = 0; i < companyPersonalRankVos.size(); i++) {
                    if (openId.equals(companyPersonalRankVos.get(i).getOpenId())) {
                        ranking = i + 1;
                        isSuccess = true;
                        break;
                    }
                }
            }
        }
        // 设置用户参与排名信息
        if (isExit || isSuccess) {
            map.put("userRankNo",ranking);
        } else {
            map.put("userRankNo", "未参与");
        }

        return map;
    }
}
