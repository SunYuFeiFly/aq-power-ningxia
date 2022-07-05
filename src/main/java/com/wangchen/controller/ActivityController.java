package com.wangchen.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.Constants;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.*;
import com.wangchen.mapper.*;
import com.wangchen.service.ActivityOptionService;
import com.wangchen.service.ActivityService;
import com.wangchen.service.ActivityTopicService;
import com.wangchen.vo.ActivityTopicHouTaiVo;
import com.wangchen.vo.UserActivityVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Slf4j
@Controller
@RequestMapping("/system/activity")
public class ActivityController {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityTopicMapper activityTopicMapper;

    @Autowired
    private ActivityOptionMapper activityOptionMapper;

    @Autowired
    private BranchTopicMapper branchTopicMapper;

    @Autowired
    private BranchOptionMapper branchOptionMapper;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityTopicService activityTopicService;

    @Autowired
    private ActivityOptionService activityOptionService;


    /**
     * 进入赛事页面(二期)
     * @param model 模板
     * @param companyType 所属公司类型
     * @return 跳转页面
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/list")
    public String index(Model model,
                        @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "activity/aList";
        } else if (2 == companyType) {
            return "activity/bList";
        } else if (3 == companyType) {
            return "activity/cList";
        }

        return null;
    }


    /**
     * 获取赛事列表（二期）
     * @param page 页码
     * @param limit 每页数据量
     * @param name 活动名（模糊搜索用）
     * @param companyType 所属公司分类
     * @return 活动赛事集合
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("selectPages")
    @ResponseBody
    public Result selectPages(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                              @RequestParam(value = "name", required = false, defaultValue = "") String name,
                              @RequestParam(value = "companyType", required = true, defaultValue = "1") Integer companyType) {
        try {
            Result result = activityService.selectPages(page, limit, name, companyType);
            return result;
        } catch (Exception e) {
            log.error("获取活动赛事列表出错，错误信息: {}",e);
            return Result.newFaild("获取活动赛事列表出错");
        }
    }


    /**
     * 编辑前查看赛事(二期)
     * @param model 数据模型
     * @param id 活动赛id
     * @param companyType 活动赛所属公司分类
     * @return 页面跳转路径
     */
    @RequiresPermissions("system:activity:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id,
                       @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        if(null == id || 0 == id){
            model.addAttribute("isEdit", false);
            model.addAttribute("activity", null);
        }else{
            Activity activity = activityService.getOne(new QueryWrapper<Activity>().eq("id", id).eq("company_type", companyType));
            model.addAttribute("isEdit", true);
            model.addAttribute("activity", activity);
        }
        model.addAttribute("companyType",companyType);
        model.addAttribute("id",id);
        if (1 == companyType) {
            return "activity/aEdit";
        } else if (2 == companyType) {
            return "activity/bEdit";
        } else if (3 == companyType) {
            return "activity/cEdit";
        }

        return null;
    }


