package com.wangchen.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.*;
import com.wangchen.mapper.UserGoodsMapper;
import com.wangchen.service.*;
import com.wangchen.utils.CsvExportUtils;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.ExportCompanyCsvVo;
import com.wangchen.vo.ExportExchangeGoodsCsvVo;
import com.wangchen.vo.ExportUserGameCsvVo;
import com.wangchen.vo.TopicExportVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出 公司活跃度信息 和 个人活跃度信息
 *
 * @author zhangcheng
 * @since 2020-09-14
 */
@Slf4j
@Controller
@RequestMapping("/system/export")
public class ExportController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private BaseUserService baseUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private SevenSignService sevenSignService;

    @Autowired
    private ExperienceService experienceService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private ThreeRoomService threeRoomService;

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    @Autowired
    private ActivityTopicService activityTopicService;

    @Autowired
    private ActivityOptionService activityOptionService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserGoodsMapper userGoodsMapper;

    @Autowired
    private SignService signService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 进入页面
     *
     * @return
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping("/list")
    public String list(Model model) {
        return "export/exportInfo";
    }


    /**
     * 导出 公司活跃数据（二期）
     * @param response 返回请求
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param companyType 所属公司分类
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping(value = "exportCompany")
    public Result exportCompany(HttpServletResponse response,
                                @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
                                @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
                                @RequestParam(value = "companyType", required = false, defaultValue = "") Integer companyType){
        try {
            this.exportCompanyCsv(response, startTime, endTime, companyType);
            return Result.newSuccess("导出公司活跃数据成功");
        } catch (Exception e) {
            log.error("导出公司活跃数据出错，错误信息: {}",e);
            return Result.newFail("导出公司活跃数据出错");
        }
    }


    /**
     * 导出 个人活跃数据(二期)
     * @param response 返回请求
     * @param companyId 公司id
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    @RequiresPermissions("system:user:view")
    @RequestMapping(value = "exportUserGame")
    public void exportUserGame(HttpServletResponse response,
                               @RequestParam(value = "companyId", required = false, defaultValue = "") Integer companyId,
                               @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
                               @RequestParam(value = "endTime", required = false, defaultValue = "1") String endTime){
        try {
            this.exportUserGameCsv(response, companyId, startTime, endTime);
        } catch (Exception e) {
            log.error("导出公司用户活跃数据出错，错误信息: {}",e);
        }
    }


    /**
     * 导出 公司活跃数据（二期 导出vcs文件）
     * @param response 返回请求
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param companyType 所属公司分类
     */
    public void exportCompanyCsv(HttpServletResponse response, String startTime, String endTime, Integer companyType) throws Exception {
        // 今天的开始时间（2021-10-12 00:00:00）
        Date todayBegin = DateUtil.beginOfDay(DateUtil.date());
        // 今天的结束时间（2021-10-12 23:59:59）
        Date todayEnd = DateUtil.endOfDay(DateUtil.date());
        if (DateUtil.parse(startTime).getTime() - DateUtil.parse(endTime).getTime() > 0) {
            // 开始时间大于结束时间，无数据导出
            return;
        }
        startTime = startTime + " 00:00:00";
        endTime = endTime + " 00:00:00";
        // 开始时间、结束时间间隔天数
        long dayNum = DateUtil.between(DateUtil.parseDate(startTime), DateUtil.parseDate(endTime), DateUnit.DAY) + 1;
        // 结束时间、当天时间间隔天数
        long dayNum01 = DateUtil.between(todayBegin, DateUtil.parseDate(endTime), DateUnit.DAY);
        // 创建一个数值格式化对象(用于计算百分比)
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        List<Company> companyList = companyService.list(new QueryWrapper<Company>().eq("type",companyType));
        List<ExportCompanyCsvVo> exportCompanyCsvVoList = new ArrayList<ExportCompanyCsvVo>();
        if (CollUtil.isNotEmpty(companyList)) {
            for(Company company : companyList){
                ExportCompanyCsvVo exportCompanyCsvVo = new ExportCompanyCsvVo();
                // 公司名称
                String companyName = company.getName();
                // 获取公司下基本员工
                List<BaseUser> baseUsers = baseUserService.list(new QueryWrapper<BaseUser>().eq("company",company.getName()));
                // 总人数
                int countNum = baseUsers.size();
                // 查询时间段内公司下所有游戏人员信息
                List<User> userList = userService.list(new QueryWrapper<User>().eq("company_name", company.getName()).lt("registered_time",endTime).eq("deleted",0).isNotNull("mobile"));
                // 获取用户id合集
                List<String> openIdlist = userList.stream().map(User::getOpenId).collect(Collectors.toList());

                // 公司下人员应参与签到、每日答题、个人赛、团队赛天数
                int signNumber = 0;
                // 公司人员应参与团队赛总天数
                // int termNumber = 0;
                for (User user : userList) {
                    Date registeredTime =  user.getRegisteredTime();
                    // 计算在查询期间此人员理论可参与铁塔（签到、每日答题、个人赛、团队赛）天数
                    int count = (int) computeCount(registeredTime, startTime, endTime);
                    signNumber += count;
                    // 计算在查询期间此人员理论可参与铁塔（团队赛）天数
                    // int count02 = (int) computeCountForTerm(registeredTime, startTime, endTime);
                    // termNumber += count02;
                }

                // 注册人数
                int registerNum = userList.size();
                // 总塔币
                int allCoin = userList.stream().mapToInt(User::getAllCoin).sum();
                // 年度经验
                int presentExperience = userList.stream().mapToInt(User::getPresentExperience).sum();
                // 总成就
                int allAchievement = userList.stream().mapToInt(User::getAllAchievement).sum();
                // 总经验
                int allExperience = 0;
                if (0 != dayNum01) {
                    // 结束时间不是当天，总经验用Experience表查询
                    allExperience = queryAllExperienceForCompany(endTime, openIdlist);
                } else {
                    // 结束时间是当天，总经验使用User表查询
                    allExperience = userList.stream().mapToInt(User::getAllExperience).sum();
                }

                // 修改结束时间便于时间区间查询
                endTime = endTime.substring(0,10) + " 23:59:59";
                // 查询时间段内公司下所有人员签到信息
                List<SevenSign> sevenSignList = sevenSignService.list(new QueryWrapper<SevenSign>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdlist));
                // 总签到次数
                int allSign = sevenSignList.size();

                // 查询时间段内公司下所有人员时间区间内每日答题信息
                List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdlist));
                // 时间段内公司员工 总共答过多少题
                int allDayGameCount = userDayGameLogList.size() * 10;
                // 时间段内公司员工 总归答对多少题
                int allDayGameTrueCount = userDayGameLogList.stream().mapToInt(UserDayGameLog :: getScore).sum() / 10;
                // 时间段内公司员工 参与过的总天数之和
                int dayGameCanYu = 0;
                if (CollUtil.isNotEmpty(userDayGameLogList)) {
                    Map<Date, List<UserDayGameLog>> userDayGameLogMap = userDayGameLogList.stream().collect(Collectors.groupingBy(UserDayGameLog::getDayGameDate));
                    Iterator<Date> iterator = userDayGameLogMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        Date key = iterator.next();
                        List<UserDayGameLog> userDayGameLogs = userDayGameLogMap.get(key);
                        ArrayList<UserDayGameLog> dayGameLogArrayList = userDayGameLogs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<UserDayGameLog>(Comparator.comparing(UserDayGameLog::getOpenId))), ArrayList::new));
                        dayGameCanYu += dayGameLogArrayList.size();
                    }
                }

                // 查询时间段内公司下所有人员个人赛信息
                QueryWrapper<UserOneVsOneLog> queryWrapper = new QueryWrapper<>();
                List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.list(queryWrapper.gt("create_time", startTime).lt("create_time", endTime).and(wrapper -> wrapper.in("room_open_id", openIdlist).or().in("friend_open_id", openIdlist)));
                // 时间段内 用户参加每日答题天数之和
                int oneVsOneNum = 0;
                Map<Date, List<UserOneVsOneLog>> userOneVsOneLogMap = userOneVsOneLogList.stream().collect(Collectors.groupingBy(UserOneVsOneLog::getCreateDate));
                Iterator<Date> iterator = userOneVsOneLogMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Date key = iterator.next();
                    List<UserOneVsOneLog> userOneVsOneLogs = userOneVsOneLogMap.get(key);
                    if (CollUtil.isNotEmpty(userOneVsOneLogs)) {
                        HashSet<String> hashSet = new HashSet<>();
                        for (UserOneVsOneLog userOneVsOneLog : userOneVsOneLogs) {
                            hashSet.add(userOneVsOneLog.getRoomOpenId());
                            hashSet.add(userOneVsOneLog.getFriendOpenId());
                        }
                        oneVsOneNum += hashSet.size();
                    }
                }

                // 查询时间段内公司下所有人员团队赛信息
                List<UserTeamVsTeamLog> userTeamVsTeamLogList = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdlist));
                // 时间段内参与团队赛人数
                int teamVsTeamCanYu = userTeamVsTeamLogList.size();

                // 计算各种百分比数值
                // 签到率
                if (0 != allSign) {
                    exportCompanyCsvVo.setAllSignRate(numberFormat.format((float)allSign / (float)(signNumber)*100) + "%");
                } else {
                    exportCompanyCsvVo.setAllSignRate("0%");
                }
                // 每日答题参与率
                if (0 != dayGameCanYu) {
                    exportCompanyCsvVo.setDayGameRate(numberFormat.format((float)dayGameCanYu / (float)(signNumber)*100) + "%");
                } else {
                    exportCompanyCsvVo.setDayGameRate("0%");
                }
                // 每日答题正确率
                if (0 != allDayGameTrueCount) {
                    exportCompanyCsvVo.setDayGameTrueRate(numberFormat.format((float)allDayGameTrueCount / (float)allDayGameCount * 100) + "%");
                } else {
                    exportCompanyCsvVo.setDayGameTrueRate("0%");
                }
                // 个人赛参与率
                if (0 != oneVsOneNum) {
                    exportCompanyCsvVo.setOneVsOneRate(numberFormat.format((float)oneVsOneNum / (float)(signNumber)*100) + "%");
                } else {
                    exportCompanyCsvVo.setOneVsOneRate("0%");
                }
                // 团队赛参与率
                if (0 != teamVsTeamCanYu) {
                    exportCompanyCsvVo.setTeamVsTeamRate(numberFormat.format((float)teamVsTeamCanYu / (float)(signNumber)*100) + "%");
                } else {
                    exportCompanyCsvVo.setTeamVsTeamRate("0%");
                }

                // 统一设置数值
                exportCompanyCsvVo.setCompanyName(companyName);
                exportCompanyCsvVo.setCountNum(countNum);
                exportCompanyCsvVo.setRegisterNum(registerNum);
                exportCompanyCsvVo.setAllCoinNum(allCoin);
                exportCompanyCsvVo.setAllAchievementNum(allAchievement);
                exportCompanyCsvVo.setAllExperienceNum(allExperience);
                exportCompanyCsvVo.setPresentExperienceNum(presentExperience);
                exportCompanyCsvVo.setAllSignNum(allSign);

                exportCompanyCsvVoList.add(exportCompanyCsvVo);
            }
        }

        HashMap map = new LinkedHashMap();
        map.put("1", "单位");
        map.put("2", "总人数");
        map.put("3", "注册人数");
        map.put("4", "塔币(截至"+ todayBegin.toString().substring(0,10) + ")");
        map.put("5", "成就值");
        map.put("6", "总经验值(" + startTime.substring(0,10) + "至" + endTime.substring(0,10) + ")");
        map.put("7", "年度经验值(" + startTime.substring(0,10) + "至" + endTime.substring(0,10) + ")");
        map.put("8", "签到总数");
        map.put("9", "签到率(%)");
        map.put("10", "每日答题参与度(%)");
        map.put("11", "每日答题正确率(%)");
        map.put("12", "个人赛参与度(%)");
        map.put("13", "团队赛参与度(%)");
        String fileds[] = new String[] {"companyName","countNum","registerNum","allCoinNum","allAchievementNum","allExperienceNum",
                "presentExperienceNum","allSignNum","allSignRate","dayGameRate","dayGameTrueRate","oneVsOneRate","teamVsTeamRate"};
        CsvExportUtils.exportFile(response, map, exportCompanyCsvVoList,
                fileds,startTime.substring(0,10)+" - " + endTime.substring(0,10) +"公司活跃");//这是导出csv文件 可以导出10多万的数据
    }

    /**
     * 查询结合用户总经验之和
     * @param endTime 结束时间
     * @param openIdlist 用户openid集合
     * @return 用户总经验之和
     */
    private int queryAllExperienceForCompany(String endTime, List<String> openIdlist) {
        int allExperience = 0;
        // 结束时间所属日期
        int e_day = DateUtil.dayOfMonth(DateUtil.parse(endTime));
        // 结束时间所属月份
        int e_month = DateUtil.month(DateUtil.parse(endTime)) + 1;
        // 结束时间所属年份
        int e_year = DateUtil.year(DateUtil.parse(endTime));
        List<Experience> experienceList = experienceService.list(new QueryWrapper<Experience>().eq("part_month", e_month).eq("part_year", e_year).in("open_id", openIdlist));
        if (CollUtil.isNotEmpty(experienceList)) {
            allExperience = experienceList.stream().mapToInt(experience -> {
                return Integer.parseInt(experience.getDayExperience().split(",")[e_day-1]);
            }).sum();
        }

        return allExperience;
    }


    /**
     * 导出 个人活跃数据（二期 导出vcs文件）
     * @param response 返回请求
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param companyId 公司名称
     */
    public void exportUserGameCsv(HttpServletResponse response, Integer companyId, String startTime, String endTime) throws Exception{
        // 当前时间
        Date now = DateUtil.date();
        if (DateUtil.parse(startTime).getTime() - DateUtil.parse(endTime).getTime() > 0) {
            // 开始时间大于结束时间，无数据导出
            return;
        }
        startTime = startTime + " 00:00:00";
        endTime = endTime + " 00:00:00";

        // 开始时间 <openId, allExperience> 的k-v对照map
        Map<String, Integer> startExperienceMap = new HashMap<String, Integer>();
        // 结束时间 <openId, allExperience> 的k-v对照map
        Map<String, Integer> endExperienceMap = new HashMap<String, Integer>();
        // 对七天签到作 <openId, 签到天数> 的的k-v对照map
        Map<String, List<SevenSign>> sevenSignMap =  new HashMap<String, List<SevenSign>>();
        // 对每日答题集合作 <openId, List<score>> 的k-v对照map
        Map<String, List<UserDayGameLog>> userDayGameMap = new HashMap<String, List<UserDayGameLog>>();
        // 对个人赛集合作 <roomOpenId, List<userOneVsOneLog>> 的k-v对照map
        Map<String, List<UserOneVsOneLog>> roomUserOneVsOneMap = new HashMap<String, List<UserOneVsOneLog>>();
        // 对个人赛集合作 <friendOpenId, List<userOneVsOneLog>> 的k-v对照map
        Map<String, List<UserOneVsOneLog>> friendUserOneVsOneMap = new HashMap<String, List<UserOneVsOneLog>>();
        // 对团队赛信息集合作openId - rightAnswerNum 的k-v对照map
        Map<String, Integer> teamRightAnswerMap = new HashMap<String, Integer>();
        // 对团队赛信息集合作openId - allAnswerNum 的k-v对照map
        Map<String, Integer> teamAllAnswerMap = new HashMap<String, Integer>();
        // 对团队赛信息集合作openId - teamNum 的k-v对照map
        Map<String, Integer> teamNumMap = new HashMap<String, Integer>();

        // 获取公司分类下用户
        List<User> userList = userService.list(new QueryWrapper<User>().isNotNull("mobile").eq("company_id",companyId).eq("deleted",0).lt("registered_time",endTime));
        if (CollUtil.isNotEmpty(userList)) {
            // 获取用户id集合
            List<String> openIdList = userList.stream().map(User::getOpenId).collect(Collectors.toList());
            // 开始时间所属日期
            int s_day = DateUtil.dayOfMonth(DateUtil.parse(startTime));
            // 开始时间所属月份
            int s_month = DateUtil.month(DateUtil.parse(startTime)) + 1;
            // 开始时间所属年份
            int s_year = DateUtil.year(DateUtil.parse(startTime));
            // 结束时间所属日期
            int e_day = DateUtil.dayOfMonth(DateUtil.parse(endTime));
            // 结束时间所属月份
            int e_month = DateUtil.month(DateUtil.parse(endTime)) + 1;
            // 结束时间所属年份
            int e_year = DateUtil.year(DateUtil.parse(endTime));
            // 获取开始时间下用户经验数据集合
            List<Experience> startExperienceList = experienceService.list(new QueryWrapper<Experience>().eq("part_month", s_month).eq("part_year", s_year).in("open_id", openIdList));
            // 获取结束时间下用户经验数据集合
            List<Experience> endExperienceList = new ArrayList<>();
            if (s_month == e_month && s_year == e_year) {
                endExperienceList = startExperienceList;
            } else {
                endExperienceList = experienceService.list(new QueryWrapper<Experience>().eq("part_month", e_month).eq("part_year", e_year).in("open_id", openIdList));
            }
            // 对开始、结束时间下经验集合作 openId-allExperience 的k-v对照map处理，方便后续查询
            startExperienceMap = startExperienceList.stream().collect(Collectors.toMap(Experience::getOpenId, Experience -> {return Integer.parseInt(Experience.getDayExperience().split(",")[s_day-1]);}));
            endExperienceMap = endExperienceList.stream().collect(Collectors.toMap(Experience::getOpenId, Experience -> {return Integer.parseInt(Experience.getDayExperience().split(",")[e_day-1]);}));

            // 修改结束时间便于时间区间查询
            endTime = endTime.substring(0,10) + " 23:59:59";
            // 查询时间段内公司下所有人员签到信息
            List<SevenSign> sevenSignList = sevenSignService.list(new QueryWrapper<SevenSign>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdList));
            // 对七天签到作 openId-签到天数的 的k-v对照map处理，方便后续查询
            sevenSignMap = sevenSignList.stream().collect(Collectors.groupingBy(SevenSign::getOpenId));

            // 查询时间段内公司下所有人员时间区间内每日答题信息
            List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdList));
            // 对每日答题集合作openId-List<score> 的k-v对照map处理，方便后续查询
            userDayGameMap = userDayGameLogList.stream().collect(Collectors.groupingBy(UserDayGameLog::getOpenId));

            // 查询时间段内公司下所有人员个人赛信息
            QueryWrapper<UserOneVsOneLog> queryWrapper = new QueryWrapper<>();
            List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.list(queryWrapper.gt("create_time", startTime).lt("create_time", endTime).and(wrapper -> wrapper.in("room_open_id", openIdList).or().in("friend_open_id", openIdList)));
            // 对个人赛集合作roomOpenId-List<userOneVsOneLog>、friendOpenId-List<userOneVsOneLog> 的k-v对照map处理，方便后续查询
            roomUserOneVsOneMap = userOneVsOneLogList.stream().collect(Collectors.groupingBy(UserOneVsOneLog::getRoomOpenId));
            friendUserOneVsOneMap = userOneVsOneLogList.stream().collect(Collectors.groupingBy(UserOneVsOneLog::getFriendOpenId));

            // 查询时间段内公司下所有人员团队赛信息(每人每天只能参加一次)
            List<UserTeamVsTeamLog> userTeamVsTeamLogList = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>().ge("create_time", startTime).le("create_time", endTime).in("open_id", openIdList));
            if (CollUtil.isNotEmpty(userTeamVsTeamLogList)) {
                for (UserTeamVsTeamLog userTeamVsTeamLog : userTeamVsTeamLogList) {
                    if (teamRightAnswerMap.containsKey(userTeamVsTeamLog.getOpenId())) {
                        teamRightAnswerMap.put(userTeamVsTeamLog.getOpenId(),teamRightAnswerMap.get(userTeamVsTeamLog.getOpenId()) + userTeamVsTeamLog.getRightAnswerNum());
                        teamNumMap.put(userTeamVsTeamLog.getOpenId(), teamNumMap.get(userTeamVsTeamLog.getOpenId()) + 1);
                    } else {
                        teamRightAnswerMap.put(userTeamVsTeamLog.getOpenId(), userTeamVsTeamLog.getRightAnswerNum());
                        teamNumMap.put(userTeamVsTeamLog.getOpenId(), 1);
                    }
                    if (teamAllAnswerMap.containsKey(userTeamVsTeamLog.getOpenId())) {
                        teamAllAnswerMap.put(userTeamVsTeamLog.getOpenId(), teamAllAnswerMap.get(userTeamVsTeamLog.getOpenId()) + userTeamVsTeamLog.getAllAnswerNum());
                    } else {
                        teamAllAnswerMap.put(userTeamVsTeamLog.getOpenId(), userTeamVsTeamLog.getAllAnswerNum());
                    }
                }
            }
        }

        List<ExportUserGameCsvVo> exportUserGameCsvVoList =  new ArrayList<ExportUserGameCsvVo>();
        // 获取公司名称
        String companyName = userList.get(0).getCompanyName();
        // 时间间隔天数
        long dayNum = DateUtil.between(DateUtil.parseDate(startTime), DateUtil.parseDate(endTime), DateUnit.DAY);
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        for (User user : userList) {
            String openId = user.getOpenId();
            ExportUserGameCsvVo exportUserGameCsvVo = new ExportUserGameCsvVo();
            exportUserGameCsvVo.setCompanyName(companyName);
            exportUserGameCsvVo.setName(user.getName());
            exportUserGameCsvVo.setExperienceNum(user.getAllExperience());
            exportUserGameCsvVo.setPresentExperienceNum(user.getPresentExperience());
            exportUserGameCsvVo.setEndExperienceNum(endExperienceMap.get(user.getOpenId()));
            exportUserGameCsvVo.setCoinNum(user.getAllCoin());
            exportUserGameCsvVo.setAchievementNum(user.getAllAchievement());
            exportUserGameCsvVo.setExperienceChangeNum(startExperienceMap.keySet().contains(openId) ? endExperienceMap.get(openId) - startExperienceMap.get(openId) : endExperienceMap.get(openId));

            // 判断是否计算结束时间时年内经验值（如果结束时间不是在今年的话，目前数据库结构是无法计算年内经验值的）
            Integer endPresentExperienceNum = null;
            if (DateUtil.year(DateUtil.parse(now.toString())) == DateUtil.year(DateUtil.parse(endTime))) {
                endPresentExperienceNum = user.getPresentExperience() - user.getAllExperience() + endExperienceMap.get(user.getOpenId());
            }
            exportUserGameCsvVo.setEndPresentExperienceNum(endPresentExperienceNum);

            // 时间内签到次数
            Integer signNum = sevenSignMap.get(openId) == null ? 0 : sevenSignMap.get(openId).size();
            exportUserGameCsvVo.setSignNum(signNum);

            // 每日答题总次数
            Integer dayGameNum = 0;
            // 每日答题正确题数
            Integer dayGameTrueNum = 0;
            if (CollUtil.isNotEmpty(userDayGameMap) && userDayGameMap.containsKey(openId)) {
                dayGameNum = userDayGameMap.get(openId).size();
                dayGameTrueNum = userDayGameMap.get(openId).stream().mapToInt(UserDayGameLog :: getScore).sum() / 10;
            }
            // 每日答题总正确率
            String dayGameTrueRate = "";
            if (0 == dayGameNum || 0 == dayGameTrueNum) {
                dayGameTrueRate = "0%";
            } else {
                dayGameTrueRate = numberFormat.format((float)dayGameTrueNum / (float)(dayGameNum * 10)*100) + "%";
            }
            exportUserGameCsvVo.setDayGameNum(dayGameNum);
            exportUserGameCsvVo.setDayGameTrueNum(dayGameTrueNum);
            exportUserGameCsvVo.setDayGameTrueRate(dayGameTrueRate);

            // 个人赛总次数
            Integer oneVsOneNum = 0;
            // 个人赛正确题数
            Integer oneVsOneTrueNum = 0;
            if (CollUtil.isNotEmpty(roomUserOneVsOneMap) && roomUserOneVsOneMap.containsKey(openId)) {
                oneVsOneNum = roomUserOneVsOneMap.get(openId).size();
                oneVsOneTrueNum = roomUserOneVsOneMap.get(openId).stream().mapToInt(UserOneVsOneLog :: getRoomOpenScore).sum() / 10;
            }
            if (CollUtil.isNotEmpty(friendUserOneVsOneMap) && friendUserOneVsOneMap.containsKey(openId)) {
                oneVsOneNum += friendUserOneVsOneMap.get(openId).size();
                oneVsOneTrueNum += friendUserOneVsOneMap.get(openId).stream().mapToInt(UserOneVsOneLog :: getFriendOpenScore).sum() / 10;
            }
            // 个人赛总正确率
            String oneVsOneTrueRate = "";
            if (0 == oneVsOneNum || 0 == oneVsOneTrueNum) {
                oneVsOneTrueRate = "0%";
            } else {
                oneVsOneTrueRate = numberFormat.format((float)oneVsOneTrueNum / (float)(oneVsOneNum * 10)*100) + "%";
            }
            exportUserGameCsvVo.setOneVsOneNum(oneVsOneNum);
            exportUserGameCsvVo.setOneVsOneTrueNum(oneVsOneTrueNum);
            exportUserGameCsvVo.setOneVsOneTrueRate(oneVsOneTrueRate);

            // 团队赛总次数
            Integer teamVsTeamNum = 0;
            // 团队赛答题总题数
            Integer teamVsTeamCount = 0;
            // 团队赛正确题数
            Integer teamVsTeamTrueNum = 0;
            if (!teamAllAnswerMap.isEmpty() && teamAllAnswerMap.containsKey(openId)) {
                teamVsTeamNum = teamNumMap.get(openId);
                teamVsTeamCount = teamAllAnswerMap.get(openId);
                teamVsTeamTrueNum = teamRightAnswerMap.get(openId);
            }

            //团队赛总正确率
            String teamVsTeamTrueRate = "0%";
            if (0 != teamVsTeamCount && 0 != teamVsTeamTrueNum) {
                teamVsTeamTrueRate = numberFormat.format((float)teamVsTeamTrueNum / (float)teamVsTeamCount * 100) + "%";
            }
            exportUserGameCsvVo.setTeamVsTeamNum(teamVsTeamNum);
            exportUserGameCsvVo.setTeamVsTeamTrueNum(teamVsTeamTrueNum);
            exportUserGameCsvVo.setTeamVsTeamTrueRate(teamVsTeamTrueRate);

            // 完成总题目数
            Integer allAnswerNum = dayGameNum * 10 + oneVsOneNum * 10 + teamVsTeamCount;
            // 完成正确总题目数
            Integer allAnswerTrueNum = dayGameTrueNum + oneVsOneTrueNum + teamVsTeamTrueNum;
            // 总正确率
            String allAnswerTrueRate = "0%";
            if (0 != allAnswerNum && 0 != allAnswerTrueNum) {
                allAnswerTrueRate = numberFormat.format((float)allAnswerTrueNum / (float)(allAnswerNum)*100) + "%";
            }
            exportUserGameCsvVo.setAllAnswerNum(allAnswerNum);
            exportUserGameCsvVo.setAllAnswerTrueRate(allAnswerTrueRate);

            exportUserGameCsvVoList.add(exportUserGameCsvVo);
        }

        // 导出数据
        HashMap map = new LinkedHashMap();
        map.put("1", "单位");
        map.put("2", "姓名");
        map.put("3", "现总经验值("+ now.toString().substring(0,10) +")");
        map.put("4", "现年度经验值("+ now.toString().substring(0,10) +")");
        map.put("5", "总经验值(截至"+ endTime.substring(0,10) +")");
        map.put("6", "年度经验值(截至"+ endTime.substring(0,10) +")");
        map.put("7", "经验变化值("+ startTime.substring(0,10) +"至"+ endTime.substring(0,10) +")");
        map.put("8", "塔币(截至"+ now.toString().substring(0,10) +")");
        map.put("9", "成就");
        map.put("10", "签到次数");
        map.put("11", "每日答题总次数");
        map.put("12", "每日答题正确题数");
        map.put("13", "每日答题总正确率（%）");
        map.put("14", "个人赛总次数");
        map.put("15", "个人赛正确题数");
        map.put("16", "个人赛答题总正确率");
        map.put("17", "团队赛总次数");
        map.put("18", "团队赛正确题数");
        map.put("19", "团队赛答题总正确率");
        map.put("20", "总完成题目次数");
        map.put("21", "总正确率");
        String fileds[] = new String[] {
                "companyName", "name" , "experienceNum", "presentExperienceNum", "endExperienceNum", "endPresentExperienceNum", "experienceChangeNum", "coinNum","achievementNum", "signNum",
                "dayGameNum", "dayGameTrueNum", "dayGameTrueRate", "oneVsOneNum", "oneVsOneTrueNum", "oneVsOneTrueRate" , "teamVsTeamNum",
                "teamVsTeamTrueNum", "teamVsTeamTrueRate", "allAnswerNum", "allAnswerTrueRate"};
        CsvExportUtils.exportFile(response, map, exportUserGameCsvVoList, fileds,
                startTime.substring(0,10)+" - " + endTime.substring(0,10) + "_" + companyName + "员工活跃度");
    }


    /**
     * 导出部门题库下题目数据至Excel表格（二期 目前默认导出选择题,但导出填空题、判断退功能也已经实现）
     * @param response 响应对象
     * @param companyType 所属公司分类
     * @param type 所属部门分类
     * @param topicType 题目分类
     */
    @RequiresPermissions("system:branchTopic:view")
    @RequestMapping(value = "exportBranchToExcel")
    public void exportBranchToExcel(HttpServletResponse response,
                                    @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                    @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                                    @RequestParam(value = "topicType", required = false, defaultValue = "0") Integer topicType) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (null == companyType || null == type || null == topicType) {
            throw new BusinessException("题目所属公司分类、题目所属部门、题目类型不能为空");
        }
        // 导出部门题库下题目数据至Excel表格
        // 查询导出题目数据集合
        List<BranchTopic> branchTopicList = branchTopicService.list(new QueryWrapper<BranchTopic>().eq("type", type).eq("topic_type", topicType).eq("company_type", companyType));
        if (CollUtil.isNotEmpty(branchTopicList)) {
            List<Integer> branchIds = branchTopicList.stream().map(BranchTopic::getId).collect(Collectors.toList());
            // 查询导出题目对应答案数据集合
            List<BranchOption> branchOptionList = branchOptionService.list(new QueryWrapper<BranchOption>().in("topic_id", branchIds));
            if (CollUtil.isNotEmpty(branchOptionList)) {
                // 对答案数据进行处理，便于查找，省去多次数据库查询
                Map<Integer, List<BranchOption>> branchMap = branchOptionList.stream().collect(Collectors.groupingBy(BranchOption::getTopicId));
                // 导出题目数据末班对象集合
                ArrayList<TopicExportVo> branchTopicExportVoList = new ArrayList<TopicExportVo>();
                for (BranchTopic branchTopic : branchTopicList) {
                    TopicExportVo branchTopicExportVo = new TopicExportVo();
                    branchTopicExportVo.setTitle(branchTopic.getTitle().trim());
                    branchTopicExportVo.setCorrectParse(branchTopic.getCorrectParse().trim());
                    branchTopicExportVo.setImageUrl(branchTopic.getImageUrl());
                    branchTopicExportVo.setVideoUrl(branchTopic.getVideoUrl());
                    // 查找题目对应答案集合
                    List<BranchOption> branchOptions = branchMap.get(branchTopic.getId());
                    if (CollUtil.isNotEmpty(branchOptions)) {
                        if (0 == topicType) {
                            // 选择题
                            for (int i = 0; i < branchOptions.size(); i++) {
                                if (branchTopic.getCorrectOptionId().equals(branchOptions.get(i).getId())) {
                                    if (0 == i) {
                                        branchTopicExportVo.setSelect("A");
                                    } else if (1 == i) {
                                        branchTopicExportVo.setSelect("B");
                                    } else if (2 == i) {
                                        branchTopicExportVo.setSelect("C");
                                    } else if (3 == i) {
                                        branchTopicExportVo.setSelect("D");
                                    }
                                }
                                if (0 == i) {
                                    branchTopicExportVo.setContext1(branchOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    branchTopicExportVo.setContext2(branchOptions.get(i).getContent().trim());
                                }
                                if (2 == i) {
                                    branchTopicExportVo.setContext3(branchOptions.get(i).getContent().trim());
                                }
                                if (3 == i) {
                                    branchTopicExportVo.setContext4(branchOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (1 == topicType) {
                            // 填空题
                            for (int i = 0; i < branchOptions.size(); i++) {
                                if (0 == i) {
                                    branchTopicExportVo.setTContext1(branchOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    branchTopicExportVo.setTContext2(branchOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (2 == topicType) {
                            // 判断题
                            branchTopicExportVo.setSelect(branchOptions.get(0).getContent());
                        } else {
                            throw new BusinessException("导出题目类型不对");
                        }
                    }

                    branchTopicExportVoList.add(branchTopicExportVo);
                }
                // 对导出数据进行处理（主要替换英文环境下','为中文环境下的'，'，避免导出数据时因','造成单列数据占据两列甚至多列）
                branchTopicExportVoList = replaceExportData(branchTopicExportVoList);
                // 查询部门名称
                String typeName = Constants.newBuMenKu.get(type)[1];
                String companyTypeName = 1 == companyType ? "A类公司" : 2 == companyType ? "B类公司" : 3 == companyType ? "C类公司" : null;
                String topicTypeName = 0 == topicType ? "选择题" : 1 == topicType ? "填空题" : 2 == topicType ? "判断题" : null;
                HashMap map = new LinkedHashMap();
                if (0 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "A");
                    map.put("4", "B");
                    map.put("5", "C");
                    map.put("6", "D");
                    map.put("7", "题目解析(需要填写@来标注正确答案)");
                    map.put("8", "相关图片地址路径");
                    map.put("9", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "context1", "context2", "context3", "context4", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, branchTopicExportVoList, fileds, companyTypeName + "" + typeName + "部们下题目（" + topicTypeName + "）");
                } else if (1 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "填空项1");
                    map.put("3", "填空项2");
                    map.put("4", "题目解析(需要填写@来标注正确答案)");
                    map.put("5", "相关图片地址路径");
                    map.put("6", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "tContext1", "tContext2", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, branchTopicExportVoList, fileds, companyTypeName + "" + typeName + "部们下题目（" + topicTypeName + "）");
                } else if (2 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "题目解析(需要填写@来标注正确答案)");
                    map.put("4", "相关图片地址路径");
                    map.put("5", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, branchTopicExportVoList, fileds, companyTypeName + "" + typeName + "部们下题目（" + topicTypeName + "）");
                }
            }
        }
    }


    /**
     * 导出非部门题库（必知必会）下题目数据至Excel表格（二期 目前默认导出选择题,但导出填空题、判断退功能也已经实现）
     * @param response 响应对象
     * @param companyType 所属公司分类
     * @param topicType 题目分类
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @RequestMapping(value = "exportFeiBranchToExcel")
    public void exportFeiBranchToExcel(HttpServletResponse response,
                                       @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                       @RequestParam(value = "topicType", required = false, defaultValue = "0") Integer topicType) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (null == companyType || null == topicType) {
            throw new BusinessException("题目所属公司分类、题目类型不能为空");
        }
        // 导出部门题库下题目数据至Excel表格
        // 查询导出题目数据集合
        List<FeiBranchTopic> feiBranchTopicList = feiBranchTopicService.list(new QueryWrapper<FeiBranchTopic>().eq("topic_type", topicType).eq("company_type", companyType));
        if (CollUtil.isNotEmpty(feiBranchTopicList)) {
            List<Integer> feiBranchIds = feiBranchTopicList.stream().map(FeiBranchTopic::getId).collect(Collectors.toList());
            // 查询导出题目对应答案数据集合
            List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>().in("topic_id", feiBranchIds));
            if (CollUtil.isNotEmpty(feiBranchOptionList)) {
                // 对答案数据进行处理，便于查找，省去多次数据库查询
                Map<Integer, List<FeiBranchOption>> feiBranchMap = feiBranchOptionList.stream().collect(Collectors.groupingBy(FeiBranchOption::getTopicId));
                // 导出题目数据末班对象集合
                ArrayList<TopicExportVo> feiBranchTopicExportVoList = new ArrayList<TopicExportVo>();
                for (FeiBranchTopic feiBranchTopic : feiBranchTopicList) {
                    TopicExportVo feiBranchTopicExportVo = new TopicExportVo();
                    feiBranchTopicExportVo.setTitle(feiBranchTopic.getTitle().trim());
                    feiBranchTopicExportVo.setCorrectParse(feiBranchTopic.getCorrectParse().trim());
                    feiBranchTopicExportVo.setImageUrl(feiBranchTopic.getImageUrl());
                    feiBranchTopicExportVo.setVideoUrl(feiBranchTopic.getVideoUrl());
                    // 查找题目对应答案集合
                    List<FeiBranchOption> feiBranchOptions = feiBranchMap.get(feiBranchTopic.getId());
                    if (CollUtil.isNotEmpty(feiBranchOptions)) {
                        if (0 == topicType) {
                            // 选择题
                            for (int i = 0; i < feiBranchOptions.size(); i++) {
                                if (feiBranchTopic.getCorrectOptionId().equals(feiBranchOptions.get(i).getId())) {
                                    if (0 == i) {
                                        feiBranchTopicExportVo.setSelect("A");
                                    } else if (1 == i) {
                                        feiBranchTopicExportVo.setSelect("B");
                                    } else if (2 == i) {
                                        feiBranchTopicExportVo.setSelect("C");
                                    } else if (3 == i) {
                                        feiBranchTopicExportVo.setSelect("D");
                                    }
                                }
                                if (0 == i) {
                                    feiBranchTopicExportVo.setContext1(feiBranchOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    feiBranchTopicExportVo.setContext2(feiBranchOptions.get(i).getContent().trim());
                                }
                                if (2 == i) {
                                    feiBranchTopicExportVo.setContext3(feiBranchOptions.get(i).getContent().trim());
                                }
                                if (3 == i) {
                                    feiBranchTopicExportVo.setContext4(feiBranchOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (1 == topicType) {
                            // 填空题
                            for (int i = 0; i < feiBranchOptions.size(); i++) {
                                if (0 == i) {
                                    feiBranchTopicExportVo.setTContext1(feiBranchOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    feiBranchTopicExportVo.setTContext2(feiBranchOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (2 == topicType) {
                            // 判断题
                            feiBranchTopicExportVo.setSelect(feiBranchOptions.get(0).getContent().trim());
                        } else {
                            throw new BusinessException("导出题目类型不对");
                        }
                    }

                    feiBranchTopicExportVoList.add(feiBranchTopicExportVo);
                }
                // 对导出数据进行处理（主要替换英文环境下','为中文环境下的'，'，避免导出数据时因','造成单列数据占据两列甚至多列）
                feiBranchTopicExportVoList = replaceExportData(feiBranchTopicExportVoList);
                // 查询部门名称
                String companyTypeName = 1 == companyType ? "A类公司" : 2 == companyType ? "B类公司" : 3 == companyType ? "C类公司" : null;
                String topicTypeName = 0 == topicType ? "选择题" : 1 == topicType ? "填空题" : 2 == topicType ? "判断题" : null;
                HashMap map = new LinkedHashMap();
                if (0 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "A");
                    map.put("4", "B");
                    map.put("5", "C");
                    map.put("6", "D");
                    map.put("7", "题目解析(需要填写@来标注正确答案)");
                    map.put("8", "相关图片地址路径");
                    map.put("9", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "context1", "context2", "context3", "context4", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, feiBranchTopicExportVoList, fileds, companyTypeName + "下必知必会题目（" + topicTypeName + "）");
                } else if (1 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "填空项1");
                    map.put("3", "填空项2");
                    map.put("4", "题目解析(需要填写@来标注正确答案)");
                    map.put("5", "相关图片地址路径");
                    map.put("6", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "tContext1", "tContext2", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, feiBranchTopicExportVoList, fileds, companyTypeName + "下必知必会题目（" + topicTypeName + "）");
                } else if (2 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "题目解析(需要填写@来标注正确答案)");
                    map.put("4", "相关图片地址路径");
                    map.put("5", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "correctParse", "imageUrl", "videoUrl"};

                    CsvExportUtils.exportFile(response, map, feiBranchTopicExportVoList, fileds, companyTypeName + "下必知必会题目（" + topicTypeName + "）");
                }
            }
        }
    }


    /**
     * 导出活动题库下题目数据至Excel表格（二期 目前默认导出选择题,但导出填空题、判断退功能也已经实现）
     * @param response 响应对象
     * @param companyType 所属公司分类
     * @param activityId 活动赛id
     * @param topicType 题目分类
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping(value = "exportActivityToExcel")
    public void exportActivityBranchToExcel(HttpServletResponse response,
                                            @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                            @RequestParam(value = "activityId",required = false,defaultValue = "") Integer activityId,
                                            @RequestParam(value = "topicType", required = false, defaultValue = "0") Integer topicType) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (null == companyType || null == topicType) {
            throw new BusinessException("题目所属公司分类、题目类型不能为空");
        }
        // 导出部门题库下题目数据至Excel表格
        // 查询导出题目数据集合
        List<ActivityTopic> activityTopicList = activityTopicService.list(new QueryWrapper<ActivityTopic>().eq("topic_type", topicType).eq(null != activityId,"activity_id", activityId).eq("company_type", companyType));
        String activityName = "";
        if (null != activityId) {
            Activity activity = activityService.getOne(new QueryWrapper<Activity>().eq("id", activityId));
            activityName = activity.getName();
        }
        if (CollUtil.isNotEmpty(activityTopicList)) {
            List<Integer> activityIds = activityTopicList.stream().map(ActivityTopic::getId).collect(Collectors.toList());
            // 查询导出题目对应答案数据集合
            List<ActivityOption> activityOptionList = activityOptionService.list(new QueryWrapper<ActivityOption>().in("topic_id", activityIds));
            if (CollUtil.isNotEmpty(activityOptionList)) {
                // 对答案数据进行处理，便于查找，省去多次数据库查询
                Map<Integer, List<ActivityOption>> activityBranchMap = activityOptionList.stream().collect(Collectors.groupingBy(ActivityOption::getTopicId));
                // 导出题目数据末班对象集合
                ArrayList<TopicExportVo> activityTopicExportVoList = new ArrayList<TopicExportVo>();
                for (ActivityTopic activityTopic : activityTopicList) {
                    TopicExportVo activityTopicExportVo = new TopicExportVo();
                    activityTopicExportVo.setTitle(activityTopic.getTitle().trim());
                    activityTopicExportVo.setCorrectParse(activityTopic.getCorrectParse().trim());
                    activityTopicExportVo.setImageUrl(activityTopic.getImageUrl());
                    activityTopicExportVo.setVideoUrl(activityTopic.getVideoUrl());
                    // 查找题目对应答案集合
                    List<ActivityOption> activityOptions = activityBranchMap.get(activityTopic.getId());
                    if (CollUtil.isNotEmpty(activityOptions)) {
                        if (0 == topicType) {
                            // 选择题
                            for (int i = 0; i < activityOptions.size(); i++) {
                                if (activityTopic.getCorrectOptionId().equals(activityOptions.get(i).getId())) {
                                    if (0 == i) {
                                        activityTopicExportVo.setSelect("A");
                                    } else if (1 == i) {
                                        activityTopicExportVo.setSelect("B");
                                    } else if (2 == i) {
                                        activityTopicExportVo.setSelect("C");
                                    } else if (3 == i) {
                                        activityTopicExportVo.setSelect("D");
                                    }
                                }
                                if (0 == i) {
                                    activityTopicExportVo.setContext1(activityOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    activityTopicExportVo.setContext2(activityOptions.get(i).getContent().trim());
                                }
                                if (2 == i) {
                                    activityTopicExportVo.setContext3(activityOptions.get(i).getContent().trim());
                                }
                                if (3 == i) {
                                    activityTopicExportVo.setContext4(activityOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (1 == topicType) {
                            // 填空题
                            for (int i = 0; i < activityOptions.size(); i++) {
                                if (0 == i) {
                                    activityTopicExportVo.setTContext1(activityOptions.get(i).getContent().trim());
                                }
                                if (1 == i) {
                                    activityTopicExportVo.setTContext2(activityOptions.get(i).getContent().trim());
                                }
                            }
                        } else if (2 == topicType) {
                            // 判断题
                            activityTopicExportVo.setSelect(activityOptions.get(0).getContent().trim());
                        } else {
                            throw new BusinessException("导出题目类型不对");
                        }
                    }

                    activityTopicExportVoList.add(activityTopicExportVo);
                }
                // 对导出数据进行处理（主要替换英文环境下','为中文环境下的'，'，避免导出数据时因','造成单列数据占据两列甚至多列）
                activityTopicExportVoList = replaceExportData(activityTopicExportVoList);
                // 查询部门名称
                String companyTypeName = 1 == companyType ? "A类公司" : 2 == companyType ? "B类公司" : 3 == companyType ? "C类公司" : null;
                String topicTypeName = 0 == topicType ? "选择题" : 1 == topicType ? "填空题" : 2 == topicType ? "判断题" : null;
                HashMap map = new LinkedHashMap();
                if (0 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "A");
                    map.put("4", "B");
                    map.put("5", "C");
                    map.put("6", "D");
                    map.put("7", "题目解析(需要填写@来标注正确答案)");
                    map.put("8", "相关图片地址路径");
                    map.put("9", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "context1", "context2", "context3", "context4", "correctParse", "imageUrl", "videoUrl"};

                    if (null != activityId) {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下'"+ activityName + "'题目（" + topicTypeName + "）");
                    } else {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下活动赛题目（" + topicTypeName + "）");
                    }
                } else if (1 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "填空项1");
                    map.put("3", "填空项2");
                    map.put("4", "题目解析(需要填写@来标注正确答案)");
                    map.put("5", "相关图片地址路径");
                    map.put("6", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "tContext1", "tContext2", "correctParse", "imageUrl", "videoUrl"};

                    if (null != activityId) {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下'"+ activityName + "'题目（" + topicTypeName + "）");
                    } else {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下活动赛题目（" + topicTypeName + "）");
                    }
                } else if (2 == topicType) {
                    map.put("1", "题目");
                    map.put("2", "答案");
                    map.put("3", "题目解析(需要填写@来标注正确答案)");
                    map.put("4", "相关图片地址路径");
                    map.put("5", "相关视频地址路径");
                    String[] fileds = new String[] {"title", "select", "correctParse", "imageUrl", "videoUrl"};

                    if (null != activityId) {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下'"+ activityName + "'题目（" + topicTypeName + "）");
                    } else {
                        CsvExportUtils.exportFile(response, map, activityTopicExportVoList, fileds, companyTypeName + "下活动赛题目（" + topicTypeName + "）");
                    }
                }
            }
        }
    }


    /**
     * 整体替换导出数据内容中，英文环境下的','为中文环境下的'，'
     * @param branchTopicExportVoList 导出数据
     * @return 内容作替换后的导出数据
     */
    private ArrayList<TopicExportVo> replaceExportData(ArrayList<TopicExportVo> branchTopicExportVoList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 遍历TopicExportVo类 属性内容包含英文输入环境','全部替换为中文环境下的'，'
        if (CollUtil.isNotEmpty(branchTopicExportVoList)) {
            // 获取导出数据类所有属性字段
            Field[] fields = branchTopicExportVoList.get(0).getClass().getDeclaredFields();
            for (TopicExportVo topicExportVo : branchTopicExportVoList) {
                for (int i = 0; i < fields.length; i++) {
                    // 获取属性的名字
                    String name = fields[i].getName();
                    // 将属性的首字符大写，方便构造get，set方法
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    // 获取属性的类型
                    String type = fields[i].getGenericType().toString();
                    if (type.equals("class java.lang.String")) {
                        // 获取属性对应get方法
                        Method method = topicExportVo.getClass().getMethod("get" + name);
                        // 调用getter方法获取属性值
                        String value = (String)method.invoke(topicExportVo);
                        String newValue = value;
                        if (null != value && value.contains(",")) {
                            // 替换英文输入环境','为中文环境下的'，'
                            newValue = value.replaceAll(",","，");
                            newValue = newValue.replaceAll(String.valueOf((char)32),"");
                        }
                        // 将替换后的值set回原来属性中
                        Class[] parameterTypes = new Class[1];
                        parameterTypes[0] = fields[i].getType();
                        method = topicExportVo.getClass().getMethod("set" + name, parameterTypes);
                        Object[] objects = new Object[1];
                        objects[0] = newValue;
                        method.invoke(topicExportVo, objects);
                    }
                }
            }

            return branchTopicExportVoList;
        } else {
            return null;
        }
    }


    /**
     * 导出固定时间内实物兑换记录信息
     * @param startTime 兑换开始时间
     * @param endTime 兑换结束时间
     * @param goodId 商品id
     * @param response 响应对象
     */
    @RequiresPermissions("system:goods:view")
    @RequestMapping(value = "exportExchangeGoodsInfo")
    public void exportExchangeGoodsInfo(@RequestParam(value = "startTime", required = true, defaultValue = "") String startTime,
                                        @RequestParam(value = "endTime", required = true, defaultValue = "") String endTime,
                                        @RequestParam(value = "goodId", required = false, defaultValue = "") Integer goodId,
                                        HttpServletResponse response) throws IOException {

        // 今天的开始时间（2021-11-30 00:00:00）
        Date todayBegin = DateUtil.beginOfDay(DateUtil.date());
        // 今天的结束时间（2021-11-30 23:59:59）
        Date todayEnd = DateUtil.endOfDay(DateUtil.date());
        if (StringUtils.isEmpty(startTime)) {
            throw new BusinessException("兑换开始时间不能为空");
        } else {
            // 判断开始时间是否大于今天
            if (DateUtil.parse(startTime).getTime() - todayBegin.getTime() > 0) {
                startTime = DateUtil.format(todayBegin, "yyyy-MM-dd HH:mm:ss");
            } else {
                startTime = startTime + " 00:00:00";
            }
        }
        // 判断结束时间
        if (StringUtils.isEmpty(endTime)) {
            throw new BusinessException("兑换结束时间不能为空");
        } else {
            // 判断大于等于开始时间
            endTime = endTime + " 23:59:59";
            if (DateUtil.parse(startTime).getTime() - DateUtil.parse(endTime).getTime() > 0) {
                throw new BusinessException("商品兑换开始时间不能大于结束时间");
            } else {
                // 如果结束时间大于今天开始时间，则设置值为今天结束时间
                if (DateUtil.parse(endTime).getTime() - todayBegin.getTime() > 0) {
                    endTime = DateUtil.format(todayEnd, "yyyy-MM-dd HH:mm:ss");
                }
            }
        }

        // 查询特定类型下所有时间段内兑换记录
        List<ExportExchangeGoodsCsvVo> exportExchangeGoodsCsvVos = userGoodsMapper.exportExchangeGoodsInfo(2);
        if (!CollUtil.isNotEmpty(exportExchangeGoodsCsvVos)) {
            return;
        }
        if (null != goodId) {
            if (goodId < 1) {
                throw new BusinessException("兑换商品id不能为空且不能小于1");
            }
            // 挑选出特定某一个实物商品兑换记录
            exportExchangeGoodsCsvVos = exportExchangeGoodsCsvVos.stream().filter(exportExchangeGoodsCsvVo -> exportExchangeGoodsCsvVo.getGoodId().equals(goodId)).collect(Collectors.toList());
        }
        if (!CollUtil.isNotEmpty(exportExchangeGoodsCsvVos)) {
            return;
        }
        List<ExportExchangeGoodsCsvVo> newExportExchangeGoodsCsvVos = new ArrayList<>();
        long start = DateUtil.parse(startTime).getTime();
        long end = DateUtil.parse(endTime).getTime();
        for (ExportExchangeGoodsCsvVo exportExchangeGoodsCsvVo : exportExchangeGoodsCsvVos) {
            long now = DateUtil.parse(exportExchangeGoodsCsvVo.getExchangeTime()).getTime();
            if (now >= start && now <= end) {
                newExportExchangeGoodsCsvVos.add(exportExchangeGoodsCsvVo);
            }
        }

        HashMap map = new LinkedHashMap();
        map.put("1", "用户id");
        map.put("2", "用户姓名");
        map.put("3", "电话号码");
        map.put("4", "公司名称");
        map.put("5", "商品名称");
        map.put("6", "兑换时间");
        map.put("7", "商品收货地址");
        String fileds[] = new String[] {"openId","name","mobile","companyName","goodName", "exchangeTime","address"};
        CsvExportUtils.exportFile(response, map, newExportExchangeGoodsCsvVos,
                fileds,startTime.substring(0,10)+" - " + endTime.substring(0,10) +"实物商品兑换记录");
    }


    /**
     * daochu 某时间段公司活跃度数据导出（弥补因先前团队赛后上线导致整体区间必须位于团队赛2021-10-08之后）
     * @param startTime
     * @param endTime
     */
    @RequiresPermissions("tsystem:user:view")
    @RequestMapping(value = "exportCompanySection")
    public void test12138(@RequestParam(value = "startTime") String startTime,
                          @RequestParam(value = "endTime") String endTime) {
        List<User> userList = userService.list(new QueryWrapper<User>().eq("deleted", 0));
        Map<String, List<User>> map = userList.stream().collect(Collectors.groupingBy(u -> u.getCompanyName()));
        // 每个公司及对应人员数目
        Map<String, Integer> personMap = new HashMap<>();
        // 公司下人员应参与签到、每日答题、个人赛、团队赛总天数
        Map<String, Integer> signNumberMap = new HashMap<>();
        // 公司人员应参与团队赛总天数 -> 宁夏铁塔一开始即可使用团队赛功能，所能参加天数不用单独计算
        // Map<String, Integer> termNumberMap = new HashMap<>();

        // 公司人员实签到总次数
        Map<String, Integer> sjSumSignMap = new HashMap<>();
        // 公司人员实际参与每日答题总次数（去重）
        Map<String, Integer> dayGameMap = new HashMap<>();
        // 公司人员实际参与个人赛总次数
        Map<String, Integer> oneGameMap = new HashMap<>();
        // 公司人员实际参与团队赛总次数
        Map<String, Integer> sjTermGameMap = new HashMap<>();

        Iterator<String> iterator = map.keySet().iterator();
        while(iterator.hasNext()) {
            String index = iterator.next();
            List<User> users = map.get(index);
            personMap.put(index, users.size());

            // 公司下人员应参与签到、每日答题、个人赛天数
            int signNumber = 0;
            // 公司人员应参与团队赛总天数
            int termNumber = 0;
            for (User user : users) {
                Date registeredTime =  user.getRegisteredTime();
                // 计算在查询期间此人员理论可参与铁塔（签到、每日答题、个人赛、团队赛）天数
                int count01 = (int) computeCount(registeredTime, startTime, endTime);
                signNumber += count01;
                // 计算在查询期间此人员理论可参与铁塔（团队赛）天数 -> 宁夏铁塔一开始团队赛即可使用，不用单独计算
                // int count02 = (int) computeCountForTerm(registeredTime, startTime, endTime);
                // termNumber += count01;
            }
            signNumberMap.put(index, signNumber);
            // termNumberMap.put(index, termNumber);

            List<String> openIdList = users.stream().map(User::getOpenId).collect(Collectors.toList());
            // 查询区间内人员的签到次数
            List<Sign> list = signService.list(new QueryWrapper<Sign>().ge("sign_date", startTime).le("create_time", endTime).in("open_id", openIdList));
            String str01 = "";
            for (String openId : openIdList) {
                str01 = str01 + openId + ",";
            }
            sjSumSignMap.put(index, list.size());

            // 查询时间段内公司下所有人员时间区间内每日答题信息
            List<UserDayGameLog> userDayGameLogList = userDayGameLogService.list(new QueryWrapper<UserDayGameLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdList));
            // 时间段内公司员工 参与过的总天数之和
            int dayGameCanYu = 0;
            if (CollUtil.isNotEmpty(userDayGameLogList)) {
                Map<Date, List<UserDayGameLog>> userDayGameLogMap = userDayGameLogList.stream().collect(Collectors.groupingBy(UserDayGameLog::getDayGameDate));
                Iterator<Date> iterator01 = userDayGameLogMap.keySet().iterator();
                while (iterator01.hasNext()) {
                    Date key = iterator01.next();
                    List<UserDayGameLog> userDayGameLogs = userDayGameLogMap.get(key);
                    ArrayList<UserDayGameLog> dayGameLogArrayList = userDayGameLogs.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<UserDayGameLog>(Comparator.comparing(UserDayGameLog::getOpenId))), ArrayList::new));
                    dayGameCanYu += dayGameLogArrayList.size();
                }
            }
            dayGameMap.put(index, dayGameCanYu);

            // 查询时间段内公司下所有人员个人赛信息
            QueryWrapper<UserOneVsOneLog> queryWrapper = new QueryWrapper<>();
            List<UserOneVsOneLog> userOneVsOneLogList = userOneVsOneLogService.list(queryWrapper.gt("create_time", startTime).lt("create_time", endTime).and(wrapper -> wrapper.in("room_open_id", openIdList).or().in("friend_open_id", openIdList)));
            // 时间段内 用户参加每日答题天数之和
            int oneVsOneNum = 0;
            Map<Date, List<UserOneVsOneLog>> userOneVsOneLogMap = userOneVsOneLogList.stream().collect(Collectors.groupingBy(UserOneVsOneLog::getCreateDate));
            Iterator<Date> iterator02 = userOneVsOneLogMap.keySet().iterator();
            while (iterator02.hasNext()) {
                Date key = iterator02.next();
                List<UserOneVsOneLog> userOneVsOneLogs = userOneVsOneLogMap.get(key);
                if (CollUtil.isNotEmpty(userOneVsOneLogs)) {
                    HashSet<String> hashSet = new HashSet<>();
                    for (UserOneVsOneLog userOneVsOneLog : userOneVsOneLogs) {
                        hashSet.add(userOneVsOneLog.getRoomOpenId());
                        hashSet.add(userOneVsOneLog.getFriendOpenId());
                    }
                    oneVsOneNum += hashSet.size();
                }
            }
            oneGameMap.put(index, oneVsOneNum);

            // 查询时间段内公司下所有人员团队赛信息
            List<UserTeamVsTeamLog> userTeamVsTeamLogList = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>().gt("create_time", startTime).lt("create_time", endTime).in("open_id", openIdList));
            // 时间段内参与团队赛人数
            int teamVsTeamCanYu = userTeamVsTeamLogList.size();
            sjTermGameMap.put(index, teamVsTeamCanYu);
        }

        System.out.println("每个公司及对应人员数目:" + personMap);
        System.out.println("公司下人员应参与签到、每日答题、个人赛天数:" + signNumberMap);
        System.out.println("公司人员应参与团队赛总天数:" + signNumberMap);
        System.out.println("公司人员实签到总次数:" + sjSumSignMap);
        System.out.println("公司人员实际参与每日答题总次数（去重）:" + dayGameMap);
        System.out.println("公司人员实际参与个人赛总次数:" + oneGameMap);
        System.out.println("公司人员实际参与团队赛总次数:" + sjTermGameMap);
    }


    /**
     * 计算在查询期间此人员理论可参与铁塔（团队赛）天数
     * @param registeredTime 用户注册时间
     * @param startTime 查询期间开始时间
     * @param endTime 查询期间结束时间
     * @return 查询期间用户实际可参与铁塔平台答题天数
     */
    private long computeCountForTerm(Date registeredTime, String startTime, String endTime) {
        // 获取铁塔团队赛上线时间(理论只上线一次)
        Map<Integer,String> startTermTime = Constants.startTermTime;
        // 查询期间此人员理论可参与铁塔（团队赛）天数
        int termCount = 0;
        if (DateUtil.parse(endTime, "yyyy-MM-dd").getTime() > DateUtil.parse(startTermTime.get(1), "yyyy-MM-dd").getTime()) {
            if (DateUtil.parse(startTermTime.get(1), "yyyy-MM-dd").getTime() > DateUtil.parse(startTime, "yyyy-MM-dd").getTime()) {
                termCount = (int) computeCount(registeredTime, startTermTime.get(1), endTime);
            } else {
                termCount = (int) computeCount(registeredTime, startTime, endTime);
            }
        }

        return termCount;
    }


    /**
     * 计算在查询期间此人员理论可参与铁塔答题天数
     * @param registeredTime 用户注册时间
     * @param startTime 查询期间开始时间
     * @param endTime 查询期间结束时间
     * @return 查询期间用户实际可参与铁塔平台答题天数
     */
    private long computeCount(Date registeredTime, String startTime, String endTime) {
        // 计算注册日期至查询结束时间 天数差
        Long count = 0L;
        if (DateUtil.parse(startTime, "yyyy-MM-dd").getTime() > registeredTime.getTime()) {
            count = DateUtil.betweenDay(DateUtil.parseDate(startTime), DateUtil.parseDate(endTime),false) + 1;
        } else {
            count = DateUtil.betweenDay(registeredTime, DateUtil.parseDate(endTime), false) + 1;
        }

        // 获取相应停服时间信息
        Map<Integer,String> stopServerTime = Constants.stopServerTime;
        // 获取铁塔团队赛上线时间(理论只上线一次)
        Map<Integer,String> startTermTime = Constants.startTermTime;
        // 应扣除天数之和
        Long subtractCount = 0L;
        if (startTermTime.size() > 0) {
            Iterator iterator = stopServerTime.keySet().iterator();
            while (iterator.hasNext()) {
                String dateSection = stopServerTime.get(iterator.next());
                String startDateSection = dateSection.split("~")[0];
                String endDateSection = dateSection.split("~")[1];
                // 对停服时间段是否符合此次区间查询进行判断
                if (DateUtil.parse(endTime, "yyyy-MM-dd").getTime() > DateUtil.parse(startDateSection, "yyyy-MM-dd").getTime() && DateUtil.parse(startTime, "yyyy-MM-dd").getTime() < DateUtil.parse(endDateSection, "yyyy-MM-dd").getTime()) {
                    if (registeredTime.getTime() <= DateUtil.parse(startDateSection, "yyyy-MM-dd").getTime()) {
                        subtractCount += DateUtil.betweenDay(DateUtil.parse(startDateSection, "yyyy-MM-dd"), DateUtil.parse(endDateSection, "yyyy-MM-dd"), false);
                    } else if (registeredTime.getTime() <= DateUtil.parse(endDateSection, "yyyy-MM-dd").getTime() && registeredTime.getTime() >= DateUtil.parse(startDateSection, "yyyy-MM-dd").getTime()) {
                        if (DateUtil.parse(endTime, "yyyy-MM-dd").getTime() > DateUtil.parse(endDateSection, "yyyy-MM-dd").getTime()) {
                            subtractCount = subtractCount + DateUtil.betweenDay(registeredTime, DateUtil.parse(endDateSection, "yyyy-MM-dd"), false) + 1;
                        } else {
                            subtractCount = subtractCount + DateUtil.betweenDay(registeredTime, DateUtil.parse(endTime, "yyyy-MM-dd"), false) + 1;
                        }
                    }
                }
            }
        }

        return count - subtractCount;
    }


}
