package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.entity.Feedback;
import com.wangchen.entity.User;
import com.wangchen.service.FeedbackService;
import com.wangchen.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 用户反馈接口
 * @Package: com.wangchen.api
 * @ClassName: FeedbackApi
 * @Author: 2
 * @Description: zhangcheng
 * @Date: 2020/6/4 15:08
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/feedbackapi")
public class FeedbackApi {

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 查看用户反馈
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/queryFeedback")
    @ResponseBody
    public Result queryFeedback(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            List<Feedback> feedbackList = feedbackService.list(new QueryWrapper<Feedback>().eq("open_id",openId));
            return Result.newSuccess(feedbackList);
        }catch (Exception e){
            log.error("查看用户反馈信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

    /**
     * 保存用户反馈信息
     * @param openId 用户id
     * @param context 用户反馈内容
     * @param type 反馈人员所属公司类型
     * @return 保存用户反馈信息结果
     */
    @PostMapping("/saveFeedback")
    @ResponseBody
    public Result saveFeedback(@RequestParam(value = "openId",required = false) String openId,
                                @RequestParam(value = "context",required = false) String context,
                                @RequestParam(value = "type",required = false) Integer type){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            Feedback feedback = new Feedback();
            feedback.setOpenId(openId);
            feedback.setContext(context);
            feedback.setType(type);
            feedback.setCreateTime(new Date());
            feedback.setStatus(0);
            feedbackService.save(feedback);
            return Result.newSuccess("反馈成功");
        }catch (Exception e){
            log.error("保存用户反馈信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

}