    /**
     * 新增或修改赛事（二期）
     * @param id 赛事id
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param name 题目标题
     * @param type 活动赛所属部门
     * @param topicType 活动赛题目类型
     * @param companyType 活动赛所属公司类型
     * @param file 上传题目excel文件（目前只考虑选择题）
     * @return 新增或修改赛事是否成功
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/editActivity")
    @ResponseBody
    public Result editActivity(@RequestParam(value = "id", required = false, defaultValue = "") Long id,
                               @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
                               @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
                               @RequestParam(value = "name", required = false, defaultValue = "") String name,
                               @RequestParam(value = "type",required = false) Integer type,
                               @RequestParam(value = "topicType",required = false,defaultValue = "0") Integer topicType,
                               @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType,
                               MultipartFile file) throws Exception {
        // 判断时间不能为空，且开始时间小于结束时间
        startTime = startTime.trim();
        endTime = endTime.trim();
        if ("".equals(startTime) && startTime == null) {
            return Result.newFaild("开始时间不能为空！");
        }
        if ("".equals(endTime) && endTime == null) {
            return Result.newFaild("结束时间不能为空！");
        }
        // 计算开始时间与结束时间时间差（小时）
        Long reduceHours = reduceHours(startTime,endTime);
        if (reduceHours <= 0) {
            return Result.newFaild("开始时间不能大于结束时间！");
        } else if (reduceHours < 2) {
            return Result.newFaild("开始时间与结束时间时间差最少为2个小时！");
        }
        // 根据id是否有值判断是新增、修改操作
        try {
            if (id != null) {
                // 修改操作
                Result result = activityService.editActivity(id, startTime, endTime, name, companyType);
                return result;
            } else {
                // 新增操作
                Result result = activityService.insertActivity(startTime, endTime, name, type, topicType, companyType, file);
                return result;
            }
        } catch (Exception e) {
            log.error("新增或修改活动赛事出错，错误信息: {}",e);
            return Result.newFaild("新增或修改活动赛事出错");
        }
    }


    /**
     * 新增或修改活动赛题目（单个 二期）
     * @param activityTopicHouTaiVo 活动赛题目对象
     * @return 添加或修改是否成功
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/editActivityTopic")
    @ResponseBody
    public Result editActivityTopic(ActivityTopicHouTaiVo activityTopicHouTaiVo) {
        try {
            // 公共参数校验
            if (StringUtils.isEmpty(activityTopicHouTaiVo.getTitle())) {
                return Result.newFaild("添加或修改题目不能为空");
            }
            if (StringUtils.isEmpty(activityTopicHouTaiVo.getCorrectParse())) {
                return Result.newFaild("添加或修改题目正确解析不能为空");
            }
            if (0 == activityTopicHouTaiVo.getTopicType()) {
                // 选择题
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getContext1())) {
                    return Result.newFaild("选择题的正确答案选项不能为空");
                }
                // 选择题选项重新赋值，去除期间的空白项、重复项
                LinkedHashSet<String> contexts = new LinkedHashSet<>();
                contexts.add(activityTopicHouTaiVo.getContext1());
                if (!StringUtils.isEmpty(activityTopicHouTaiVo.getContext2())) {
                    contexts.add(activityTopicHouTaiVo.getContext2());
                }
                if (!StringUtils.isEmpty(activityTopicHouTaiVo.getContext3())) {
                    contexts.add(activityTopicHouTaiVo.getContext3());
                }
                if (!StringUtils.isEmpty(activityTopicHouTaiVo.getContext4())) {
                    contexts.add(activityTopicHouTaiVo.getContext4());
                }
                if (contexts.size() < 2) {
                    return Result.newFaild("选择题的选项不能少于2个");
                }
                // 选项再赋值
                Iterator<String> iterator = contexts.iterator();
                int index = 1;
                while (iterator.hasNext()) {
                    if (1 == index) {
                        activityTopicHouTaiVo.setContext1(iterator.next());
                        activityTopicHouTaiVo.setContext2(null);
                        activityTopicHouTaiVo.setContext3(null);
                        activityTopicHouTaiVo.setContext4(null);
                    }
                    if (2 == index) {
                        activityTopicHouTaiVo.setContext2(iterator.next());
                    }
                    if (3 == index) {
                        activityTopicHouTaiVo.setContext3(iterator.next());
                    }
                    if (4 == index) {
                        activityTopicHouTaiVo.setContext4(iterator.next());
                    }
                    index++;
                }
            } else if (1 == activityTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getTContext1()) && StringUtils.isEmpty(activityTopicHouTaiVo.getTContext2())) {
                    return Result.newFaild("填空题至少有一个选项");
                }
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getTContext1())) {
                    activityTopicHouTaiVo.setTContext1(activityTopicHouTaiVo.getTContext2());
                    activityTopicHouTaiVo.setTContext2(null);
                }
            } else if (2 == activityTopicHouTaiVo.getTopicType()) {
                // 判断题
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getPContext())) {
                    return Result.newFaild("判断题答案不能为空");
                }
            } else {
                return Result.newFaild("题目类型只能为0/1/2");
            }

            // 新增或修改活动赛题目
            Result result = activityTopicService.editActivityTopic(activityTopicHouTaiVo);
            return result;
        } catch (Exception e) {
            log.error("新增或修改活动赛题目出错，错误信息: {}",e);
            return Result.newFail("新增或修改活动赛题目出错");
        }
    }


    /**
     * 批量导入活动赛题目（二期）
     * @param file 导入选择题excel文件
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @param activityId 活动赛id
     * @return 导入选择题信息成功与否
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("inputActivityFromExcel")
    @ResponseBody
    public Result inputXuanZeExcel(@RequestParam("file") MultipartFile file,
                                   // @RequestParam(value = "type",required = false,defaultValue = "") Integer type,
                                   @RequestParam(value = "topicType",required = true,defaultValue = "0") Integer topicType,
                                   @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType,
                                   @RequestParam(value = "activityId",required = true,defaultValue = "1") Integer activityId) {
        if (topicType == null || companyType == null) {
            return Result.newFaild("题目类型、题目所属公司参数均不能为空");
        }
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //哪些题目保存出错
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                if (0 == topicType) {
                    // 选择题
                    topicError = activityTopicService.inputXuanZeExcel(is, 0,1, null, topicType, companyType, activityId);
                } else if (1 == topicType) {
                    // 填空题
                    topicError = activityTopicService.inputTianKongExcel(is, 0,1, null, topicType, companyType, activityId);
                } else if (2 == topicType) {
                    // 判断题
                    topicError = activityTopicService.inputPanDuanExcel(is, 0,1, null, topicType, companyType, activityId);
                } else {
                    return Result.newFail("导入题目类型错误");
                }
            } catch (IOException | InvalidFormatException e) {
                log.error("批量导入活动赛题目(选择题)出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 计算开始时间与结束时间时间差（小时）
     */
    private long reduceHours(String startTime, String endTime) throws Exception {
        Date start = DateUtil.parse(startTime);
        Date end = DateUtil.parse(endTime);
        return DateUtil.between(start, end, DateUnit.HOUR);
    }


