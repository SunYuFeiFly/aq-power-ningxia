package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wangchen.common.Result;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.Branch;
import com.wangchen.entity.Company;
import com.wangchen.entity.User;
import com.wangchen.entity.UserBranch;
import com.wangchen.mapper.UserBranchMapper;
import com.wangchen.service.BranchService;
import com.wangchen.service.CompanyService;
import com.wangchen.service.UserBranchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 用户部门表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class UserBranchServiceImpl extends ServiceImpl<UserBranchMapper, UserBranch> implements UserBranchService {

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private BranchService branchService;


    /**
     * 修改游戏用户信息 （二期）
     * @param openId 用户id
     * @param branch1 所属部门一
     * @param branch2 所属部门二
     * @param branch3 所属部门三
     * @param type 用户所属公司类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @return 修改成功与否
     */
    @Override
    @Transactional
    public Result editGameUser(String openId, Integer branch1, Integer branch2, Integer branch3, Integer type) {
        // 先移除用户-部门表数据
        userBranchService.remove(new QueryWrapper<UserBranch>().eq("open_id",openId));
        // 部门一二三重新赋值
        Set branchSet = new HashSet<Integer>();
        if (null != branch1 && branch1 != 0) {
            branchSet.add(branch1);
        }
        if (null != branch2 && branch2 != 0) {
            branchSet.add(branch2);
        }
        if (null != branch3 && branch3 != 0) {
            branchSet.add(branch3);
        }
        Iterator iterator = branchSet.iterator();
        if (branchSet.size() == 0) {
            return Result.newFaild("修改员工部门至少需要勾选一个部门！");
        }
        int index = 1;
        while (iterator.hasNext()) {
            if (1 == index) {
                branch1 = (Integer) iterator.next();
                branch2 = null;
                branch3 = null;
            }
            if (2 == index) {
                branch2 = (Integer) iterator.next();
            }
            if (3 == index) {
                branch3 = (Integer) iterator.next();
            }
            index ++;
        }

        if (null != branch1) {
            UserBranch userBranch = new UserBranch();
            userBranch.setOpenId(openId);
            userBranch.setBranchId(branch1.intValue());
            Branch branch = branchService.getById(branch1.intValue());
            userBranch.setBranchName(branch.getName());
            userBranch.setCreateDate(new Date());

            userBranchService.save(userBranch);
        }
        if (null != branch2) {
            UserBranch userBranch = new UserBranch();
            userBranch.setOpenId(openId);
            userBranch.setBranchId(branch2.intValue());
            Branch branch = branchService.getById(branch2.intValue());
            userBranch.setBranchName(branch.getName());
            userBranch.setCreateDate(new Date());

            userBranchService.save(userBranch);
        }
        if (null != branch3) {
            UserBranch userBranch = new UserBranch();
            userBranch.setOpenId(openId);
            userBranch.setBranchId(branch3.intValue());
            Branch branch = branchService.getById(branch3.intValue());
            userBranch.setBranchName(branch.getName());
            userBranch.setCreateDate(new Date());

            userBranchService.save(userBranch);
        }

        return Result.newSuccess("修改游戏用户信息成功");
    }


    /**
     * 检测更改游戏用户所选的部门id是否合法，主要预防直接用其他软件的url请求
     * @param type 用户所属公司类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @param branch 部门id
     * @return 部门id是否合法
     */
    private Boolean verificationRationality(Integer type, Integer branch) {
        // 获取所属公司类型下公司数据集合
        List<Company> Companies = companyService.list(new QueryWrapper<Company>().eq("type",type));
        Boolean isFound = false;
        if (CollUtil.isNotEmpty(Companies)) {
            for (Company company : Companies) {
                if (branch == company.getId()) {
                    isFound = true;
                    break;
                }
            }
        }

        return isFound;
    }
}
