package com.wangchen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.AllProvinceRank;
import com.wangchen.entity.UserBranch;
import com.wangchen.entity.UserHonor;
import com.wangchen.mapper.AllProvinceRankMapper;
import com.wangchen.service.AllProvinceRankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.service.UserBranchService;
import com.wangchen.service.UserHonorService;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.AllProvinceRankHouTaiLookVo;
import com.wangchen.vo.AllProvinceRankQianDuanLookVo;
import com.wangchen.vo.AllProvinceRankVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.*;

/**
 * <p>
 * 全省排行榜信息 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */

@Service
public class AllProvinceRankServiceImpl extends ServiceImpl<AllProvinceRankMapper, AllProvinceRank> implements AllProvinceRankService {

    @Autowired
    private AllProvinceRankMapper allProvinceRankMapper;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private UserBranchService userBranchService;

    /**
     * 全省个人排行列表页面数据 （二期 后台）
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param companyType 所属公司排行类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @return 全省个人排行榜数据集合
     */
    @Override
    @Transactional
    public Result selectPagesAll(int page, int limit, String time, Integer type, Integer companyType) {
        // 获取固定时间、固定类型排行榜信息
        IPage<AllProvinceRank> allProvinceRankListPage = allProvinceRankMapper.selectPage(new Page<>(page, limit),new QueryWrapper<AllProvinceRank>()
                .eq("rank_date", time).eq("type",type).eq("company_type",companyType).orderByAsc("id"));
        List<AllProvinceRank> allProvinceRankList = allProvinceRankListPage.getRecords();
        List<AllProvinceRankHouTaiLookVo> allProvinceRankHotTaiLookVoList = new ArrayList<AllProvinceRankHouTaiLookVo>();
        if (CollUtil.isNotEmpty(allProvinceRankList)) {
            for(AllProvinceRank allProvinceRank : allProvinceRankList){
                AllProvinceRankHouTaiLookVo vo = new AllProvinceRankHouTaiLookVo();
                vo.setRankNo(allProvinceRank.getRankNo());
                vo.setOpenId(allProvinceRank.getOpenId());
                vo.setName(allProvinceRank.getName());
                vo.setAllAch(allProvinceRank.getAllAch());
                vo.setAllExp(allProvinceRank.getAllExp());
                vo.setBranchName(allProvinceRank.getBranchName());
                vo.setCompanyName(allProvinceRank.getCompanyName());
                vo.setHonorName(allProvinceRank.getHonorName());
                vo.setCompositeScore(allProvinceRank.getCompositeScore());
                allProvinceRankHotTaiLookVoList.add(vo);
            }
        }
        if (CollUtil.isNotEmpty(allProvinceRankHotTaiLookVoList)) {
            return ResultLayuiTable.newSuccess(allProvinceRankListPage.getTotal(), allProvinceRankHotTaiLookVoList);
        } else {
            return ResultLayuiTable.newFaild("全省个人排行列表页面数据为空");
        }

    }


    /**
     * 查询全省个人排行榜信息 (二期、小程序 )
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 查询全省个人排行榜数据集合
     */
    @Override
    @Transactional
    public Map<String, Object> getAllProvinceRankList(String openId, int page, int limit, Integer type, Integer cType) {
        Map<String,Object> map = new HashMap<String,Object>();
        List<AllProvinceRankVo> allProvinceRankVos = new ArrayList<>();
        if (1 == type) {
            // 全省个人排行榜（历史总排行）
            allProvinceRankVos = allProvinceRankMapper.getAllProvinceRankListForAll(cType);
        } else if (2 == type){
            // 全省个人排行榜（年内总排行）
            allProvinceRankVos = allProvinceRankMapper.getAllProvinceRankListForYear(cType);
        } else {
            throw new BusinessException("选择排行类型数据错误，只能为1/2");
        }

        // 参与排名名次
        Integer ranking = 1;
        // 个人排名数据是否在本次查询区间中
        Boolean isExit = false;
        // 数据封装
        if (CollUtil.isNotEmpty(allProvinceRankVos)) {
            // 查询页码不大于数据页，才进行数据遍历封装
            if (Math.ceil((double) allProvinceRankVos.size() / (double)limit) >= page) {
                Integer start = (page-1) * limit;
                Integer end = page * limit > allProvinceRankVos.size() ? allProvinceRankVos.size() : page * limit;
                List<AllProvinceRankQianDuanLookVo> allProvinceRankQianDuanLookVos = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    AllProvinceRankQianDuanLookVo allProvinceRankQianDuanLookVo = new AllProvinceRankQianDuanLookVo();
                    BeanUtil.copyProperties(allProvinceRankVos.get(i),allProvinceRankQianDuanLookVo);
                    allProvinceRankQianDuanLookVo.setRankNo(i + 1);
                    allProvinceRankQianDuanLookVo.setRankDate(new Date());
                    allProvinceRankQianDuanLookVo.setCreateTime(new Date());
                    // 完善称号id、称号名称（返回最新拥有那个）
                    // 查询用户称号集合
                    List<UserHonor> userHonors = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",allProvinceRankQianDuanLookVo.getOpenId()).orderByDesc("create_time"));
                    if (CollUtil.isNotEmpty(userHonors)) {
                        allProvinceRankQianDuanLookVo.setHonorNo(userHonors.get(0).getHonorId());
                        allProvinceRankQianDuanLookVo.setHonorName(userHonors.get(0).getHonorName());
                    } else {
                        allProvinceRankQianDuanLookVo.setHonorNo(0);
                        allProvinceRankQianDuanLookVo.setHonorName(null);
                    }
                    // 完善部门名称（如果有多个，返回id最小那个）
                    // 查询用户部门集合
                    List<UserBranch> userBranchs = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id",allProvinceRankQianDuanLookVo.getOpenId()).orderByAsc("id"));
                    if (CollUtil.isNotEmpty(userBranchs)) {
                        allProvinceRankQianDuanLookVo.setBranchName(userBranchs.get(0).getBranchName());
                    } else {
                        throw new BusinessException("用户相关所属部门不能为空！");
                    }
                    // 判断个人排名数据是否在本次查询区间中
                    if (!isExit && openId.equals(allProvinceRankQianDuanLookVo.getOpenId())) {
                        isExit = true;
                        ranking = i + 1;
                    }

                    allProvinceRankQianDuanLookVos.add(allProvinceRankQianDuanLookVo);
                }
                map.put("rankList",allProvinceRankQianDuanLookVos);
            } else {
                map.put("rankList",null);
            }
        }

        // 是否通过全面遍历查询到用户排名
        Boolean isSuccess = false;
        if (!isExit) {
            if (CollUtil.isNotEmpty(allProvinceRankVos)) {
                for (int i = 0; i < allProvinceRankVos.size(); i++) {
                    if (openId.equals(allProvinceRankVos.get(i).getOpenId())) {
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