    /**
     * 移入题目页面跳转
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:12 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @GetMapping("/editEnter")
    public String editEnter(Model model,
                            @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Activity activity = activityMapper.selectById(id);
        model.addAttribute("activity", activity);
        return "activity/editEnter";
    }

    /**
     * 移出题目页面跳转
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:12 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @GetMapping("/editCome")
    public String editCome(Model model,
                           @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Activity activity = activityMapper.selectById(id);
        model.addAttribute("activity", activity);
        return "activity/editCome";
    }

    /**
     * 条件查询没有在活动题库的题目
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:07 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/findEnterList")
    public @ResponseBody
    Result findEnterList(@RequestParam(value = "id", required = false, defaultValue = "") Long id,
                         @RequestParam(value = "type", required = false, defaultValue = "") Integer type,
                         @RequestParam(value = "topicType", required = false, defaultValue = "") Integer topicType,
                         @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                         @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        //根据活动赛事id，根据题库和题目类型条件查询没有在该活动下的题目展示

        List<ActivityTopic>  activityTopicList = activityTopicMapper.selectList(new QueryWrapper<ActivityTopic>().eq("activity_id",id));
        List<Integer> hasTipocIds = new ArrayList<Integer>();
        for(ActivityTopic activityTopic:activityTopicList){
            if(null != activityTopic.getBranchTopicId()){
                hasTipocIds.add(activityTopic.getBranchTopicId());
            }
        }

        IPage<BranchTopic> pages = branchTopicMapper.selectPage(new Page<BranchTopic>(page, limit),
                new QueryWrapper<BranchTopic>()
                        .eq(null !=type && 0!=type , "type", type)
                        .eq(null !=topicType , "topic_type", topicType)
                        .notIn(hasTipocIds.size()>0,"id",hasTipocIds));
        return ResultLayuiTable.newSuccess(pages.getTotal(),pages.getRecords());
    }

    /**
     * 条件查询在活动题库的题目
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:07 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/findComeList")
    public @ResponseBody
    Result findComeList(@RequestParam(value = "id", required = false, defaultValue = "") Long id,
//                        @RequestParam(value = "topicType", required = false, defaultValue = "") Integer topicType,
                        @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                        @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        //根据活动赛事id，根据题库和题目类型条件查询没有在该活动下的题目展示
        IPage<ActivityTopic>  pages = activityTopicMapper.selectPage(new Page<ActivityTopic>(page, limit),new QueryWrapper<ActivityTopic>()
                        .eq("activity_id",id));
        return ResultLayuiTable.newSuccess(pages.getTotal(),pages.getRecords());
    }

    /**
     * 题目的录入
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:59 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/topicEnter")
    @ResponseBody
    public Result topicEnter(@RequestParam(value = "ids", required = false, defaultValue = "") String[] ids,
                             @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        //根据赛事id，数组格式题目id进行录入
        if(null == id || ids.length == 0){
            return Result.newFail("未获取到参数信息");
        }

        Activity activity = activityMapper.selectById(id);
        if(null == activity){
            return Result.newFail("活动赛不存在");
        }

        for(String topicId: ids){
            BranchTopic branchTopic = branchTopicMapper.selectById(topicId);
            if(null == branchTopic){
                continue;
            }
            ActivityTopic activityTopic = new ActivityTopic();
            activityTopic.setActivityId(id.intValue());
            activityTopic.setTitle(branchTopic.getTitle());
            activityTopic.setType(branchTopic.getType());
            activityTopic.setTopicType(branchTopic.getTopicType());
            activityTopic.setCreateTime(new Date());
            activityTopic.setCorrectParse(branchTopic.getCorrectParse());
            activityTopic.setPoint(branchTopic.getTitle());
            activityTopic.setBranchTopicId(Integer.parseInt(topicId));
            activityTopicMapper.insert(activityTopic);

            if(0 == branchTopic.getTopicType()){
                List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId()));
                for(BranchOption branchOption : branchOptionList)   {
                    ActivityOption activityOption = new ActivityOption();
                    activityOption.setContent(branchOption.getContent());
                    activityOption.setCreateTime(new Date());
                    activityOption.setTopicId(activityTopic.getId());
                    activityOptionMapper.insert(activityOption);

                    if(branchOption.getId().intValue() == branchTopic.getCorrectOptionId().intValue()){
                        activityTopic.setCorrectOptionId(activityOption.getId());
                        activityTopicMapper.updateById(activityTopic);
                    }
                }
            }else{
                List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId()));
                for(BranchOption branchOption : branchOptionList)   {
                    ActivityOption activityOption = new ActivityOption();
                    activityOption.setContent(branchOption.getContent());
                    activityOption.setCreateTime(new Date());
                    activityOption.setTopicId(activityTopic.getId());
                    activityOptionMapper.insert(activityOption);
                }
            }
        }

        return Result.newSuccess("添加成功");
    }

    /**
     * 题目的移出
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:00 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/topicCome")
    @ResponseBody
    public Result topicCome(@RequestParam(value = "ids", required = false, defaultValue = "") String[] ids,
                            @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        //根据赛事id，数组格式题目id进行移出
        //根据赛事id，数组格式题目id进行录入
        if(null == id || ids.length == 0){
            return Result.newFail("未获取到参数信息");
        }

        Activity activity = activityMapper.selectById(id);
        if(null == activity){
            return Result.newFail("活动赛不存在");
        }

        for(String topicId: ids){
            activityOptionMapper.delete(new QueryWrapper<ActivityOption>().eq("topic_id",topicId));
            activityTopicMapper.delete(new QueryWrapper<ActivityTopic>().eq("id",topicId));
        }
        return Result.newSuccess("删除成功");
    }


    /**
     * 移入题目页面跳转
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:12 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @GetMapping("/activityRankInit")
    public String activityRankInit(Model model,
                            @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Activity activity = activityMapper.selectById(id);
        model.addAttribute("activity", activity);
        return "activity/rank";
    }


    @Autowired
    private UserActivityMapper userActivityMapper;

    /**
     * 活动赛排行榜信息
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:17:00 2020/6/29
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/activityRankList")
    @ResponseBody
    public Result activityRankList(@RequestParam(value = "id", required = false, defaultValue = "") Long id,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                   @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        if(null == id){
            return Result.newFail("未获取到参数信息");
        }

        Activity activity = activityMapper.selectById(id);
        if(null == activity){
            return Result.newFail("活动赛不存在");
        }

        IPage<UserActivity> pages = userActivityMapper.selectPage(new Page<UserActivity>(page, limit),new QueryWrapper<UserActivity>().eq("activity_id",id.intValue()).orderByDesc("score")
                .orderByAsc("submit_time"));
        List<UserActivityVo> userActivityVoList = new ArrayList<UserActivityVo>();
        int i = 0;
        if(page.intValue() == 1){
            i = 1;
        }else{
            i = page * 10 - 9;
        }

        for(UserActivity userActivity : pages.getRecords()){
            UserActivityVo userActivityVo =new UserActivityVo();
            BeanUtils.copyProperties(userActivity,userActivityVo);
            userActivityVo.setRankNo(i);
            i++;
            userActivityVoList.add(userActivityVo);
        }
        return ResultLayuiTable.newSuccess(pages.getTotal(),userActivityVoList);
    }

    // @RequestMapping("/12138")
    @ResponseBody
    public void test01() {
        List<ActivityTopic> list = activityTopicService.list();
        for (ActivityTopic activityTopic : list) {
            if (activityTopic.getId() % 5 == 0) {
                activityTopic.setVideoUrl("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
            }
            if (activityTopic.getId() % 5 == 1) {
                activityTopic.setImageUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fup.enterdesk.com%2Fedpic_source%2F6d%2F1a%2F35%2F6d1a35f4634729a4a3e1a155093531a5.jpg");
            }
            if (activityTopic.getId() % 5 == 2) {
                activityTopic.setImageUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fwww.cbt.com.cn%2Frw%2Fgslx%2F201903%2FW020190323705565932858.jpg");
            }
            if (activityTopic.getId() % 5 == 3) {
                activityTopic.setImageUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg5.51tietu.net%2Fpic%2F2019-081921%2F2h3nlenxqkb2h3nlenxqkb.jpg");
            }
            if (activityTopic.getId() % 5 == 4) {
                activityTopic.setImageUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fdingyue.nosdn.127.net%2FE07sNTLmW7ucFoCtnBN2yifwb65dQL79e8W9gX0Y8NAyJ1540377409200compressflag.jpg");
            }
            activityTopicService.updateById(activityTopic);
        }
    }


    /**
     * 活动赛查看题目页面跳转（二期）
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/select")
    public String select(Model model,
                         @RequestParam(value = "activityId",required = false,defaultValue = "") Integer activityId,
                         @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        if (null == activityId) {
            throw new BusinessException("活动赛id不能为空");
        }
        model.addAttribute("companyType",companyType);
        model.addAttribute("activityId",activityId);
        if (1 == companyType) {
            return "activity/aSelect";
        } else if (2 == companyType) {
            return "activity/bSelect";
        } else if (3 == companyType) {
            return "activity/cSelect";
        }

        return null;
    }


    /**
     * 获取活动赛下题目（二期）
     * @param title 题目名称（用于模糊搜索）
     * @param page 页码
     * @param limit 每页数据量
     * @param activityId 活动赛id
     * @param companyType 所属公司类型
     * @return 活动赛下题目集合
     */
    @RequiresPermissions("system:activity:view")
    @RequestMapping("/selectActivityTopicPage")
    @ResponseBody
    public Result selectActivityTopicPage(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                                          // @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                          @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                          @RequestParam(value = "activityId",required = false,defaultValue = "18") Integer activityId,
                                          @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        try {
            if (null == activityId && 0 == activityId) {
                throw new BusinessException("活动赛id不能为空");
            }

            // 获取活动赛下题目
            Result result = activityService.selectActivityTopicPage(title, page, limit, activityId, companyType);
            return result;
        } catch (Exception e) {
            log.error("获取活动赛下题目出错，错误信息: {}",e);
            return Result.newFail("获取活动赛下题目出错");
        }
    }


