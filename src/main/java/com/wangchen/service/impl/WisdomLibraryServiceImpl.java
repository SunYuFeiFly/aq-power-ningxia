package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.constant.UserConstants;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.*;
import com.wangchen.mapper.WisdomLibraryMapper;
import com.wangchen.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 铁塔智库表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-18
 */
@Service
public class WisdomLibraryServiceImpl extends ServiceImpl<WisdomLibraryMapper, WisdomLibrary> implements WisdomLibraryService {

    @Autowired
    private WisdomLibraryMapper wisdomLibraryMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private LevelService levelService;


    /**
     * 获取审核题目列表页面(二期)
     *
     * @param page        页码
     * @param limit       每页数据量
     * @param status      题目状态
     * @param type        题目所属部门id
     * @param topicType   题目所属类型
     * @param companyType 题目所属公司分类
     * @return 审核题目列表数据
     */
    @Override
    @Transactional
    public Result selectPages(int page, int limit, String status, Integer type, String topicType, String companyType) {
        IPage<WisdomLibrary> pages = wisdomLibraryMapper.selectPage(new Page<>(page, limit),
                new QueryWrapper<WisdomLibrary>().eq(!StringUtils.isEmpty(status), "status", status)
                        .eq(null != type, "bu_men_type", type).eq(!StringUtils.isEmpty(topicType), "topic_type", topicType)
                        .eq("company_type", companyType).orderByDesc("create_time"));
        for (WisdomLibrary record : pages.getRecords()) {
            User user = userService.getUserByOpenId(record.getOpenId());
            record.setName(user.getName());
            record.setBuMenTypeTxt(record.getTypeTxt());
            record.setTopicTypeTxtStr(record.getTopicTypeTxt());
        }
        return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
    }


