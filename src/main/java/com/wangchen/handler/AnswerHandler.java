//package com.wangchen.handler;
//
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.wangchen.entity.BranchOption;
//import com.wangchen.entity.BranchTopic;
//import com.wangchen.mapper.BranchOptionMapper;
//import com.wangchen.response.AnswerResponse;
//import com.wangchen.service.BranchTopicService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Slf4j
//@Component
//public class AnswerHandler {
//
//    @Autowired
//    private BranchTopicService branchTopicService;
//    @Autowired
//    private BranchOptionMapper branchOptionMapper;
//
//    public AnswerResponse getAnswerResponse() {
//        List<BranchTopic> branchTopics = branchTopicService.listTopicRandom(10);
//        for (BranchTopic topic : branchTopics) {
//            List<BranchOption>branchOptions = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().lambda()
//                    .eq(BranchOption::getTopicId,topic.getId()));
//            topic.setOptionList(branchOptions);
//        }
//        AnswerResponse answerResponse = new AnswerResponse();
//        answerResponse.setAnswerQuestionResponses(branchTopics);
//        return answerResponse;
//    }
//
//
//}
