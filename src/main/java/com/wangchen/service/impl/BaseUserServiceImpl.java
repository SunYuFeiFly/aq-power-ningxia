package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.*;
import com.wangchen.mapper.BaseUserMapper;
import com.wangchen.service.*;
import com.wangchen.utils.CompanyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */

@Slf4j
@Service
public class BaseUserServiceImpl extends ServiceImpl<BaseUserMapper, BaseUser> implements BaseUserService {

    @Autowired
    private BaseUserMapper baseUserMapper;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SysDeptService deptService;

    @Autowired
    private UserService userService;

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
    private BranchService branchService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;

    /**
     * 批量删除基本员工信息
     */
    @Override
    @Transactional
    public void deleteBaseUsers(int[] ids) {
        // 获取要删除基本用户集合
        List<BaseUser> baseUsers = baseUserMapper.selectList(new QueryWrapper<BaseUser>().in("id", ids));
        // 获取要删除基本用户对应电话号码集合
        List<String> phoneList = baseUsers.stream().map(BaseUser::getPhone).collect(Collectors.toList());
        ArrayList<User> users = new ArrayList<User>();
        if (phoneList != null && !phoneList.isEmpty()) {
            // 遍历获取对应电话号码游戏用户
            for (String phone : phoneList) {
                User user = userService.getOne(new QueryWrapper<User>().eq("mobile",phone));
                if (user != null) {
                    // 删除游戏相关信息
                    deleteUserRelevance(user);
                }
            }
        }
        // 批量删除选中基本用户信息
        baseUserMapper.deleteByIds(ids);
    }

    /**
     * 删除游戏相关信息
     */
    @Transactional(rollbackFor = {})
    public void deleteUserRelevance(User user) {
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
    }

