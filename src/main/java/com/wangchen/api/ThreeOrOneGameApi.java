package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.User;
import com.wangchen.entity.UserOneVsOneLog;
import com.wangchen.entity.UserThreeTeamLog;
import com.wangchen.service.UserOneVsOneLogService;
import com.wangchen.service.UserService;
import com.wangchen.service.UserTeamVsTeamLogService;
import com.wangchen.service.UserThreeTeamLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @ProjectName: 查询是否完成1v1 或3v3
 * @Package: com.wangchen.api
 * @ClassName: ThreeOrOneGameApi
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/8/17 14:21
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@Controller
@Slf4j
@RequestMapping("/api/threeoronegame")
public class ThreeOrOneGameApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private UserTeamVsTeamLogService userTeamVsTeamLogService;


    /**
     * 查询用户当前今天完成1v1情况（二期用于显示完成情况和经验获得情况）
     * @param openId 用户id
     * @return 用户当前完成1v1情况
     * 规则：前五场 胜者：2经验2塔币 输者：1经验0塔币   后5场：胜者：2经验0塔币 输者：1经验0塔币
     */
    @ResponseBody
    @PostMapping(value = "/userIsOneVsOne")
    public Result userIsOneVsOne(@RequestParam(value = "openId", required = false, defaultValue = "") String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            Result Result = userOneVsOneLogService.userIsOneVsOne(openId);
            return Result;
        }catch (Exception e){
            log.error("查看1V1对战信息出错，错误信息: {}",e.getMessage());
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 查询用户当前今天完成3人团战情况
     * @param openId 用户id
     * @return 用户当前完成3人团战情况
     *
     */
    @ResponseBody
    @PostMapping(value = "/userIsThreeTeam")
    public Result userIsThreeTeam(Model model, @RequestParam(value = "openId", required = false, defaultValue = "") String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            Result Result = userTeamVsTeamLogService.userIsTeamVsTeam(openId);
            return Result;
        }catch (Exception e){
            log.error("查看3人团战对战信息出错，错误信息: {}",e.getMessage());
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


}