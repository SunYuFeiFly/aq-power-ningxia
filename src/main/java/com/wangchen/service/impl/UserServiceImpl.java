package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.mapper.UserDayGameLogMapper;
import com.wangchen.mapper.UserMapper;
import com.wangchen.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.utils.StringUtils;
import com.wangchen.vo.CompanyUserInfoDetailVo;
import com.wangchen.vo.ExportCompanyCsvVo;
import com.wangchen.vo.ExportUserGameCsvVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SevenSignService sevenSignService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;

    @Autowired
    private ExperienceService experienceService;


    /**
     * 根据openId获取用户
     * @param openId 用户id
     * @return 用户
     */
    @Override
    public User getUserByOpenId(String openId){
        return userMapper.selectOne(new QueryWrapper<User>().eq("open_id",openId).last("limit 1"));
    }


    /**
     * 更新游戏用户截至年底所拥有总积分（定时任务）
     */
    @Override
    @Transactional
    public Result updateLastYearExperience(List<User> userList) {
        if (null != userList && !userList.isEmpty()) {
            for (User user : userList) {
                user.setLastYearExperience(user.getAllExperience());
                user.setPresentExperience(0);
            }
        } else {
            return Result.newFaild("没有游戏用户年度积分更新！");
        }
        // 批量更新用户年度积分
        try {
            this.updateBatchById(userList);
        } catch (Exception e) {
            log.info("批量更新用户年度积分异常！");
            e.printStackTrace();
        }
        return Result.newSuccess();
    }


    /**
     * 员工管理 - 公司员工列表数据（二期 后台 优化）
     * @param name 公司名称
     * @param time 查询时间
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 查询所属分类公司
     * @return 公司活跃数据集合
     */
    @Override
    @Transactional
    public Result selectPages(String name, String time, Integer page, Integer limit, Integer companyType) {
        String today = Constants.SDF_YYYY_MM_DD.format(new Date());
        if(StringUtils.isEmpty(time)){
            time = today;
        }
        String startTime = time + " 00:00:00";
        String endTime = time + " 23:59:59";
        // 公司活跃数据集合
        List<ExportCompanyCsvVo> exportCompanyCsvVoList = new ArrayList<ExportCompanyCsvVo>();
        Map<String, ExportCompanyCsvVo> companyCsvVoHashMap = new HashMap<>();

        // 公司对象集合
        List<Company> companies = new ArrayList<>();
        IPage<Company> companyIPage = companyService.page(new Page<>(page, limit), new QueryWrapper<Company>().eq("type", companyType).eq(!StringUtils.isEmpty(name), "name", name));
        if (CollUtil.isNotEmpty(companyIPage.getRecords())) {
            companies = companyIPage.getRecords();
        } else {
            return null;
        }
        // 创建一个数值格式化对象 （用于计算后面各项参与率）
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        // 获取公司名称集合
        List<String> companyNameList = companies.stream().map(Company::getName).collect(Collectors.toList());
        // 获取对应公司id下的所有基本用户集合
        List<BaseUser> baseUserList = baseUserService.list(new QueryWrapper<BaseUser>().in("company", companyNameList));
        // 根据公司名称对基本用户集合进行分类
        Map<String, List<BaseUser>> baseUserMap = baseUserList.stream().collect(Collectors.groupingBy(BaseUser::getCompany));
        // 获取对应公司id下的所有游戏用户集合
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("deleted", 0).in("company_name", companyNameList).lt("registered_time", time + " 23:59:59").isNotNull("mobile"));
        // 根据公司名称对游戏员工集合进行分类
        Map<String, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getCompanyName));

        Iterator<String> baseUserIterator = baseUserMap.keySet().iterator();
        while (baseUserIterator.hasNext()) {
            String index = baseUserIterator.next();
            List<BaseUser> baseUsers = baseUserMap.get(index);
            ExportCompanyCsvVo exportCompanyCsvVo = new ExportCompanyCsvVo();
            // 设置该公司下基本用户个数
            exportCompanyCsvVo.setCompanyName(index);
            exportCompanyCsvVo.setCountNum(baseUsers.size());
            companyCsvVoHashMap.put(index,exportCompanyCsvVo);
        }

        Iterator<String> userIterator = userMap.keySet().iterator();
        while (userIterator.hasNext()) {
            String index = userIterator.next();
            List<User> users = userMap.get(index);
            ExportCompanyCsvVo exportCompanyCsvVo = companyCsvVoHashMap.get(index);
            exportCompanyCsvVo.setId(users.get(0).getCompanyId());
            if (null != exportCompanyCsvVo) {
                // 注册人数
                Integer registerNum = users.size();
                // 总人数
                Integer countNum = exportCompanyCsvVo.getCountNum();
                // 获取用户openId集合
                List<String> openIdList = users.stream().map(User::getOpenId).collect(Collectors.toList());
                if (0 == registerNum) {
                    continue;
                }
                // 塔币
                Integer allCoinNum = users.stream().mapToInt(User :: getAllCoin).sum();
                // 成就值
                Integer allAchievementNum = users.stream().mapToInt(User :: getAllAchievement).sum();
                // 年度经验值
                Integer presentExperienceNum = users.stream().mapToInt(User :: getPresentExperience).sum();
                Integer allExperienceNum = 0;
                if (time.equals(today)) {
                    // 如果选择的时间是当天时间，则总经验从user表获取
                    allExperienceNum = users.stream().mapToInt(User :: getAllExperience).sum();
                } else {
                    // 如果选择的时间不是当天时间，则总经验从experience表获取
                    // 获取所选时间日期
                    int day = DateUtil.dayOfMonth(DateUtil.parse(time));
                    // 获取所选时间月份
                    int month = DateUtil.month(DateUtil.parse(time)) + 1;
                    // 获取所选时间年份
                    int year = DateUtil.year(DateUtil.parse(time));
                    // 获取所属年月下openid对应的用户经验集合
                    List<Experience> experienceList = experienceService.list(new QueryWrapper<Experience>().eq("part_month", month).eq("part_year", year).in("open_id", openIdList));
                    // 总经验值
                    allExperienceNum = experienceList.stream().mapToInt(e -> {return Integer.parseInt(e.getDayExperience().split(",")[day-1]);}).sum();
                }

                // 获取当前时间用户七天登录集合
                List<SevenSign> sevenSignList = sevenSignService.list(new QueryWrapper<SevenSign>().eq("sign_date", time).in("open_id", openIdList));
                // 签到人数
                Integer allSignNum = sevenSignList.size();
                // 签到率（%）
                String allSignRate = "";
                if (!CollUtil.isNotEmpty(sevenSignList)) {
                    allSignRate = "0%";
                } else {
                    allSignRate = numberFormat.format((float)sevenSignList.size() / (float)countNum * 100) + "%";
                }

                // 获取当前时间用户每日答题记录集合
                List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdList));
                // 当前时间用户总共答过多少题
                int allDayGameCount = userDayGameLogList.size() * 10;
                // 当前时间用户总共答对多少题
                int allDayGameTrueCount = userDayGameLogList.stream().mapToInt(UserDayGameLog :: getScore).sum() / 10;
                // 每日答题正确率
                String dayGameTrueRate = "";
                if(0 != allDayGameTrueCount){
                    dayGameTrueRate = numberFormat.format((float)allDayGameTrueCount / (float)allDayGameCount * 100) + "%";
                }else{
                    dayGameTrueRate = "0%";
                }
                // 根据openId对每日答题记录去重（每个人每日可多次参与每日答题）
                userDayGameLogList = userDayGameLogList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<UserDayGameLog>(Comparator.comparing(UserDayGameLog::getOpenId))), ArrayList::new));
                // 每日答题参与度（%）
                String dayGameRate = "";
                if (!CollUtil.isNotEmpty(userDayGameLogList)) {
                    dayGameRate = "0%";
                } else {
                    dayGameRate = numberFormat.format((float)userDayGameLogList.size() / (float)countNum * 100) + "%";
                }

                // 获取当前时间用户个人赛记录集合
                QueryWrapper<UserOneVsOneLog> queryWrapper = new QueryWrapper<>();
                List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.list(queryWrapper.gt("create_time", startTime).lt("create_time", endTime).and(wrapper -> wrapper.in("room_open_id", openIdList).or().in("friend_open_id", openIdList)));
                List<String> openIds01 = userOneVsOneLogList.stream().map(UserOneVsOneLog::getRoomOpenId).collect(Collectors.toList());
                List<String> openIds02 = userOneVsOneLogList.stream().map(UserOneVsOneLog::getFriendOpenId).collect(Collectors.toList());
                HashSet<String> openIdSet = new HashSet<>();
                openIdSet.addAll(openIds01);
                openIdSet.addAll(openIds02);
                // 个人赛参与度
                String oneVsOneRate = "";
                if (!CollUtil.isNotEmpty(userOneVsOneLogList)) {
                    oneVsOneRate = "0%";
                } else {
                    oneVsOneRate = numberFormat.format((float)openIdSet.size() / (float)countNum * 100) + "%";
                }

                // 团队赛数据库更新表单
                // 获取当前时间用户团队赛记录集合(每人每天只能参加一次)
                List<UserTeamVsTeamLog> userTeamVsTeamLogList = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>().ge("create_time", startTime).le("create_time", endTime).in("open_id", openIdList));
                // 团队赛参与度
                String teamVsTeamRate = "";
                if (!CollUtil.isNotEmpty(userTeamVsTeamLogList)) {
                    teamVsTeamRate = "0%";
                } else {
                    teamVsTeamRate = numberFormat.format((float)userTeamVsTeamLogList.size() / (float)countNum * 100) + "%";
                }

                // 整体赋值
                exportCompanyCsvVo.setRegisterNum(registerNum);
                exportCompanyCsvVo.setAllCoinNum(allCoinNum);
                exportCompanyCsvVo.setAllAchievementNum(allAchievementNum);
                exportCompanyCsvVo.setAllExperienceNum(allExperienceNum);
                exportCompanyCsvVo.setPresentExperienceNum(presentExperienceNum);
                exportCompanyCsvVo.setAllSignNum(allSignNum);
                exportCompanyCsvVo.setAllSignRate(allSignRate);
                exportCompanyCsvVo.setDayGameRate(dayGameRate);
                exportCompanyCsvVo.setDayGameTrueRate(dayGameTrueRate);
                exportCompanyCsvVo.setOneVsOneRate(oneVsOneRate);
                exportCompanyCsvVo.setTeamVsTeamRate(teamVsTeamRate);

                companyCsvVoHashMap.put(index,exportCompanyCsvVo);
            }
        }

        exportCompanyCsvVoList = companyCsvVoHashMap.values().stream().collect(Collectors.toList());
        exportCompanyCsvVoList = exportCompanyCsvVoList.stream().sorted(Comparator.comparing(ExportCompanyCsvVo::getId)).collect(Collectors.toList());

        return ResultLayuiTable.newSuccess(companyIPage.getTotal(), exportCompanyCsvVoList);
    }


    /**
     * 员工管理 - 公司员工列表数据（二期 后台 优化，此方法暂留）
     * @param name 公司名称
     * @param time 查询时间
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 查询所属分类公司
     * @return 公司活跃数据集合
     */
    // @Override
    @Transactional
    public Result selectPages02(String name, String time, Integer page, Integer limit, Integer companyType) {
        // 公司活跃数据集合
        List<ExportCompanyCsvVo> exportCompanyCsvVoList = new ArrayList<ExportCompanyCsvVo>();
        // 公司对象集合
        List<Company> companies = new ArrayList<>();
        IPage<Company> companyIPage = null;
        try {
            if(StringUtils.isEmpty(time)){
                time = Constants.SDF_YYYY_MM_DD.format(new Date());
            }
            companyIPage = companyService.page(new Page<>(page,limit),new QueryWrapper<Company>().eq("type",companyType).eq(!StringUtils.isEmpty(name),"name",name));
            if (CollUtil.isNotEmpty(companyIPage.getRecords())) {
                companies = companyIPage.getRecords();
            } else {
                return null;
            }
            // 创建一个数值格式化对象 （用于计算后面各项参与率）
            NumberFormat numberFormat = NumberFormat.getInstance();
            // 设置精确到小数点后2位
            numberFormat.setMaximumFractionDigits(2);

            // 遍历封装数据
            for(Company company : companies){
                // 查询该公司下基本员工列表
                List<BaseUser> baseUsers = baseUserService.list(new QueryWrapper<BaseUser>().eq("company",company.getName()));
                // 公司下员工拥有手机号码信息集合（利用手机号将基本用户与游戏用户联系起来）
                List<String> phoneList = new ArrayList<String>();
                for(BaseUser baseUser : baseUsers){
                    phoneList.add(baseUser.getPhone());
                }
                if (CollUtil.isNotEmpty(phoneList)) {

                }else{
                    continue;
                }
                // 查询在天最后一秒之前注册的游戏用户集合
                List<User> userList = userMapper.selectList(new QueryWrapper<User>().in("mobile",phoneList).lt("registered_time",time +" 23:59:59"));
                int allCoin = 0; //总塔币
                int allAchievement = 0; //总成就
                int allExperience = 0; //总经验
                int allSign = 0; //公司里有所有用户签到次数
                int dayGameCanYu = 0; //时间段内公司员工 每日参与过的总人数
                int allDayGameCount = 0; //时间段内公司员工 每日总共答过多少题
                int allDayGameTrueCount = 0; //时间段内公司员工 每日总归答对多少题
                int oneVsOneNum = 0; //时间段内 用户每日是否参与个人赛总数
                int threeVsThreeNum = 0; //时间段内 用户每日是否参与团队赛总数
                // 遍历统计每个用户在选定时间内塔币、成就、总经验、签到数、答题数、答对题数、是否参与每日答题、个人赛、团队赛数据，方便统计总体用户数据
                for(User user : userList){
                    // 总塔币
                    allCoin += user.getAllCoin();
                    // 总成就
                    allAchievement += user.getAllAchievement();
                    // 总经验
                    allExperience += user.getAllExperience();
                    // 查询当天是否签到
                    List<SevenSign> sevenSignList = sevenSignService.list(new QueryWrapper<SevenSign>().eq("open_id",user.getOpenId()).eq(StrUtil.isNotBlank(time),"sign_date",time));
                    // 总签到数
                    allSign += sevenSignList.size();
                    // 计算每日答题（可以一天参加多次）
                    List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().eq("open_id",user.getOpenId()).eq("day_game_date",time));
                    // 参与次数统计
                    if(CollUtil.isNotEmpty(userDayGameLogList)){
                        dayGameCanYu += 1;
                    }
                    // 计算每日答题总答题数、答对总数
                    for(UserDayGameLog userDayGameLog : userDayGameLogList){
                        // 每局10题
                        allDayGameCount += 10;
                        // 每局满分100分
                        allDayGameTrueCount += userDayGameLog.getScore() / 10;
                    }
                    // 个人赛
                    List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.getGameLogByOpenId(time,user.getOpenId());
                    if(CollUtil.isNotEmpty(userOneVsOneLogList)){
                        oneVsOneNum += 1;
                    }
                    // 团队赛
                    List<UserThreeTeamLog> userThreeTeamLogList = userThreeTeamLogService.list(new QueryWrapper<UserThreeTeamLog>().eq("open_id",user.getOpenId()).eq("create_date",time));
                    if(CollUtil.isNotEmpty(userThreeTeamLogList)){
                        threeVsThreeNum += 1;
                    }
                }

                ExportCompanyCsvVo exportCompanyCsvVo = new ExportCompanyCsvVo();
                exportCompanyCsvVo.setId(company.getId());
                exportCompanyCsvVo.setCompanyName(company.getName());
                exportCompanyCsvVo.setCountNum(baseUsers.size());
                exportCompanyCsvVo.setRegisterNum(userList.size());
                exportCompanyCsvVo.setAllCoinNum(allCoin);
                exportCompanyCsvVo.setAllAchievementNum(allAchievement);
                exportCompanyCsvVo.setAllExperienceNum(allExperience);
                exportCompanyCsvVo.setAllSignNum(allSign);
                // 计算签到率（已注册游戏用户的签到率）
                if(0 != allSign){
                    exportCompanyCsvVo.setAllSignRate(numberFormat.format((float)allSign/(float)exportCompanyCsvVo.getRegisterNum().intValue() * 100) + "%");
                }else{
                    exportCompanyCsvVo.setAllSignRate("0%");
                }
                // 计算每日答题参与度
                if(0 != dayGameCanYu){
                    exportCompanyCsvVo.setDayGameRate(numberFormat.format((float)dayGameCanYu/(float)exportCompanyCsvVo.getRegisterNum() * 100) + "%");
                }else{
                    exportCompanyCsvVo.setDayGameRate("0%");
                }
                // 计算每日答题正确率
                if(0 != allDayGameTrueCount){
                    exportCompanyCsvVo.setDayGameTrueRate(numberFormat.format((float)allDayGameTrueCount/(float)allDayGameCount * 100) + "%");
                }else{
                    exportCompanyCsvVo.setDayGameTrueRate("0%");
                }
                // 计算个人赛参与度
                if(0 != oneVsOneNum){
                    exportCompanyCsvVo.setOneVsOneRate(numberFormat.format((float)oneVsOneNum/(float)exportCompanyCsvVo.getRegisterNum() * 100) + "%");
                }else{
                    exportCompanyCsvVo.setOneVsOneRate("0%");
                }
                // 计算团队赛参与度
                if(0 != threeVsThreeNum){
                    exportCompanyCsvVo.setTeamVsTeamRate(
                            numberFormat.format((float)threeVsThreeNum/(float)exportCompanyCsvVo.getRegisterNum() * 100) + "%");
                }else{
                    exportCompanyCsvVo.setTeamVsTeamRate("0%");
                }

                exportCompanyCsvVoList.add(exportCompanyCsvVo);
            }

        }catch (Exception e){
            log.error("查询员工列表错误：{}",e);
        }

        return ResultLayuiTable.newSuccess(companyIPage.getTotal(), exportCompanyCsvVoList);
    }


    /**
     * 员工个人信息详情页面数据 （二期，后台管理）
     * @param id 公司id
     * @param name 员工姓名
     * @param time 时间（2021-10-12）
     * @param page 页码
     * @param limit 每页数据量
     * @return 特定时间公司员工数据集合
     */
    @Override
    @Transactional
    public Result selectUserList(Long id, int page, int limit, String name, String time) {
        String today = Constants.SDF_YYYY_MM_DD.format(new Date());
        // 公司活跃数据集合
        List<ExportUserGameCsvVo> exportUserGameCsvVoList =  new ArrayList<ExportUserGameCsvVo>();
        // 创建一个数值格式化对象（用于计算员工项参与率）
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        if(StringUtils.isEmpty(time)){
            time = Constants.SDF_YYYY_MM_DD.format(new Date());
        }
        // 获取公司下员工数据
        String endDayTime = time + " 23:59:59";
        IPage<User> userIPage = userMapper.selectPage(new Page<>(page,limit),new QueryWrapper<User>().eq("company_id",id).lt("registered_time",endDayTime).eq(!StringUtils.isEmpty(name),"name",name).eq("deleted",0));
        List<User> userList = userIPage.getRecords();
        // 获取所选时间日期
        int day = DateUtil.dayOfMonth(DateUtil.parse(time));
        Map<String, List<Experience>> experiencMap = new HashMap<>();
        if (!today.equals(time)) {
            // 获取所选时间月份
            int month = DateUtil.month(DateUtil.parse(time)) + 1;
            // 获取所选时间年份
            int year = DateUtil.year(DateUtil.parse(time));
            // 获取用户openId合集
            List<String> openIdlist = userList.stream().map(User::getOpenId).collect(Collectors.toList());
            // 不是查询当天员工个人信息，应从experience表获取相应经验数据
            List<Experience> experienceList = experienceService.list(new QueryWrapper<Experience>().eq("part_month", month).eq("part_year", year).in("open_id", openIdlist));
            experiencMap = experienceList.stream().collect(Collectors.groupingBy(Experience::getOpenId));
        }
        // 遍历统计每个用户在选定时间内塔币、成就、总经验、签到数、答题数、答对题数、是否参与每日答题、个人赛、团队赛数据，方便统计总体用户数据
        if (CollUtil.isNotEmpty(userList)) {
            // 获取用户openId合集
            List<String> openIdlist = userList.stream().map(User::getOpenId).collect(Collectors.toList());
            // 获取用户签到集合
            List<SevenSign> sevenSignList = sevenSignService.list(new QueryWrapper<SevenSign>().in("open_id", openIdlist).eq(StrUtil.isNotBlank(time),"sign_date",time));
            Map<String, List<SevenSign>> sevenSignMap = sevenSignList.stream().collect(Collectors.groupingBy(SevenSign::getOpenId));

            // 获取每日答题数据集合
            List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().in("open_id", openIdlist).eq("day_game_date", time));
            Map<String, List<UserDayGameLog>> userDayGameLogMap = userDayGameLogList.stream().collect(Collectors.groupingBy(UserDayGameLog::getOpenId));

            // 获取团队赛数据集合
            List<UserTeamVsTeamLog> userTeamVsTeamLog = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>().in("open_id", openIdlist).eq("create_date", time));
            Map<String, List<UserTeamVsTeamLog>> userTeamVsTeamLogMap = userTeamVsTeamLog.stream().collect(Collectors.groupingBy(UserTeamVsTeamLog::getOpenId));

            for(User user : userList) {
                String openId = user.getOpenId();
                // 经验
                int experienceNum = 0;
                if (today.equals(time)) {
                    experienceNum = user.getAllExperience();
                } else {
                    experienceNum = Integer.parseInt(experiencMap.get(openId).get(0).getDayExperience().split(",")[day - 1]);
                }
                // 塔币
                int coinNum = user.getAllCoin();
                // 成就
                int achievementNum = user.getAllAchievement();
                // 签到数
                int signNum = sevenSignMap.containsKey(openId) ? sevenSignMap.get(openId).size() : 0;

                // 计算每日答题总次数
                int dayGameNum = 0;
                // 每日答题正确题数
                int dayGameTrueNum = 0;
                // 每日答题总正确率
                String dayGameTrueRate = "0%";
                if (userDayGameLogMap.containsKey(openId)) {
                    dayGameNum = userDayGameLogMap.get(openId).size();
                    dayGameTrueNum = userDayGameLogMap.get(openId).stream().mapToInt(UserDayGameLog :: getScore).sum() / 10;
                    if (dayGameNum !=0 && dayGameTrueNum != 0) {
                        dayGameTrueRate = numberFormat.format((float) dayGameTrueNum / (float) (dayGameNum * 10) * 100) + "%";
                    }
                }

                // 公司下员工特定时间个人赛数据集合
                List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.getGameLogByOpenId(time, openId);
                // 个人赛总次数 (个人赛能参加多次)
                int oneVsOneNum = userOneVsOneLogList.size();
                // 个人赛正确题数
                int oneVsOneTrueNum = 0;
                for (UserOneVsOneLog userOneVsOneLog : userOneVsOneLogList) {
                    if (user.getOpenId().equals(userOneVsOneLog.getFriendOpenId())) {
                        oneVsOneTrueNum += (userOneVsOneLog.getFriendOpenScore() / 10);
                    }
                    if (user.getOpenId().equals(userOneVsOneLog.getRoomOpenId())) {
                        oneVsOneTrueNum += (userOneVsOneLog.getRoomOpenScore() / 10);
                    }
                }
                //个人赛总正确率
                String oneVsOneTrueRate;
                if (oneVsOneNum == 0 || oneVsOneTrueNum == 0) {
                    oneVsOneTrueRate = "0%";
                } else {
                    oneVsOneTrueRate = numberFormat.format((float) oneVsOneTrueNum / (float) (oneVsOneNum * 10) * 100) + "%";
                }

                // 团队赛总次数
                int teamVsTeamNum = 0;
                // 团队赛正确题数
                int teamVsTeamTrueNum = 0;
                // 团队赛总答题数
                int allTeamAnswerNum = 0;
                // 团队赛总正确率
                String teamVsTeamTrueRate = "0%";
                if (userTeamVsTeamLogMap.containsKey(openId)) {
                    List<UserTeamVsTeamLog> userTeamVsTeamLogs = userTeamVsTeamLogMap.get(openId);
                    teamVsTeamNum = userTeamVsTeamLogs.size();
                    teamVsTeamTrueNum = userTeamVsTeamLogs.stream().mapToInt(UserTeamVsTeamLog :: getRightAnswerNum).sum();
                    allTeamAnswerNum = userTeamVsTeamLogs.stream().mapToInt(UserTeamVsTeamLog :: getAllAnswerNum).sum();
                    if (0 != teamVsTeamTrueNum && 0 != allTeamAnswerNum) {
                        teamVsTeamTrueRate = numberFormat.format((float) teamVsTeamTrueNum / (float) allTeamAnswerNum * 100) + "%";
                    }
                }

                // 完成总题目数
                int allAnswerNum = dayGameNum * 10 + oneVsOneNum * 10 + allTeamAnswerNum;
                // 完成正确总题目数
                int allAnswerTrueNum = dayGameTrueNum + oneVsOneTrueNum + teamVsTeamTrueNum;
                // 总正确率
                String allAnswerTrueRate = "";
                if (0 == allAnswerNum || 0 == allAnswerTrueNum) {
                    allAnswerTrueRate = "0%";
                } else {
                    allAnswerTrueRate = numberFormat.format((float) allAnswerTrueNum / (float)allAnswerNum * 100) + "%";
                }

                // 参数赋值
                ExportUserGameCsvVo exportUserGameCsvVo = new ExportUserGameCsvVo();
                exportUserGameCsvVo.setOpenId(user.getOpenId());
                exportUserGameCsvVo.setCompanyName(user.getCompanyName());
                exportUserGameCsvVo.setName(user.getName());
                exportUserGameCsvVo.setExperienceNum(experienceNum);
                exportUserGameCsvVo.setCoinNum(coinNum);
                exportUserGameCsvVo.setAchievementNum(achievementNum);
                exportUserGameCsvVo.setSignNum(signNum);
                exportUserGameCsvVo.setDayGameNum(dayGameNum);
                exportUserGameCsvVo.setDayGameTrueNum(dayGameTrueNum);
                exportUserGameCsvVo.setDayGameTrueRate(dayGameTrueRate);
                exportUserGameCsvVo.setOneVsOneNum(oneVsOneNum);
                exportUserGameCsvVo.setOneVsOneTrueNum(oneVsOneTrueNum);
                exportUserGameCsvVo.setOneVsOneTrueRate(oneVsOneTrueRate);
                exportUserGameCsvVo.setTeamVsTeamNum(teamVsTeamNum);
                exportUserGameCsvVo.setTeamVsTeamTrueNum(teamVsTeamTrueNum);
                exportUserGameCsvVo.setTeamVsTeamTrueRate(teamVsTeamTrueRate);
                exportUserGameCsvVo.setAllAnswerNum(allAnswerNum);
                exportUserGameCsvVo.setAllAnswerTrueRate(allAnswerTrueRate);

                exportUserGameCsvVoList.add(exportUserGameCsvVo);
            }

            return ResultLayuiTable.newSuccess(userIPage.getTotal(), exportUserGameCsvVoList);
        } else {
            return null;
        }
    }


    /**
     * 列表数据（二期）
     * @param name 用户名称，用于用于模糊搜索
     * @param companyName 公司id
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 所属公司类型
     * @return 员工管理-游戏用户集合
     */
    @Override
    public Result selectPages01(String name, String companyName, Integer page, Integer limit, Integer companyType) {
        IPage<User> userPage = this.page(new Page<>(page,limit),new QueryWrapper<User>().eq("deleted",0).like(!StringUtils.isEmpty(name),"name",name)
                .eq(!StringUtils.isEmpty(companyName),"company_name",companyName).eq("type",companyType).isNotNull("mobile").orderByAsc("id"));
        return ResultLayuiTable.newSuccess(userPage.getTotal(), userPage.getRecords());
    }


    /**
     * 定时清除体验账户
     */
    @Override
    public void deleteExperienceUser() {
        this.remove(new QueryWrapper<User>().isNull("id_card").and(wrapper -> wrapper.isNull("mobile")));
    }
}
