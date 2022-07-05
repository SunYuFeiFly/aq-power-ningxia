package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.CompanyRank;
import com.wangchen.entity.User;
import com.wangchen.mapper.CompanyRankMapper;
import com.wangchen.service.CompanyRankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.service.UserService;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.CompanyRankVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 所有公司的排行榜 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-23
 */
@Slf4j
@Service
public class CompanyRankServiceImpl extends ServiceImpl<CompanyRankMapper, CompanyRank> implements CompanyRankService {

    @Autowired
    private CompanyRankMapper companyRankMapper;

    @Autowired
    private UserService userService;

    /**
     * 公司排行榜 (二期 后台)
     * @param page 页码
     * @param limit 每页数据量
     * @param time 排行时间（如2021-08-25）
     * @param companyType 所属公司类型： 1:自有公司，2代维公司，3设计和监理公司
     * @param type 排行类型：1：历史排行  2：年内排行
     * @return 公司排行榜数据集合
     */
    @Override
    @Transactional
    public Result selectPages(int page, int limit, String time, Integer companyType, Integer type) {
        IPage<CompanyRank> companyRankListPage = companyRankMapper.selectPage(new Page<CompanyRank>(page, limit),new QueryWrapper<CompanyRank>()
                .eq("rank_date", time).eq("company_type",companyType).eq("type",type).orderByAsc("id"));

        if (CollUtil.isNotEmpty(companyRankListPage.getRecords())) {
            return ResultLayuiTable.newSuccess(companyRankListPage.getTotal(), companyRankListPage.getRecords());
        } else {
            return ResultLayuiTable.newFaild("公司排行榜数据为空");
        }
    }


    /**
     * 查询公司排行榜信息 (二期，小程序)
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 公司排行榜数据集合
     */
    @Transactional
    @Override
    public Map<String,Object> getCompanyRankList(String openId, int page, int limit, Integer type, Integer cType) throws ParseException {
        Map<String,Object> map = new LinkedHashMap<>();
        ArrayList<CompanyRankVo> companyRankVos = new ArrayList<>();
        if (1 == type) {
            // 全省个人排行榜（历史总排行）
            companyRankVos = companyRankMapper.getCompanyRankListForAll(cType);
        } else if (2 == type){
            // 全省个人排行榜（年内总排行）
            companyRankVos = companyRankMapper.getCompanyRankListForYear(cType);
        } else {
            throw new BusinessException("选择排行类型数据错误，只能为1/2");
        }

        if (CollUtil.isNotEmpty(companyRankVos)) {
            ArrayList<CompanyRank> companyRankList = new ArrayList<>();
            for (int i = 0; i < companyRankVos.size(); i++) {
                // 查询公司下游戏用户
                List<User> users = userService.list(new QueryWrapper<User>().eq("company_id", companyRankVos.get(i).getCompanyId()));
                CompanyRank companyRank = new CompanyRank();
                companyRank.setCompanyId(companyRankVos.get(i).getCompanyId());
                companyRank.setCompanyName(companyRankVos.get(i).getCompanyName());
                companyRank.setRankDate(new Date());
                companyRank.setCount(users.size());
                if (0 != users.size()&&null!= companyRankVos.get(i).getCompositeScore()) {
                    companyRank.setCompositeScore(companyRankVos.get(i).getCompositeScore()/users.size());
                } else {
                    companyRank.setCompositeScore(0.0);
                }
                companyRank.setRankNo(i + 1);
                companyRank.setCreateTime(new Date());
                companyRank.setCompanyType(cType);
                companyRank.setType(1);

                companyRankList.add(companyRank);
            }
            // 对公司排名
            List<CompanyRank> newCompanyRankVos = companyRankList.stream().sorted(Comparator.comparing(CompanyRank::getCompositeScore)).collect(Collectors.toList());
            // 重新设置排名
            for (int j = 1; j <= newCompanyRankVos.size(); j++) {
                newCompanyRankVos.get(j-1).setRankNo(newCompanyRankVos.size()-j+1);
            }
            Collections.reverse(newCompanyRankVos);
            // 根据查询人员所属公司分类不同，封装不同的返回数据
            map.put("rankList",newCompanyRankVos);
            User user = userService.getUserByOpenId(openId);
            if (!cType.equals(user.getType())) {
                map.put("userRankNo", "未参与");
            }else{
                //查询所在公司的当前排名
                map.put("userRankNo", "未上榜");
                for(CompanyRank item:newCompanyRankVos){
                    if(user.getCompanyId().equals(item.getCompanyId())){
                        map.put("userRankNo", ""+item.getRankNo());
                    }
                }
            }
            return map;
        } else {
            // 没有公司排名数据，个人也就不存在参与排名
            return null;
        }

    }
}
