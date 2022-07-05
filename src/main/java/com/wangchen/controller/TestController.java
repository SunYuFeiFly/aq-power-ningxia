package com.wangchen.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.vo.OneVsOneTopicVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.runners.Parameterized;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private UserBranchService userBranchService;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityTopicService activityTopicService;

    @Autowired
    private ActivityOptionService activityOptionService;

    // @RequestMapping("match")
    private Result match() throws IOException {
        int BlueCompanyType = 1;
        int RedCompanyType = 1;
        // *****现修改为AB对战人员各自所属部门一获取题目3题，必知必会题目2题
        // 用于保存专业题
        List<BranchTopic> branchTopicList = new ArrayList<>();
        // 用于保存必知必会题
        List<FeiBranchTopic> feiBranchTopicList = new ArrayList<>();
        // 获取AB对战人员所属部门
        // *****  用于测试获取题目******
        String blueOpenId = "odGvG4i9VSvfaGQ6ARKj0a8lmpIs";
        String redOpenId = "odGvG4gYk9dsD89Wusf17LOaEeu4";
        // *****  用于测试获取题目******
        // 获取对战双方所属部门(只需要获取部门一)
        int blueBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", blueOpenId).orderByAsc("id")).get(0).getBranchId();
        int redBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", redOpenId).orderByAsc("id")).get(0).getBranchId();
        // 获得用户部门对应答题部门id
        int blueBranchId01 = Constants.newBranchTypeMap.get(blueBranchId)[1];
        int redBranchId01 = Constants.newBranchTypeMap.get(redBranchId)[1];
        if (BlueCompanyType == RedCompanyType) {
            // 所属公司类型相同
            if (blueBranchId01 == redBranchId01) {
                // 对战双方答题专业题库相同（6道题）
                if (0 == blueBranchId01) {
                    // 如果对战双方是公司领导，则在专业题库选6题
                    List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(6, 1);
                    branchTopicList.addAll(currBranchTopicList);
                } else {
                    // 对战双方不是公司领导，在答题题库选6题
                    List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 6);
                    branchTopicList.addAll(currBranchTopicList);
                }
            } else {
                if (0 == blueBranchId01) {
                    // 如果蓝色对战方是公司领导，则在专业题库选3题
                    List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                    branchTopicList.addAll(blueBranchTopicList);
                } else {
                    // 如果蓝色对战方不是是公司领导，在答题题库选3题
                    List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                    branchTopicList.addAll(blueBranchTopicList);
                }
                if (0 == redBranchId01) {
                    // 如果红色对战方是公司领导，则在专业题库选3题
                    List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                    branchTopicList.addAll(redBranchTopicList);
                } else {
                    // 如果红色对战方不是是公司领导，在答题题库选3题
                    List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                    branchTopicList.addAll(redBranchTopicList);
                }
            }
            // 必知必会4道题
            List<FeiBranchTopic> currFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(4, 1);
            feiBranchTopicList.addAll(currFeiBranchTopicList);
        } else {
            // 所属公司类型不同（所属公司下每人3道专业题，2道必知必会题）
            if (0 == blueBranchId01) {
                // 如果蓝色对战方是公司领导，则在专业题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, BlueCompanyType);
                branchTopicList.addAll(currBranchTopicList);
            } else {
                // 蓝色对战方不是公司领导，在答题题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                branchTopicList.addAll(currBranchTopicList);
            }
            if (0 == redBranchId01) {
                // 如果蓝色对战方是公司领导，则在专业题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, RedCompanyType);
                branchTopicList.addAll(currBranchTopicList);
            } else {
                // 蓝色对战方不是公司领导，在答题题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                branchTopicList.addAll(currBranchTopicList);
            }

            // 必知必会4道题
            List<FeiBranchTopic> blueFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, BlueCompanyType);
            List<FeiBranchTopic> redFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, RedCompanyType);
            feiBranchTopicList.addAll(blueFeiBranchTopicList);
            feiBranchTopicList.addAll(redFeiBranchTopicList);
        }
        // 判断获取题目是否满足需求
        if (6 != branchTopicList.size() || 4 != feiBranchTopicList.size()) {
            log.error("题目数量不够");
            return null;
        }

        List<OneVsOneTopicVo> topicList = new ArrayList<OneVsOneTopicVo>();
        int rankNo = 1;
        // 处理获取题目信息(专业题)
        for (BranchTopic branchTopic : branchTopicList) {
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(branchTopic, oneVsOneTopicVo);
            oneVsOneTopicVo.setTypeName(Constants.newBuMenKu.get(branchTopic.getType())[1]);
            oneVsOneTopicVo.setRankNo(rankNo++);
            List<BranchOption> currBranchOptionList = branchOptionService.list(new QueryWrapper<BranchOption>().eq("topic_id", branchTopic.getId()));
            List<BranchOption> branchOptionList = new ArrayList<BranchOption>();
            for (BranchOption branchOption : currBranchOptionList) {
                if (2 == branchTopic.getTopicType()) {
                    BranchOption currBranchOption = new BranchOption();
                    BeanUtils.copyProperties(branchOption, currBranchOption);
                    String currStr = branchOption.getContent();
                    if ("对".equals(currStr)) {
                        currBranchOption.setContent("正确");
                    } else {
                        currBranchOption.setContent("错误");
                    }
                    branchOptionList.add(currBranchOption);
                } else {
                    branchOptionList.add(branchOption);
                }
            }
            oneVsOneTopicVo.setOptionList(branchOptionList);
            topicList.add(oneVsOneTopicVo);
        }

        for (OneVsOneTopicVo topicVo : topicList) {
            System.out.println("topicVo:" + topicVo);
        }

        // 处理获取题目信息(必知必会题)
        rankNo = topicList.size();
        for (FeiBranchTopic feiBranchTopic : feiBranchTopicList) {
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(feiBranchTopic, oneVsOneTopicVo);
            oneVsOneTopicVo.setRankNo(++rankNo);
            List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>().eq("topic_id", feiBranchTopic.getId()));
            List<BranchOption> branchOptionList = new ArrayList<>();
            if (!CollUtil.isEmpty(feiBranchOptionList)) {
                for (FeiBranchOption feiBranchOption : feiBranchOptionList) {
                    BranchOption branchOption = new BranchOption();
                    BeanUtils.copyProperties(feiBranchOption, branchOption);
                    // 对每日答题判断题返回"正确"、"错误"信息处理
                    if (2 == feiBranchTopic.getTopicType()) {
                        if ("对".equals(feiBranchOption.getContent())) {
                            branchOption.setContent("正确");
                        } else {
                            branchOption.setContent("错误");
                        }
                    }
                    branchOptionList.add(branchOption);
                }
            }
            oneVsOneTopicVo.setOptionList(branchOptionList);
            topicList.add(oneVsOneTopicVo);
        }

        for (OneVsOneTopicVo topicVo : topicList) {
            System.out.println("topicVo:" + topicVo);
        }

        return Result.newSuccess(topicList);
    }

    /**
     * 补全之前用户注册未填充创建时间疏漏
     */
    @RequestMapping("completeUserInfo")
    public String completeUserInfo() {
        List<User> userList = userService.list();
        for (User user : userList) {
            user.setCreateDate(user.getRegisteredTime());
            userService.updateById(user);
        }

        return "12138";
    }


    /**
     * 将已经过期的活动赛题目移至应知应会题库
     */
    // @RequestMapping("addTopic")
    public String addTopic(@RequestParam("activityId") Integer activityId) {
        // 活动赛结束后将活动赛题目导入到应知应会
        List<ActivityTopic> activityTopicList = activityTopicService.list(new QueryWrapper<ActivityTopic>().eq("activity_id", activityId));
        if (!CollUtil.isEmpty(activityTopicList)) {
            for (ActivityTopic activityTopic : activityTopicList) {
                FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
                BeanUtils.copyProperties(activityTopic, feiBranchTopic);
                // BeanUtils 复制的时候null不拷贝，需单独处理
                if (null == activityTopic.getImageUrl()) {
                    feiBranchTopic.setImageUrl(null);
                }
                if (null == activityTopic.getVideoUrl()) {
                    feiBranchTopic.setVideoUrl(null);
                }
                // 主键设置为null
                feiBranchTopic.setId(null);
                // 正确选项id置空
                feiBranchTopic.setCorrectOptionId(null);
                feiBranchTopicService.save(feiBranchTopic);

                // 正确选项id
                int correctOptionId = 0;
                // 获取题目对应答案
                List<ActivityOption> activityOptionList = activityOptionService.list(new QueryWrapper<ActivityOption>().eq("topic_id", activityTopic.getId()));
                if (!CollUtil.isEmpty(activityOptionList)) {
                    for (ActivityOption activityOption : activityOptionList) {
                        FeiBranchOption feiBranchOption = new FeiBranchOption();
                        BeanUtils.copyProperties(activityOption, feiBranchOption);
                        // 主键设置为null
                        feiBranchOption.setId(null);
                        feiBranchOption.setTopicId(feiBranchTopic.getId());
                        feiBranchOptionService.save(feiBranchOption);
                        // 选择题需要反馈相应的正确选项id
                        if (0 == activityTopic.getTopicType()) {
                            if (activityTopic.getCorrectOptionId().equals(activityOption.getId())) {
                                correctOptionId = feiBranchOption.getId();
                            }
                        }
                    }
                }
                // 如果是选择题，需要更新正确选项id
                if (0 == activityTopic.getTopicType()) {
                    feiBranchTopic.setCorrectOptionId(correctOptionId);
                    feiBranchTopicService.updateById(feiBranchTopic);
                }
            }
        }
        return "12138";
    }

    /**
     * 重排应知应会主键id，使之与专业题id不会发生重合现状(应知应会题目主键id增100000)
     */
    @RequestMapping("resetFeiTopicId")
    public String resetFeiTopicId() {
        // 获取所有应知应会题目
        List<FeiBranchTopic> FeiBranchTopicList = feiBranchTopicService.list(new QueryWrapper<FeiBranchTopic>());
        for (FeiBranchTopic feiBranchTopic : FeiBranchTopicList) {
            feiBranchTopic.setId(feiBranchTopic.getId() + 100000);
            feiBranchTopicService.saveOrUpdate(feiBranchTopic);
        }

        // 获取所有应知应会题目选项
//        List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>());
//        for (FeiBranchOption feiBranchOption : feiBranchOptionList) {
//            feiBranchOption.setTopicId(feiBranchOption.getTopicId() + 100000);
//            feiBranchOptionService.updateById(feiBranchOption);
//        }

        return "12138";
    }
}
