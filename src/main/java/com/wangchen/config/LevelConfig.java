package com.wangchen.config;

import com.wangchen.entity.Branch;
import com.wangchen.entity.Company;
import com.wangchen.entity.Level;
import com.wangchen.entity.UserLevel;
import com.wangchen.service.BranchService;
import com.wangchen.service.CompanyService;
import com.wangchen.service.LevelService;
import com.wangchen.utils.CompanyUtils;
import com.wangchen.utils.UserLevelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.config
 * @ClassName: LevelConfig
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/10 16:45
 * @Version: 1.0
 */
@Component
public class LevelConfig implements ApplicationRunner {

    @Autowired
    private LevelService levelService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private BranchService branchService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Level> levelList = levelService.list();
        UserLevelUtils.setLevelMap(levelList);

        List<Company> companyList = companyService.list();
        CompanyUtils.setCompanyMap(companyList);

        List<Branch> branchList = branchService.list();
        CompanyUtils.setBranchMap(branchList);
    }
}