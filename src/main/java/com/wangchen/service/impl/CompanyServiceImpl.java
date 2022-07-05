package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.Company;
import com.wangchen.entity.User;
import com.wangchen.mapper.CompanyMapper;
import com.wangchen.service.CompanyService;
import com.wangchen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 公司表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserService userService;

    /**
     * 查询A类公司对应B/C类选定公司id
     * @param openId 用户id
     * @param cType A类公司对应需查看B/C类公司类型
     * @return
     */
    @Override
    @Transactional
    public Integer getAttachedCompanyByType(String openId, Integer cType) {
        User user = userService.getUserByOpenId(openId);
        if (null != user) {
            Company company = companyMapper.selectOne(new QueryWrapper<Company>().eq("id", user.getCompanyId()));
            Company newCompany = companyMapper.selectOne(new QueryWrapper<Company>().eq("name", company.getName()).eq("type", cType));
            return newCompany.getId();
        }

        return null;
    }
}

