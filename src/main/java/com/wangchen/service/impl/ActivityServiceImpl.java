package com.wangchen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.*;
import com.wangchen.mapper.ActivityMapper;
import com.wangchen.mapper.ActivityOptionMapper;
import com.wangchen.mapper.ActivityTopicMapper;
import com.wangchen.service.*;
import com.wangchen.utils.ComputeUtils;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.ActivityTopicHouTaiVo;
import com.wangchen.vo.ActivityTopicVo;
import com.wangchen.vo.ActivityVo;
import com.wangchen.vo.BranchTopicHouTaiVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动赛表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */

@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityOptionService activityOptionService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ActivityTopicMapper activityTopicMapper;

    @Autowired
    private ManguanService manguanService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private UserDayGameLogService userDayGameLogService;

    @Autowired
    private ComputeUtils computeUtils;

    @Autowired
    private UserActivityTopicLogService userActivityTopicLogService;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityOptionMapper activityOptionMapper;

    @Autowired
    private ActivityTopicService activityTopicService;


    /**
     * 获取赛事列表（二期）
     *
     * @param page        页码
     * @param limit       每页数据量
     * @param name        活动名（模糊搜索用）
     * @param companyType 所属公司分类
     * @return 活动赛事集合
     */
    @Override
    @Transactional
    public Result selectPages(int page, int limit, String name, Integer companyType) {
        IPage<Activity> pages = activityMapper.selectPage(new Page<Activity>(page, limit),
                new QueryWrapper<Activity>().eq("company_type", companyType).like(StringUtils.isNotEmpty(name), "name", name).orderByDesc("start_time"));
        // 对活动赛目前状态做判断
        List<Activity> activityList = pages.getRecords().stream().map((activity) -> {
            Integer status = verdictActivityStatus(activity.getStartTime(), activity.getEndTime());
            activity.setDeleted(status);
            return activity;
        }).collect(Collectors.toList());
        // 更改赛事状态(活动赛总体不多，所以直接更新所有活动状态，并不对已经是‘已删除’状态进行过滤后更新)
        if (CollUtil.isNotEmpty(activityList)) {
            for (Activity activity : activityList) {
                activityMapper.updateById(activity);
            }
        }
        return ResultLayuiTable.newSuccess(pages.getTotal(), activityList);
    }


    /**
     * 判断当前时间活动赛所处状态（0:上线中 1:已删除 2:未上线(二期)）
     */
    private Integer verdictActivityStatus(Date startTime, Date endTime) {
        long start = startTime.getTime();
        long end = endTime.getTime();
        long now = DateUtil.date(System.currentTimeMillis()).getTime();
        if (now > end) {
            return 1;
        } else if (now < start) {
            return 2;
        } else {
            return 0;
        }
    }


    /**
     * 修改赛事（二期）
     *
     * @param id          赛事id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param name        题目标题
     * @param companyType 活动赛所属公司类型
     * @return 修改赛事是否成功
     */
    @Override
    @Transactional
    public Result editActivity(Long id, String startTime, String endTime, String name, Integer companyType) throws ParseException {
        long now = DateUtil.date().getTime();
        long start = DateUtil.parse(startTime).getTime();
        long end = DateUtil.parse(endTime).getTime();
        // 当前时间的结束时间 如2021.08.31 23:59:59
        long endOfDay = DateUtil.endOfDay(DateUtil.date()).getTime();
        // 已经结束的不能修改
        if (now > end) {
            return Result.newFaild("已经结束活动不能修改");
        }
        // ‘上线中’活动赛只能修改结束时间至少大于当前时间'一天'，且不能与现有活动活动时间碰撞情况存在交集情况
        if (start < now && now < end) {
            // ‘上线中’活动赛只能修改结束时间至少大于当前时间'一天'
            if (end <= endOfDay) {
                return Result.newFaild("修改后的结束时间至少为当前时间的'下一天'");
            }
            // 判断修改后的时间是否与现有活动时间存在交叉
            Boolean ifExist = timeIntersectionVerdict(id, startTime, endTime, companyType);
            if (ifExist) {
                return Result.newFaild("修改后的开始时间、结束时间不能与现有活动活动时间存在交叉");
            }
        }

        // ‘待上线’活动赛开始时间最少大于当前时间一天，且不能与现有活动活动时间碰撞情况存在交集情况
        if (start > now) {
            // 活动赛开始时间最少大于当前时间一天
            if (start <= endOfDay) {
                return Result.newFaild("活动赛开始时间最少应大于当前时间'一天'");
            }
            // 判断修改后的时间是否与现有活动时间存在交叉
            Boolean ifExist = timeIntersectionVerdict(id, startTime, endTime, companyType);
            if (ifExist) {
                return Result.newFaild("修改后的开始时间、结束时间不能与现有活动活动时间存在交叉");
            }
        }

        // 更新操作
        Activity activity = activityMapper.selectById(id);
        activity.setName(name);
        activity.setStartDate(Constants.SDF_YYYY_MM_DD.parse(startTime));
        activity.setStartTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS.parse(startTime));
        activity.setEndTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS.parse(endTime));
        activity.setUpdateTime(new Date());
        activityMapper.updateById(activity);

        return Result.newSuccess("修改活动赛事信息成功");
    }


    /**
     * 新增赛事（二期）
     *
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param name        题目标题
     * @param type        活动赛所属部门
     * @param topicType   活动赛题目类型
     * @param companyType 活动赛所属公司类型
     * @param file        上传题目excel文件（目前只考虑选择题）
     * @return 新增或修改赛事是否成功
     */
    @Override
    @Transactional
    public Result insertActivity(String startTime, String endTime, String name, Integer type, Integer topicType, Integer companyType, MultipartFile file) throws IOException, InvalidFormatException, ParseException {
        long now = DateUtil.date().getTime();
        long start = DateUtil.parse(startTime).getTime();
        long end = DateUtil.parse(endTime).getTime();
        // 当前时间的结束时间 如2021.08.31 23:59:59
        long endOfDay = DateUtil.endOfDay(DateUtil.date()).getTime();
        // 判断赛事名称不能为空
        if ("".equals(name.trim()) && null == name) {
            return Result.newFaild("活动赛标题不能为空");
        }
        // 开始时间最少为当前时间后一天
        if (start <= endOfDay) {
            return Result.newFaild("开始时间最少为当前时间后一天");
        }
        // 时间不能与‘上线中’及‘待上线’状态的活动赛存在时间上有交集情况
        Boolean ifExist = timeIntersectionVerdict(null, startTime, endTime, companyType);
        if (ifExist) {
            return Result.newFaild("新添加活动的开始时间、结束时间不能与现有活动活动时间存在交叉");
        }

        // 新增保存 (按逻辑保存题目、答案->才保存活动赛对象，但题目对象用到活动赛对象id，固先保存活动赛对象，不影响总体功能)
        Activity activity = new Activity();
        activity.setName(name);
        activity.setStartDate(Constants.SDF_YYYY_MM_DD.parse(startTime));
        activity.setStartTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS.parse(startTime));
        activity.setEndTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS.parse(endTime));
        activity.setCreateTime(new Date());
        activity.setUpdateTime(new Date());
        activity.setDeleted(0);
        activity.setCompanyType(companyType);
        activityMapper.insert(activity);

        return Result.newSuccess("新增活动赛事信息成功");
    }


    /**
     * 判断修改后的时间是否与已有的活动时间存在交集
     */
    private Boolean timeIntersectionVerdict(Long id, String startTime, String endTime, Integer companyType) {
        // 是否与已有活动活动时间存在交集
        Boolean ifExist = false;
        // 查询条件
        QueryWrapper queryWapper = new QueryWrapper<Activity>().ne("deleted", 1).eq("company_type", companyType);
        if (id != null) {
            // 如果是修改操作，则所有活动集合排除当前修改活动项
            queryWapper.ne("id", id);
        }
        // 查询活动赛集合
        List<Activity> activityList = activityMapper.selectList(queryWapper);
        if (CollUtil.isNotEmpty(activityList)) {
            long start = DateUtil.parse(startTime).getTime();
            long end = DateUtil.parse(endTime).getTime();
            Long tempStart = 0L;
            Long tempEnd = 0L;
            // 只有存在未结束的活动，才有判断活动时间交叉的必要
            for (Activity activity : activityList) {
                tempStart = activity.getStartTime().getTime();
                tempEnd = activity.getEndTime().getTime();
                if (end > tempStart && start < tempEnd) {
                    // 与已有活动活动时间存在交集，跳出循环
                    ifExist = true;
                    break;
                }
            }
        }

        return ifExist;
    }


    /**
     * 批量导入选择题
     */
    @Transactional
    public Integer inputXuanZeExcel(Integer activityId, InputStream excelInputSteam, int sheetNumber, int rowStart, Integer type, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        // 收集保存选择题对象
        List<ActivityTopic> activityTopicList = new ArrayList<>();
        //获取内容信息(导入表没有'公司分类这个选项'，导出表也没有)
        for (int i = rowStart; i <= rowEnd; ++i) {
            try {
                Row currentRow = sheet.getRow(i);
                if (Objects.isNull(currentRow)) {
                    continue;
                }
                ActivityTopic activityTopic = new ActivityTopic();
                // 二期后选择题答案不一定为4个，由导入题库选项个数决定，目前暂定校验时最少为2个
                ActivityOption activityOptionA = new ActivityOption();
                ActivityOption activityOptionB = new ActivityOption();
                ActivityOption activityOptionC = new ActivityOption();
                ActivityOption activityOptionD = new ActivityOption();
                activityTopic.setType(type);
                activityTopic.setTopicType(topicType);
                activityTopic.setCompanyType(companyType);
                activityTopic.setActivityId(activityId);
                // 标记哪一项是正确选项
                String select = "";
                // 标记之前选项哪几项不为空(默认不为空)
                Boolean aXZ = false;
                // ABCD选项内容不为空数量
                Integer cellCount = 4;

                for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                    //将null转化为Blank
                    Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                    data.setCellType(Cell.CELL_TYPE_STRING);
                    if (0 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            topicError.append("第" + i + "行第" + (j + 1) + "列[题目]内容不能为空  ");
                        } else {
                            activityTopic.setTitle(data.getStringCellValue().trim());
                            activityTopic.setPoint(data.getStringCellValue().trim());
                            activityTopic.setCreateTime(new Date());
                            activityTopicMapper.insert(activityTopic);
                        }
                    } else if (1 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            topicError.append("第" + i + "行第" + (j + 1) + "列[正确选项]内容不能为空  ");
                        } else {
                            String choose = data.getStringCellValue().trim().toUpperCase();
                            if ("A".equals(choose) || "B".equals(choose) || "C".equals(choose) || "D".equals(choose)) {
                                select = choose;
                            } else {
                                throw new BusinessException("第" + i + "行第" + (j + 1) + "列[正确选项]内容只能为ABCD其中之一");
                            }
                        }
                    } else if (2 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            topicError.append("第" + i + "行第" + (j + 1) + "列[A选项]内容不能为空  ");
                            aXZ = true;
                            cellCount--;
                        } else {
                            activityOptionA.setContent(data.getStringCellValue());
                            activityOptionA.setCreateTime(new Date());
                            activityOptionA.setTopicId(activityTopic.getId());
                            activityOptionMapper.insert(activityOptionA);
                            if ("A".equals(select.trim())) {
                                activityTopic.setCorrectOptionId(activityOptionA.getId());
                                activityTopicMapper.updateById(activityTopic);
                            }
                        }
                    } else if (3 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            topicError.append("第" + i + "行第" + (j + 1) + "列[B选项]内容不能为空  ");
                            cellCount--;
                        } else {
                            if (aXZ) {
                                topicError.append("第" + i + "行第" + (j) + "列[A选项]内容为空，应调整此单元格内容至[A选项]  ");
                            }
                            activityOptionB.setContent(new String(data.toString()));
                            activityOptionB.setCreateTime(new Date());
                            activityOptionB.setTopicId(activityTopic.getId());
                            activityOptionMapper.insert(activityOptionB);
                            if ("B".equals(select.trim())) {
                                activityTopic.setCorrectOptionId(activityOptionB.getId());
                                activityTopicMapper.updateById(activityTopic);
                            }
                        }
                    } else if (4 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            if (cellCount == 2) {
                                topicError.append("第" + i + "行第" + (j + 1) + "列[C选项]之前AB选项均为空，此选项已经不能为空 ");
                            }
                            cellCount--;
                        } else {
                            if (cellCount < 4) {
                                topicError.append("第" + i + "行第" + (j + 1) + "列[C选项]之前有列选项空白，应依次调整前移  ");
                            }
                            activityOptionC.setContent(new String(data.toString()));
                            activityOptionC.setCreateTime(new Date());
                            activityOptionC.setTopicId(activityTopic.getId());
                            activityOptionMapper.insert(activityOptionC);
                            if ("C".equals(select.trim())) {
                                activityTopic.setCorrectOptionId(activityOptionC.getId());
                                activityTopicMapper.updateById(activityTopic);
                            }
                        }
                    } else if (5 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            if (cellCount < 3) {
                                topicError.append("第" + i + "行第" + (j + 1) + "列[D选项]之前至少有2列选项空白，此项不能为空且需依次调整前移  ");
                            }
                        } else {
                            activityOptionD.setContent(new String(data.toString()));
                            activityOptionD.setCreateTime(new Date());
                            activityOptionD.setTopicId(activityTopic.getId());
                            activityOptionMapper.insert(activityOptionD);
                            if ("D".equals(select)) {
                                activityTopic.setCorrectOptionId(activityOptionD.getId());
                                activityTopicMapper.updateById(activityTopic);
                            }
                        }
                    } else if (6 == j) {
                        if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            topicError.append("第" + i + "行第" + (j + 1) + "列[题目解析]内容不能为空  ");
                        } else {
                            activityTopic.setCorrectParse(data.getStringCellValue().trim());
                            activityTopicMapper.updateById(activityTopic);
                        }
                    } else if (7 == j) {
                        // 题目相关图片路径
                        if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            // 二期需求变更，暂时不支持批量图片网址路径上传
                            // activityTopic.setImageUrl(data.getStringCellValue().trim());
                            activityTopic.setImageUrl(null);
                            activityTopicMapper.updateById(activityTopic);
                        }
                    } else if (8 == j) {
                        // 题目相关视频路径
                        if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                            // 二期需求变更，暂时不支持批量图片网址路径上传
                            // activityTopic.setVideoUrl(data.getStringCellValue().trim());
                            activityTopic.setVideoUrl(null);
                            activityTopicMapper.updateById(activityTopic);
                        }
                    }
                }
                activityTopicList.add(activityTopic);
            } catch (Exception e) {
                log.error("第" + i + "道题目导入错误，错误信息: {}", e);
                topicError.append("第" + i + "道题目导入错误 \n");
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入选择题总体数据出错，错误信息: {}", topicError.toString());
            throw new BusinessException("批量导入选择题总体数据出错");
        }
        // 批量上传
        try {
            activityTopicService.saveBatch(activityTopicList);
        } catch (Exception e) {
            log.info("批量导入选择题数据出现错误，错误信息: {}", e);
            e.printStackTrace();
        }

        return activityTopicList.size();
    }


    /**
     * 查看活动赛信息(二期)
     *
     * @param openId 用户id
     * @return 活动赛信息
     */
    @Override
    @Transactional
    public Result getActivityInfo(String openId) throws Exception {
        User user = userService.getUserByOpenId(openId);
        // 1是开启 0是未开启或已结束
        int isFlag = 1;
        // 1是已经答过 0是没有答过
        int isPlayed = 0;
        Map<Object, Object> map = new HashMap<Object, Object>();

        Activity activity = this.getOne(new QueryWrapper<Activity>().eq("deleted", 0).eq("company_type", user.getType()));
        // 存在上线的活动
        if (null != activity) {
            if ((new Date().compareTo(com.wangchen.utils.DateUtil.parse(com.wangchen.utils.DateUtil.format(activity.getEndTime())))) > 0) {
                isFlag = 0;
            }
            if (new Date().compareTo(com.wangchen.utils.DateUtil.parse(com.wangchen.utils.DateUtil.format(activity.getStartTime()))) < 0) {
                isFlag = 0;
            }
            ActivityVo activityVo = new ActivityVo();
            if (null != activity) {
                BeanUtils.copyProperties(activity, activityVo);
                activityVo.setName(activity.getName());
                activityVo.setStartTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS_CHINA.format(activity.getStartTime().getTime()));
                activityVo.setEndTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS_CHINA.format(activity.getEndTime().getTime()));
                // 每日只能答题一次
                UserActivity userActivity = userActivityService.getOne(
                        new QueryWrapper<UserActivity>().eq("open_id", openId)
                                .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
                if (null != userActivity) {
                    isPlayed = 1;
                }
            }
            map.put("activity", activityVo);
            map.put("isFlag", isFlag);
            map.put("isPlayed", isPlayed);
            return Result.newSuccess(map);
        }

        ActivityVo activityVo = new ActivityVo();
        // 查看是否有待上线的活动
        List<Activity> readyActivityList = this.list(new QueryWrapper<Activity>().eq("deleted", 2).eq("company_type", user.getType()).orderByAsc("start_date"));
        if (CollUtil.isNotEmpty(readyActivityList)) {
            activityVo.setStartTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS_CHINA.format(readyActivityList.get(0).getStartTime().getTime()));
            activityVo.setEndTime(Constants.SDF_YYYY_MM_DD_HH_MM_SS_CHINA.format(readyActivityList.get(0).getEndTime().getTime()));
            activityVo.setName(readyActivityList.get(0).getName());
            map.put("activity", activityVo);
            map.put("isFlag", 0);
            map.put("isPlayed", isPlayed);
            return Result.newSuccess(map);
        }

        // 获取之前最晚的一个activity
        activityVo.setStartTime("暂无活动赛");
        activityVo.setEndTime("暂无活动赛");

        map.put("activity", activityVo);
        map.put("isFlag", 0);
        map.put("isPlayed", isPlayed);
        return Result.newSuccess(map);
    }

    @Override
    @Transactional
    public Result getActivityTopicInfo(String openId) throws Exception {
        User user = userService.getUserByOpenId(openId);
        Activity activity = this.getOne(new QueryWrapper<Activity>().eq("deleted", 0).eq("company_type", user.getType()));
        if (null == activity) {
            return Result.newFail("今日还未有活动赛信息");
        }
        if ((new Date().compareTo(com.wangchen.utils.DateUtil.parse(com.wangchen.utils.DateUtil.format(activity.getEndTime())))) > 0) {
            return Result.newFail("今日活动赛已经结束");
        }
        if (new Date().compareTo(com.wangchen.utils.DateUtil.parse(com.wangchen.utils.DateUtil.format(activity.getStartTime()))) < 0) {
            return Result.newFail("活动赛还未开启");
        }
        // 每日只能答题一次
        UserActivity userActivity = userActivityService.getOne(
                new QueryWrapper<UserActivity>().eq("open_id", openId)
                        .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
        if (null != userActivity) {
            return Result.newFail("每日只能答一次活动赛");
        }
        List<ActivityTopic> activityTopicList = activityTopicMapper.listTopicRandom(activity.getId());
        if (activityTopicList.size() < 10) {
            return Result.newFail("活动赛题目未满10题");
        }

        // 记录下题目信息
        StringBuffer topicIds = new StringBuffer();
        List<ActivityTopicVo> activityTopicVoList = new ArrayList<ActivityTopicVo>();
        for (int i = 0; i < activityTopicList.size(); i++) {
            topicIds.append(activityTopicList.get(i).getId() + ",");
            ActivityTopic activityTopic = activityTopicList.get(i);
            ActivityTopicVo activityTopicVo = new ActivityTopicVo();
            BeanUtils.copyProperties(activityTopic, activityTopicVo);
            List<ActivityOption> activityOptionList = activityOptionService.list(new QueryWrapper<ActivityOption>().eq("topic_id", activityTopic.getId()));
            if (2 == activityTopic.getTopicType()) {
                List<ActivityOption> currActivityOptionList = new ArrayList<ActivityOption>();
                for (ActivityOption activityOption : activityOptionList) {
                    String currContent = activityOption.getContent();
                    if ("对".equals(currContent)) {
                        activityOption.setContent("正确");
                    } else {
                        activityOption.setContent("错误");
                    }
                    currActivityOptionList.add(activityOption);
                }
                activityTopicVo.setBranchOptionList(currActivityOptionList);
            } else {
                activityTopicVo.setBranchOptionList(activityOptionList);
            }

            activityTopicVoList.add(activityTopicVo);
        }

        // 挑战答题同样是获取题目已经算完成此次挑战答题，具体分数由答题提交时决定
        userActivity = new UserActivity();
        userActivity.setOpenId(openId);
        userActivity.setName(user.getName());
        userActivity.setActivityId(activity.getId());
        userActivity.setScore(0);
        userActivity.setCreateTime(new Date());
        userActivity.setCreateDate(new Date());
        userActivity.setDeleted(0);
        userActivityService.save(userActivity);

        // 记录下10道题信息
        UserActivityTopicLog userActivityTopicLog = new UserActivityTopicLog();
        userActivityTopicLog.setOpenId(openId);
        userActivityTopicLog.setTopicIds(topicIds.toString());
        userActivityTopicLog.setCreateDate(new Date());
        userActivityTopicLog.setCreateTime(new Date());
        userActivityTopicLogService.save(userActivityTopicLog);

        return Result.newSuccess(activityTopicVoList);
    }

    @Override
    @Transactional
    public Result answerActivity(String openId, Integer score, Integer activityId) {
        User user = userService.getUserByOpenId(openId);
        Activity activity = this.getOne(new QueryWrapper<Activity>().eq("id", activityId).eq("deleted", 0).eq("company_type", user.getType()));
        if (null == activity) {
            return Result.newFail("未获取到该活动赛");
        }
        UserActivity userActivity = userActivityService.getOne(new QueryWrapper<UserActivity>()
                .eq("open_id", openId)
                .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
        if (null == userActivity) {
            log.error("用户并没有先走获取题目的接口，所以是异常提交，openId为:{}", openId);
            return Result.newFail("用户并没有先走获取题目的接口，所以是异常提交");
        }
        // 活动赛，先前用分数是否为零判断是否答过题欠妥，应使用提交时间确认
        if (null != userActivity.getSubmitTime()) {
            log.error("用户已提交过答案，请勿重复提交，openId为:{}", openId);
            return Result.newFail("用户已提交过答案，请勿重复提交");
        }

        userActivity.setScore(score);
        userActivity.setSubmitTime(new Date());
        userActivityService.updateById(userActivity);

        // 计算分数（相应经验值、塔币更新，等级、成就检查更新（公告、称号、弹窗提示））
        computeUtils.computeGame02(openId, score, user);

        // 大满贯成就  对应成就表的id 7是大满贯 8是超级大满贯
        if (score >= 60) {
            // 查看当天每日答题、答题挑战完成情况
            Manguan jinTianManguan = manguanService.getOne(new QueryWrapper<Manguan>().eq("open_id", openId)
                    .eq("answer_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
            if (null != jinTianManguan) {
                UserAchievement isHas1UserAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>()
                        .eq("open_id", openId).eq("achievement_id", 7));

                // 大满贯都还没拥有的话，那昨天肯定就是没完成大满贯的任务 就不用查看昨天的数据了
                if (null == isHas1UserAchievement) {
                    user.setAllAchievement(user.getAllAchievement() + 5);
                    user.setUpdateDate(new Date());
                    userService.updateById(user);

                    // 添加满贯成就
                    UserAchievement userAchievement = new UserAchievement();
                    userAchievement.setOpenId(openId);
                    userAchievement.setAchievementId(7);
                    userAchievement.setAchievementName("大满贯得主");
                    userAchievement.setCreateTime(new Date());
                    userAchievementService.save(userAchievement);

                    // 更新每日同时完成每日答题、活动赛且都及格的连续次数
                    jinTianManguan.setHowNum(1);
                    jinTianManguan.setAnswerTiaozhan(1);
                    manguanService.updateById(jinTianManguan);

                    // 弹框提示
                    AlertTips alertTips = new AlertTips();
                    alertTips.setOpenId(openId);
                    alertTips.setAchievementId(7);
                    alertTips.setAchievementName("大满贯得主");
                    alertTips.setType(1);
                    alertTips.setStatus(0);
                    alertTips.setCreateTime(new Date());
                    alertTipsService.save(alertTips);
                } else {
                    UserAchievement isHas2UserAchievement = userAchievementService.getOne(new QueryWrapper<UserAchievement>()
                            .eq("open_id", openId).eq("achievement_id", 8));
                    // 超级大满贯为空的话 计算当前完成了多少天数了
                    if (null == isHas2UserAchievement) {
                        Manguan zuoTianManguan = manguanService.getOne(new QueryWrapper<Manguan>().eq("open_id", openId)
                                .eq("answer_date", DateUtils.getZuoTianDay()));
                        if (null == zuoTianManguan) {
                            jinTianManguan.setAnswerTiaozhan(1);
                            jinTianManguan.setHowNum(1);
                            manguanService.updateById(jinTianManguan);
                        } else {
                            // 如果昨天不等于空
                            if (0 != zuoTianManguan.getHowNum().intValue()) {
                                jinTianManguan.setAnswerTiaozhan(1);
                                jinTianManguan.setHowNum(zuoTianManguan.getHowNum() + 1);
                                manguanService.updateById(jinTianManguan);

                                if (7 == jinTianManguan.getHowNum()) {
                                    // 已经是完成了超级大满贯得主所需所有要求
                                    user.setAllAchievement(user.getAllAchievement() + 20);
                                    user.setUpdateDate(new Date());
                                    userService.updateById(user);
                                    // 添加满贯成就
                                    UserAchievement userAchievement = new UserAchievement();
                                    userAchievement.setOpenId(openId);
                                    userAchievement.setAchievementId(8);
                                    userAchievement.setAchievementName("超级大满贯得主");
                                    userAchievement.setCreateTime(new Date());
                                    userAchievementService.save(userAchievement);

                                    // 弹框提示
                                    AlertTips alertTips = new AlertTips();
                                    alertTips.setOpenId(openId);
                                    alertTips.setAchievementId(8);
                                    alertTips.setAchievementName("超级大满贯得主");
                                    alertTips.setType(1);
                                    alertTips.setStatus(0);
                                    alertTips.setCreateTime(new Date());
                                    alertTipsService.save(alertTips);
                                }
                            } else {
                                jinTianManguan.setAnswerTiaozhan(1);
                                jinTianManguan.setHowNum(1);
                                manguanService.updateById(jinTianManguan);
                            }
                        }
                    } else {
                        // 相当于大满贯和超级大满贯称号都已经拥有了 那就什么操作都不做啦
                    }
                }
            }
        }

        //返回客户端该分数对应获得的经验和塔币
        //获取分数对应的经验值和塔币
        Integer[] activity_socre = com.wangchen.common.constant.Constants.newActivitySocreMap.get(score.intValue());
        return Result.newSuccess(activity_socre);
    }


    /**
     * 获取活动赛下题目（二期）
     *
     * @param title       题目名称（用于模糊搜索）
     * @param page        页码
     * @param limit       每页数据量
     * @param activityId  活动赛id
     * @param companyType 所属公司类型
     * @return 活动赛下题目集合
     */
    @Override
    @Transactional
    public Result selectActivityTopicPage(String title, int page, int limit, Integer activityId, Integer companyType) {
        IPage<ActivityTopic> pages = activityTopicMapper.selectPage(new Page<ActivityTopic>(page, limit),
                new QueryWrapper<ActivityTopic>().eq("company_type", companyType).like(!StringUtils.isEmpty(title), "title", title).eq("activity_id", activityId).orderByDesc("create_time"));
        return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
    }


    /**
     * 手动新增或查看题目初始化（二期）
     *
     * @param activityId  活动赛id
     * @param companyType 所属公司分类
     * @param topicId     活动赛题目id
     * @return 页面跳转及数据
     */
    @Override
    @Transactional
    public HashMap<String, Object> insertOrEditInit(Integer activityId, Integer companyType, Integer topicId) {
        HashMap<String, Object> map = new HashMap<>();
        if (null != topicId && 0 != topicId) {
            // 修改操作，需查出对应ActivityTopic数据用于回显
            ActivityTopic activityTopic = activityTopicService.getById(topicId);
            ActivityTopicHouTaiVo activityTopicHouTaiVo = new ActivityTopicHouTaiVo();
            BeanUtil.copyProperties(activityTopic, activityTopicHouTaiVo);

            List<ActivityOption> activityOptionList = activityOptionService.list(new QueryWrapper<ActivityOption>().eq("topic_id", activityTopic.getId()));
            if (0 == activityTopic.getTopicType()) {
                // 选择题
                for (ActivityOption activityOption : activityOptionList) {
                    if (activityTopic.getCorrectOptionId().intValue() == activityOption.getId()) {
                        activityTopicHouTaiVo.setContext1(activityOption.getContent());
                        continue;
                    } else {
                        if (StringUtils.isBlank(activityTopicHouTaiVo.getContext2())) {
                            activityTopicHouTaiVo.setContext2(activityOption.getContent());
                            continue;
                        }
                        if (StringUtils.isBlank(activityTopicHouTaiVo.getContext3())) {
                            activityTopicHouTaiVo.setContext3(activityOption.getContent());
                            continue;
                        }
                        if (StringUtils.isBlank(activityTopicHouTaiVo.getContext4())) {
                            activityTopicHouTaiVo.setContext4(activityOption.getContent());
                            continue;
                        }
                    }
                }
            } else if (1 == activityTopic.getTopicType()) {
                // 填空题
                for (ActivityOption activityOption : activityOptionList) {
                    if (StringUtils.isBlank(activityTopicHouTaiVo.getTContext1())) {
                        activityTopicHouTaiVo.setTContext1(activityOption.getContent());
                        continue;
                    }
                    if (StringUtils.isBlank(activityTopicHouTaiVo.getTContext2())) {
                        activityTopicHouTaiVo.setTContext2(activityOption.getContent());
                        continue;
                    }
                }
            } else if (2 == activityTopic.getTopicType()) {
                // 判断题
                for (ActivityOption activityOption : activityOptionList) {
                    if (StringUtils.isBlank(activityTopicHouTaiVo.getPContext())) {
                        activityTopicHouTaiVo.setPContext(activityOption.getContent());
                        continue;
                    }
                }
            }
            map.put("activityTopic", activityTopicHouTaiVo);
        } else {
            // 新增题目（默认选择题）
            ActivityTopicHouTaiVo activityTopicHouTaiVo = new ActivityTopicHouTaiVo();
            activityTopicHouTaiVo.setTopicType(0);
            activityTopicHouTaiVo.setActivityId(activityId);
            map.put("activityTopic", activityTopicHouTaiVo);
        }

        return map;
    }


    /**
     * 删除部门下特定题目（单一道题、二期）
     *
     * @param id 题目id
     */
    @Override
    @Transactional
    public void delTopic(Integer id) {
        // 获取对应题目
        ActivityTopic activityTopic = activityTopicService.getById(id);
        if (null != activityTopic) {
            if (!StringUtils.isEmpty(activityTopic.getImageUrl())) {
                // 删除题目关联图片
                File file = new File(activityTopic.getImageUrl());
                if (file.exists() && file.isFile()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目图片文件出错");
                    }
                }
            }
            if (!StringUtils.isEmpty(activityTopic.getVideoUrl())) {
                // 删除题目关联视频
                File file = new File(activityTopic.getVideoUrl());
                if (file.exists() && file.isFile()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目视频文件出错");
                    }
                }
            }
        }
        // 删除选定题目关联答案
        activityOptionService.remove(new QueryWrapper<ActivityOption>().eq("topic_id", id));
        // 删除选定题目
        activityTopicService.remove(new QueryWrapper<ActivityTopic>().eq("id", id));
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     *
     * @param ids 选中题目id数组
     */
    @Override
    @Transactional(rollbackFor = {})
    public void deleteTopics(Integer[] ids) {
        // 获取对应题目集合
        List<ActivityTopic> activityTopics = activityTopicService.list(new QueryWrapper<ActivityTopic>().in("id", ids));
        if (CollUtil.isNotEmpty(activityTopics)) {
            // 获取有对应图片地址题目合集
            List<ActivityTopic> imageCollect = activityTopics.stream().filter(activityTopic -> !StringUtils.isEmpty(activityTopic.getImageUrl())).collect(Collectors.toList());
            // 获取有对应视频地址题目合集
            List<ActivityTopic> videoCollect = activityTopics.stream().filter(activityTopic -> !StringUtils.isEmpty(activityTopic.getVideoUrl())).collect(Collectors.toList());
            // 删除题目关联图片、视频
            if (CollUtil.isNotEmpty(imageCollect)) {
                for (ActivityTopic activityTopic : imageCollect) {
                    if (!StringUtils.isEmpty(activityTopic.getImageUrl())) {
                        File file = new File(activityTopic.getImageUrl());
                        if (file.exists() && file.isFile()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目图片文件出错");
                            }
                        }
                    }
                }
            }
            if (CollUtil.isNotEmpty(videoCollect)) {
                for (ActivityTopic activityTopic : videoCollect) {
                    if (!StringUtils.isEmpty(activityTopic.getVideoUrl())) {
                        File file = new File(activityTopic.getVideoUrl());
                        if (file.exists() && file.isFile()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目视频文件出错");
                            }
                        }
                    }
                }
            }
        }
        // 删除选定题目关联答案
        activityOptionService.remove(new QueryWrapper<ActivityOption>().in("topic_id", ids));
        // 删除选定题目
        activityTopicService.remove(new QueryWrapper<ActivityTopic>().in("id", ids));
    }

}