    /**
     * 批量上传基本用户信息
     */
    @Override
    @Transactional
    public String inputBaseUsersFromExcel(MultipartFile file, Integer companyType) throws IOException, InvalidFormatException {
        // 获取输入源
        InputStream inputStream = file.getInputStream();
        // 记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        System.out.println("topicError:" + topicError.toString());
        // 生成工作表
        Workbook workbook = WorkbookFactory.create(inputStream);
        // 默认导入第1张表
        Sheet sheet = workbook.getSheetAt(0);
        // 获取表行数
        int rowEnd = sheet.getPhysicalNumberOfRows();
        // 判断导入数据大小，如果太大则不允许导入（避免表格数据错误大量导入）
        if (rowEnd >= 1000) {
            return "传入基本用户人员数量过大，请检查是否表格数据错误！";
        }
        String idcardReg = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
        String phoneReg = "^((13[0-9])|(14[0,1,4-9])|(15[0-3,5-9])|(16[2,5,6,7])|(17[0-8])|(18[0-9])|(19[0-3,5-9]))\\d{8}$";
        BaseUser baseUser01 = new BaseUser();
        // 获取导入内容（跳过表头）
        List<BaseUser> baseUserList =  new ArrayList<BaseUser>();
        for (int i = 1; i <= rowEnd; i++) {
            try {
                // 获取当前行数据
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = BaseUserServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    BaseUser baseUser = new BaseUser();
                    baseUser.setType(companyType);
                    Company company = null;
                    // 公司下部门名称集合
                    List<String> BranchNameList = new ArrayList<String>();
                    // 循环编列该行各列的值
                    for (int j = 0; j < currentRow.getLastCellNum(); j++) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (j == 0) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String idCard = data.getStringCellValue().trim();
                                // 校验身份证规范性
                                if (idCard.matches(idcardReg)) {
                                    baseUser01 = baseUserMapper.selectOne(new QueryWrapper<BaseUser>().eq("id_card",idCard));
                                    if (null != baseUser01) {
                                        topicError.append("   第"+ (i+1) +"行，第"+ (j+1) +"列【身份证号码】已注册");
                                    } else {
                                        baseUser.setIdCard(idCard);
                                    }
                                } else {
                                    topicError.append("   第"+ (i+1) +"行，第"+ (j+1) +"列【身份证号码】填写格式不规范");
                                }
                            } else {
                                topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【身份证号码】不能为空");
                            }
                        }
                        if (j == 1) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                baseUser.setName(data.getStringCellValue().trim());
                            } else {
                                topicError.append("   第"+ (i+1) +"行，第"+ (j+1) +"列【姓名】不能为空");
                            }
                        }
                        if (j == 2) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String phone = data.getStringCellValue().trim();
                                // 检验电话号码规范性
                                if (phone.matches(phoneReg)) {
                                    baseUser01 = baseUserMapper.selectOne(new QueryWrapper<BaseUser>().eq("phone",phone));
                                    if (null != baseUser01) {
                                        topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【手机号码】已注册");
                                    } else {
                                        baseUser.setPhone(data.getStringCellValue().trim());
                                    }
                                } else {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【手机号码】填写格式不规范");
                                }
                            } else {
                                topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【手机号码】不能为空");
                            }
                        }
                        if (j == 3) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String sex = data.getStringCellValue().trim();
                                if (!("男".equals(sex) || "女".equals(sex))) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【性别】只能为'男'或'女'");
                                }
                                baseUser.setSex("男".equals(data.getStringCellValue().trim()) ? 0 : 1);
                            } else {
                                topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【性别】不能为空");
                            }
                        }
                        if (j == 4) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String companyName = data.getStringCellValue().trim();
                                // 查询是否有此公司
                                company = companyService.getOne(new QueryWrapper<Company>().eq("name",companyName).eq("type",companyType));
                                if (null == company) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【公司】不存在");
                                } else {
                                    baseUser.setCompany(data.getStringCellValue().trim());
                                    if (1 == companyType) {
                                        List<Branch> branchList = branchService.list(new QueryWrapper<Branch>().eq("company_type", companyType));
                                        BranchNameList = branchList.stream().map(Branch :: getName).collect(Collectors.toList());
                                    } else {
                                        // 根据公司查下属部门
                                        String[] strings = Constants.companyBuMenKu.get(companyName);
                                        BranchNameList = Arrays.asList(strings);
                                    }
                                }
                            } else {
                                topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【公司】不能为空");
                            }
                        }
                        if (j == 5) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String branchOne = data.getStringCellValue().trim();
                                if (company == null) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j) +"列【公司】不存在情况下无法确定下属部门");
                                }
                                // 查询该公司下是否有该部门
                                Boolean isHasDept = false;
                                if (BranchNameList.size() > 0) {
                                    for (String branchName : BranchNameList) {
                                        if (branchOne.equals(branchName)) {
                                            isHasDept = true;
                                            break;
                                        }
                                    }
                                }
                                if (isHasDept) {
                                    baseUser.setBranchOne(data.getStringCellValue().trim());
                                } else {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门一】不是该公司下部门");
                                }
                            } else {
                                topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门一】不能为空");
                            }
                        }
                        if (j == 6) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String branchTwo = data.getStringCellValue().trim();
                                if (company == null) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j-1) +"列【公司】不存在情况下无法确定下属部门");
                                }
                                if (baseUser.getBranchOne() == null) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ j +"列【部门一】内容为空时不能先填写第"+ (j+1) +"列【部门二】内容");
                                }
                                if (baseUser.getBranchOne().equals(branchTwo)) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门二】内容不能与第"+ j +"列【部门一】内容相同");
                                }
                                // 查询该公司下是否有该部门
                                Boolean isHasDept = false;
                                if (BranchNameList.size() > 0) {
                                    for (String branchName : BranchNameList) {
                                        if (branchTwo.equals(branchName)) {
                                            isHasDept = true;
                                            break;
                                        }
                                    }
                                }
                                if (isHasDept) {
                                    baseUser.setBranchTwo(branchTwo);
                                } else {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门一】不是该公司下部门");
                                }
                            }
                        }
                        if (j == 7) {
                            if (data.getStringCellValue().trim() != null && !"".equals(data.getStringCellValue().trim())) {
                                String branchThree = data.getStringCellValue().trim();
                                if (company == null) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j-2) +"列【公司】不存在情况下无法确定下属部门");
                                }
                                if (baseUser.getBranchOne() == null || baseUser.getBranchTwo() == null) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j-1) +"列【部门一】或第"+ (j) +"列【部门二】内容为空时不能先填写第"+ (j+1) +"列【部门三】内容");
                                }
                                if (branchThree.equals(baseUser.getBranchOne()) || branchThree.equals(baseUser.getBranchTwo())) {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门三】内容不能与第"+ (j-1) +"列【部门一】或第"+ j +"列【部门二】内容相同");
                                }
                                // 查询该公司下是否有该部门
                                Boolean isHasDept = false;
                                if (BranchNameList.size() > 0) {
                                    for (String branchName : BranchNameList) {
                                        if (branchThree.equals(branchName)) {
                                            isHasDept = true;
                                            break;
                                        }
                                    }
                                }
                                if (isHasDept) {
                                    baseUser.setBranchThree(branchThree);
                                } else {
                                    topicError.append("    第"+ (i+1) +"行，第"+ (j+1) +"列【部门一】不是该公司下部门");
                                }
                            }
                        }
                    }

                    baseUserList.add(baseUser);
                } else {
                    break;
                }
            } catch (Exception e){
                log.error("第"+i+"道题目导入错误:{}",e);
                topicError.append("第" + (i+1) + "道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }

        if (topicError.length() > 0) {
            // 上传数据存在错误
            log.error("批量上传基本人员信息总体数据存在错误:{}",topicError.toString());
            throw new BusinessException("上传基本人员信息总体数据存在错误:" + topicError.toString());
        }

        try {
            this.saveBatch(baseUserList);
        } catch (Exception e) {
            log.error("批量上传基本人员信息出错，错误信息: {}",e);
            throw new BusinessException("批量上传基本人员信息出错:" + e.getMessage());
        }

        return "批量上传基本人员信息成功！";
    }


    /**
     * 判断读取行是否内容为空（存在格式设置）
     * @param row 行数据
     * @return 该行是否存在数据
     */
    public static boolean isRowEmpty(Row row){
        boolean isNull = true;
        // 只要行包含格式，则不为null，然后再根据内容判断该行是否为空
        if (null != row) {
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (!"".equals(cell.toString().trim())) {
                    isNull = false;
                    break;
                }
            }
        }

        return isNull;
    }


    /**
     * 新增修改员工数据 (二期)
     * @param info 基础人员数据
     */
    @Override
    @Transactional
    public Result editBaseUser(BaseUser info) {
        try {
            //新增
            if(null == info.getId() || 0 == info.getId()){
                List<BaseUser> baseUserList = this.list(new QueryWrapper<BaseUser>().eq("phone",info.getPhone()));
                if(CollectionUtils.isNotEmpty(baseUserList)){
                    return Result.newFail("该手机号已经存在");

                }
                List<BaseUser> baseUsers = this.list(new QueryWrapper<BaseUser>().eq("id_card",info.getIdCard()));
                if(CollectionUtils.isNotEmpty(baseUsers)){
                    return Result.newFail("该身份证号已经存在");

                }
                BaseUser baseUser = new BaseUser();
                baseUser.setIdCard(info.getIdCard());
                baseUser.setPhone(info.getPhone());
                baseUser.setName(info.getName());
                baseUser.setSex(info.getSex());
                baseUser.setCompany(info.getCompany());
                baseUser.setBranchOne(info.getBranchOne());
                if(StringUtils.isNotEmpty(info.getBranchTwo())){
                    baseUser.setBranchTwo(info.getBranchTwo());
                }
                if(StringUtils.isNotEmpty(info.getBranchThree())){
                    baseUser.setBranchThree(info.getBranchThree());
                }
                baseUser.setType(info.getType());
                this.save(baseUser);
            }else{
                //修改
                BaseUser baseUser = this.getById(info.getId());
                // 在已修改用户手机号情况下查手机号是否已注册
                if(!baseUser.getPhone().equals(info.getPhone())){
                    List<BaseUser> baseUserList = this.list(new QueryWrapper<BaseUser>().eq("phone",info.getPhone()));
                    if(CollectionUtils.isNotEmpty(baseUserList)){
                        return Result.newFail("该手机号已经存在");
                    }
                }
                // 在已修改用户身份证号情况下查手机号是否已注册 （按理说不能修改身份证号）
                if (!baseUser.getIdCard().equals(info.getIdCard())) {
                    List<BaseUser> baseUserList = this.list(new QueryWrapper<BaseUser>().eq("id_card",info.getIdCard()));
                    if(CollectionUtils.isNotEmpty(baseUserList)){
                        return Result.newFail("该身份证号已经存在");
                    }
                }
                // 更新关联游戏用户手机号
                User user = userService.getOne(new QueryWrapper<User>().eq("mobile",baseUser.getPhone()));
                if(null!= user){
                    user.setMobile(info.getPhone());
                    if(!baseUser.getCompany().equals(info.getCompany())){
                        user.setCompanyId(CompanyUtils.getCompanyMap(info.getCompany()));
                        user.setCompanyName(info.getCompany());
                    }
                    if(!baseUser.getName().equals(info.getName())){
                        user.setName(info.getName());
                    }
                    if(!baseUser.getIdCard().equals(info.getIdCard())){
                        user.setIdCard(info.getIdCard());
                    }
                    userService.updateById(user);
                    userBranchService.remove(new QueryWrapper<UserBranch>().eq("open_id",user.getOpenId()));
                    //部门1  部门1永远会有的
                    UserBranch userBranch = new UserBranch();
                    userBranch.setOpenId(user.getOpenId());
                    userBranch.setBranchId(CompanyUtils.getBranchMap(info.getBranchOne()));
                    userBranch.setBranchName(info.getBranchOne());
                    userBranch.setCreateDate(new Date());
                    userBranchService.save(userBranch);
                    //部门2 可选是否拥有部门2
                    if(StringUtils.isNotEmpty(info.getBranchTwo())){
                        UserBranch userBranch2 = new UserBranch();
                        userBranch2.setOpenId(user.getOpenId());
                        userBranch2.setBranchId(CompanyUtils.getBranchMap(info.getBranchTwo()));
                        userBranch2.setBranchName(info.getBranchTwo());
                        userBranch2.setCreateDate(new Date());
                        userBranchService.save(userBranch2);
                    }
                    //部门3 可选是否拥有部门3
                    if(StringUtils.isNotEmpty(info.getBranchThree())){
                        UserBranch userBranch3 = new UserBranch();
                        userBranch3.setOpenId(user.getOpenId());
                        userBranch3.setBranchId(CompanyUtils.getBranchMap(info.getBranchThree()));
                        userBranch3.setBranchName(info.getBranchThree());
                        userBranch3.setCreateDate(new Date());
                        userBranchService.save(userBranch3);
                    }
                }
                baseUser.setCompany(info.getCompany());
                baseUser.setBranchOne(info.getBranchOne());
                baseUser.setBranchTwo(info.getBranchTwo());
                baseUser.setBranchThree(info.getBranchThree());
                baseUser.setIdCard(info.getIdCard());
                baseUser.setName(info.getName());
                baseUser.setPhone(info.getPhone());
                this.updateById(baseUser);
            }
        } catch (Exception e) {
            log.error("新增修改员工数出错，错误信息: {}",e);
            return Result.newFail(null == info.getId() ? "新增基本员工数据出错" : "修改基本员工数据出错");
        }

        return Result.newSuccess(null == info.getId() ? "新增基本员工数据成功" : "修改基本员工数据成功");
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
    @Override
    public Result selectPages(String name, String companyName, Integer page, Integer limit, Integer companyType) {
        IPage<BaseUser> baseUserIPage = this.page(new Page<>(page,limit),new QueryWrapper<BaseUser>()
                .eq("type",companyType).like(!StringUtils.isEmpty(name),"name",name).like(!StringUtils.isEmpty(companyName),"company",companyName).orderByDesc("id"));
        return ResultLayuiTable.newSuccess(baseUserIPage.getTotal(), baseUserIPage.getRecords());
    }

}