    /**
     * 通过、驳回题目（二期）
     *
     * @param id          题目id
     * @param status      题目审核状态
     * @param tikuType    归属题库 0专业题库，1非专业题库
     * @param companyType 题目划分所属公司分类
     * @return 题目审核结果
     */
    @Override
    @Transactional
    public Result passStatus(Integer id, Integer status, Integer tikuType, Integer companyType) {
        WisdomLibrary wisdomLibrary = wisdomLibraryMapper.selectById(id);
        wisdomLibrary.setStatus(status);
        wisdomLibrary.setTikuType(tikuType);
        wisdomLibraryMapper.updateById(wisdomLibrary);

        if (tikuType.intValue() == 0) {
            // 专业题库
            BranchTopic branchTopic = new BranchTopic();
            branchTopic.setTitle(wisdomLibrary.getTitle());
            branchTopic.setPoint(wisdomLibrary.getTitle());
            branchTopic.setCorrectParse(formatCorrectParse(wisdomLibrary.getTitle()));
            branchTopic.setType(wisdomLibrary.getBuMenType());
            branchTopic.setCreateTime(new Date());
            branchTopic.setCompanyType(companyType);

            if (0 == wisdomLibrary.getTopicType()) {
                // 选择题
                branchTopic.setTopicType(0);
                branchTopicService.save(branchTopic);

                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 = false;
                boolean c3 = false;
                if (StringUtils.isEmpty(wisdomLibrary.getContext3())) {
                    c3 = true;
                }
                boolean c4 = false;
                if (StringUtils.isEmpty(wisdomLibrary.getContext4())) {
                    c4 = true;
                }
                for (int i = 0; i < 4; i++) {
                    if (i == num) {
                        BranchOption branchOption = new BranchOption();
                        branchOption.setTopicId(branchTopic.getId());
                        branchOption.setCreateTime(new Date());
                        branchOption.setContent(wisdomLibrary.getContext1());
                        branchOptionService.save(branchOption);

                        branchTopic.setCorrectOptionId(branchOption.getId());
                        branchTopicService.updateById(branchTopic);
                    } else {
                        if (!c2) {
                            BranchOption branchOption = new BranchOption();
                            branchOption.setTopicId(branchTopic.getId());
                            branchOption.setCreateTime(new Date());
                            branchOption.setContent(wisdomLibrary.getContext2());
                            branchOptionService.save(branchOption);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            BranchOption branchOption = new BranchOption();
                            branchOption.setTopicId(branchTopic.getId());
                            branchOption.setCreateTime(new Date());
                            branchOption.setContent(wisdomLibrary.getContext3());
                            branchOptionService.save(branchOption);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            BranchOption branchOption = new BranchOption();
                            branchOption.setTopicId(branchTopic.getId());
                            branchOption.setCreateTime(new Date());
                            branchOption.setContent(wisdomLibrary.getContext4());
                            branchOptionService.save(branchOption);
                            c4 = true;
                            continue;
                        }
                    }
                }
            }
            if (1 == wisdomLibrary.getTopicType()) {
                // 填空题
                branchTopic.setTopicType(1);
                branchTopicService.save(branchTopic);

                if (StringUtils.isNotEmpty(wisdomLibrary.getContext1())) {
                    BranchOption branchOption = new BranchOption();
                    branchOption.setTopicId(branchTopic.getId());
                    branchOption.setCreateTime(new Date());
                    branchOption.setContent(wisdomLibrary.getContext1());
                    branchOptionService.save(branchOption);
                }
                if (StringUtils.isNotEmpty(wisdomLibrary.getContext2())) {
                    BranchOption branchOption = new BranchOption();
                    branchOption.setTopicId(branchTopic.getId());
                    branchOption.setCreateTime(new Date());
                    branchOption.setContent(wisdomLibrary.getContext2());
                    branchOptionService.save(branchOption);
                }
            }
            if (2 == wisdomLibrary.getTopicType()) {
                // 判断题
                branchTopic.setTopicType(2);
                branchTopicService.save(branchTopic);

                if (StringUtils.isNotEmpty(wisdomLibrary.getContext1())) {
                    BranchOption branchOption = new BranchOption();
                    branchOption.setTopicId(branchTopic.getId());
                    branchOption.setCreateTime(new Date());
                    branchOption.setContent(wisdomLibrary.getContext1());
                    branchOptionService.save(branchOption);
                }
            }
        } else {
            // 非专业题库
            FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
            feiBranchTopic.setTitle(wisdomLibrary.getTitle());
            feiBranchTopic.setPoint(wisdomLibrary.getTitle());
            feiBranchTopic.setCorrectParse(formatCorrectParse(wisdomLibrary.getTitle()));
            feiBranchTopic.setCreateTime(new Date());
            feiBranchTopic.setCompanyType(companyType);

            if (0 == wisdomLibrary.getTopicType()) {
                feiBranchTopic.setTopicType(0);
                feiBranchTopicService.save(feiBranchTopic);
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 = false;
                boolean c3 = false;
                if (StringUtils.isEmpty(wisdomLibrary.getContext3())) {
                    c3 = true;
                }
                boolean c4 = false;
                if (StringUtils.isEmpty(wisdomLibrary.getContext4())) {
                    c4 = true;
                }
                for (int i = 0; i < 4; i++) {
                    if (i == num) {
                        FeiBranchOption feiBranchOption = new FeiBranchOption();
                        feiBranchOption.setTopicId(feiBranchTopic.getId());
                        feiBranchOption.setCreateTime(new Date());
                        feiBranchOption.setContent(wisdomLibrary.getContext1());
                        feiBranchOptionService.save(feiBranchOption);

                        feiBranchTopic.setCorrectOptionId(feiBranchOption.getId());
                        feiBranchTopicService.updateById(feiBranchTopic);
                    } else {
                        if (!c2) {
                            FeiBranchOption feiBranchOption = new FeiBranchOption();
                            feiBranchOption.setTopicId(feiBranchTopic.getId());
                            feiBranchOption.setCreateTime(new Date());
                            feiBranchOption.setContent(wisdomLibrary.getContext2());
                            feiBranchOptionService.save(feiBranchOption);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            FeiBranchOption feiBranchOption = new FeiBranchOption();
                            feiBranchOption.setTopicId(feiBranchTopic.getId());
                            feiBranchOption.setCreateTime(new Date());
                            feiBranchOption.setContent(wisdomLibrary.getContext3());
                            feiBranchOptionService.save(feiBranchOption);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            FeiBranchOption feiBranchOption = new FeiBranchOption();
                            feiBranchOption.setTopicId(feiBranchTopic.getId());
                            feiBranchOption.setCreateTime(new Date());
                            feiBranchOption.setContent(wisdomLibrary.getContext4());
                            feiBranchOptionService.save(feiBranchOption);
                            c4 = true;
                            continue;
                        }
                    }
                }
            }
            if (1 == wisdomLibrary.getTopicType()) {
                feiBranchTopic.setTopicType(1);
                feiBranchTopicService.save(feiBranchTopic);

                if (StringUtils.isNotEmpty(wisdomLibrary.getContext1())) {
                    FeiBranchOption feiBranchOption = new FeiBranchOption();
                    feiBranchOption.setTopicId(feiBranchTopic.getId());
                    feiBranchOption.setCreateTime(new Date());
                    feiBranchOption.setContent(wisdomLibrary.getContext1());
                    feiBranchOptionService.save(feiBranchOption);
                }
                if (StringUtils.isNotEmpty(wisdomLibrary.getContext2())) {
                    FeiBranchOption feiBranchOption = new FeiBranchOption();
                    feiBranchOption.setTopicId(feiBranchTopic.getId());
                    feiBranchOption.setCreateTime(new Date());
                    feiBranchOption.setContent(wisdomLibrary.getContext2());
                    feiBranchOptionService.save(feiBranchOption);
                }
            }

            if (2 == wisdomLibrary.getTopicType()) {
                feiBranchTopic.setTopicType(2);
                feiBranchTopicService.save(feiBranchTopic);

                if (StringUtils.isNotEmpty(wisdomLibrary.getContext1())) {
                    FeiBranchOption feiBranchOption = new FeiBranchOption();
                    feiBranchOption.setTopicId(feiBranchTopic.getId());
                    feiBranchOption.setCreateTime(new Date());
                    feiBranchOption.setContent(wisdomLibrary.getContext1());
                    feiBranchOptionService.save(feiBranchOption);
                }
            }
        }
        // 为用户添加出题奖励(5经验值)
        String openId = wisdomLibrary.getOpenId();
        User user = userService.getOne(new QueryWrapper<User>().eq("open_id", openId));
        if (null != user) {
            // 先更新用户经验值
            user.setAllExperience(user.getAllExperience() + UserConstants.TITTLE_PASS_AWARD);
            user.setPresentExperience(user.getPresentExperience() + UserConstants.TITTLE_PASS_AWARD);
            user.setUpdateDate(new Date());
            userService.updateById(user);
            // 判断是否存在等级升级
            UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id", openId));
            Level level = levelService.getOne(new QueryWrapper<Level>().eq("id", userLevel.getLevelId() + 1));
            if (level.getValue() < user.getPresentExperience()) {
                userLevel.setLevelId(userLevel.getLevelId() + 1);
                userLevel.setLevelName((userLevel.getLevelId() - 1) + "级");
                userLevel.setNowExperience(0);
                userLevel.setUpdateTime(new Date());
                userLevelService.updateById(userLevel);
            }
        }

        return Result.newSuccess("通过、驳回题目操作成功");
    }