    /**
     * 手动新增或查看题目初始化（二期）
     * @param model 数据模型
     * @param activityId 活动赛id
     * @param companyType 所属公司分类
     * @param topicId 活动赛题目id
     * @return 页面跳转及数据
     */
    @RequiresPermissions("system:activity:view")
    @GetMapping("/insertOrEditInit")
    public String insertOrEditInit(Model model,
                                   @RequestParam(value = "activityId", required = false, defaultValue = "") Integer activityId,
                                   @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                   @RequestParam(value = "topicId", required = false, defaultValue = "") Integer topicId) {
        try {
            HashMap<String,Object> map = activityService.insertOrEditInit(activityId, companyType, topicId);
            model.addAttribute("isEdit",topicId!=null);
            model.addAttribute("topicId",topicId);
            model.addAttribute("companyType",companyType);
            model.addAttribute("activityTopic",map.get("activityTopic"));
            model.addAttribute("activityId",activityId);

            return "activitytopicedit/insertOrEdit";
        } catch (Exception e) {
            log.error("手动新增或查看活动赛题目初始化出错，错误信息: {}",e);
            throw new BusinessException("手动新增或查看题目初始化异常");
        }
    }


    /**
     * 删除部门下特定题目（单一道题、二期）
     * @param id 题目id
     * @return 删除题目成功与否
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/delTopic")
    @ResponseBody
    public Result delTopic(@RequestParam(value = "id", required = true) Integer id) {
        if (id == 0) {
            return Result.newFail("删除题目id不能为空！");
        }
        try {
            activityService.delTopic(id);
        } catch (Exception e) {
            log.error("删除活动赛下特定题目出错，错误信息: {}",e);
            return Result.newFail("删除活动赛下特定题目出错");
        }

        return Result.newSuccess("删除部门下特定题目成功");
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     * @param ids 选中题目id数组
     * @return 删除选中多道题成功与否
     */
    @RequiresPermissions("system:activity:view")
    @PostMapping("/deleteTopics")
    @ResponseBody
    public Result deleteTopics(@RequestParam(value = "ids",required = true) Integer[] ids) {
        if (ids.length == 0) {
            return Result.newFail("批量删除操作，题目勾选不能为空！");
        }
        try {
            activityService.deleteTopics(ids);
        } catch (Exception e) {
            log.error("批量删除部门下选定题目出错，错误信息: {}",e);
            return Result.newFail("批量删除部门下选定题目出错");
        }

        return Result.newSuccess("删除部门下特定题目成功");
    }
}
