package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.entity.Feedback;
import com.wangchen.entity.User;
import com.wangchen.mapper.FeedbackMapper;
import com.wangchen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * <p>
 * 系统-反馈
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */

@Slf4j
@Controller
@RequestMapping("/system/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private UserService userService;

    /**
     * 进入页面
     */
    @RequiresPermissions("system:feedback:view")
    @RequestMapping("/list")
    public String list(Model model) {
        //查询所有
        long count = feedbackMapper.selectCount(new QueryWrapper<>());
        //查询未处理的
        long count0 = feedbackMapper.selectCount(new QueryWrapper<Feedback>().lambda()
                .eq(Feedback::getStatus, 0));
        //查询已处理的
        long count1 = feedbackMapper.selectCount(new QueryWrapper<Feedback>().lambda()
                .eq(Feedback::getStatus, 1));
        model.addAttribute("count", count);
        model.addAttribute("count0", count0);
        model.addAttribute("count1", count1);
        return "feedback/list";
    }


    /**
     * 编辑页面
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/6/30
     */
    @RequiresPermissions("system:feedback:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Feedback feedback = feedbackMapper.selectById(id);

        model.addAttribute("isEdit", id != null);
        model.addAttribute("feedback", feedback);
        return "feedback/edit";
    }


    /**
     * 反馈信息列表页面
     */
    @RequiresPermissions("system:feedback:view")
    @PostMapping("selectPages")
    public @ResponseBody
    Result selectPages(Model model,
                       @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                       @RequestParam(value = "status", required = false, defaultValue = "") String status,
                       @RequestParam(value = "type", required = false, defaultValue = "") Integer type) {
        try {
            IPage<Feedback> pages = feedbackMapper.selectPage(new Page<>(page, limit),
                    new QueryWrapper<Feedback>().eq(!StringUtils.isEmpty(status),"status", status)
                            .eq(null!=type,"type", type)
                                            .orderByDesc("create_time"));
            for (Feedback record : pages.getRecords()) {
                User user = userService.getUserByOpenId(record.getOpenId());
                record.setName(user.getName());
                record.setPhone(user.getMobile());
            }
            return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询反馈信息出错，错误信息: {}",e);
            return Result.newFaild("查询错误");
        }
    }


    /**
     * 新增修改反馈内容
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:15:20 2020/6/30
     */
    @RequiresPermissions("system:feedback:view")
    @RequestMapping("/editFeedback")
    @ResponseBody
    public Result editFeedback(Feedback feedback) {
        if (feedback.getId() == null) {
            feedback.setCreateTime(new Date());
            feedbackMapper.insert(feedback);
        } else {
            feedbackMapper.updateById(feedback);
        }
        //实例化赛事，根据id是否为空判断新增或者修改
        return Result.newSuccess();
    }

    /**
     * 反馈信息状态
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:11:11 2020/6/30
     */
    @RequiresPermissions("system:feedback:view")
    @PostMapping("/isStatus")
    public @ResponseBody
    Result isStatus(@RequestParam(value = "id", required = false, defaultValue = "") Integer id,
                        @RequestParam(value = "status", required = false, defaultValue = "") Integer status) {
        try {
            Feedback feedback = feedbackMapper.selectById(id);
            feedback.setStatus(status);
            feedbackMapper.updateById(feedback);
        } catch (Exception e) {
            log.error("查询反馈信息状态出错，错误信息: {}",e);
            return Result.newFail("查询反馈信息状态出错");
        }

        return Result.newSuccess();
    }

    /**
     * 删除反馈信息
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:feedback:view")
    @PostMapping("/deleteFeedback")
    public @ResponseBody
    Result deleteFeedback(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        try {
            Feedback feedback = feedbackMapper.selectById(id);
            feedbackMapper.delete(new QueryWrapper<Feedback>().eq("id", id));

        } catch (Exception e) {
            log.error("删除反馈信息出错，错误信息: {}",e);
            return Result.newFail("删除反馈信息出错");
        }

        return Result.newSuccess();
    }


}