    /**
     * 转换tittle中（）为 @
     * @param title
     * @return
     */
    private String formatCorrectParse(String title) {
        // 用于保存tittle中"（"、"）"的位置
        Map<Integer, Integer> map = new HashMap<>();
        int index = 1;
        for (int i = 0; i < title.length(); i++) {
            char curr = title.charAt(i);
            if ('（' == curr || '(' == curr || '）' == curr || ')' == curr) {
                map.put(index++, i);
            }
        }
        StringBuilder currStr = new StringBuilder();
        if (map.size() > 0) {
            for (int i = 0; i < map.size() / 2; i++) {
                if (0 == i) {
                    currStr.append(title.substring(0, map.get(1)) + "@");
                } else {
                    currStr.append(title.substring(map.get(2 * i) + 1, map.get(2 * i + 1)) + "@");
                }
            }
        }

        return currStr.toString() + "。";
    }


    /**
     * 使题目变为废弃状态(二期)
     *
     * @param id     待审题目id
     * @param status 题目审核状态
     * @return 题目废弃实现情况
     */
    @Override
    public Result rejectStatus(Integer id, Integer status) {
        WisdomLibrary wisdomLibrary = wisdomLibraryMapper.selectById(id);
        wisdomLibrary.setStatus(status);
        wisdomLibraryMapper.updateById(wisdomLibrary);
        return Result.newSuccess("已使题目变为废弃状态");
    }
}
