package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.entity.Honor;
import com.wangchen.entity.Level;
import com.wangchen.entity.User;
import com.wangchen.entity.UserHonor;
import com.wangchen.service.HonorService;
import com.wangchen.service.LevelService;
import com.wangchen.service.UserHonorService;
import com.wangchen.service.UserService;
import com.wangchen.vo.HonorVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户段位信息
 */

@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/honor")
public class UserHonorApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private HonorService honorService;

    @Autowired
    private LevelService levelService;


    /**
     * 获取用户称号段位信息 (二期，补全未达到的等级)
     * @param openId 用户id
     * @Return 用户称号段位信息
     */
    @PostMapping("/getHonorList")
    @ResponseBody
    public Result getHonorList(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            // 当前拥有经验
            Integer presentExperience = user.getPresentExperience();
            //所有称号
            List<Honor> honorList = honorService.list(new QueryWrapper<Honor>());
            //vo里面加了两个值
            List<HonorVo> honorVoList = new ArrayList<HonorVo>();
            //称号
            List<UserHonor> userHonorList = userHonorService.list(new QueryWrapper<UserHonor>().eq("open_id",openId));
            if(CollectionUtils.isEmpty(userHonorList)){
                for (int i = 0; i < honorList.size(); i++) {
                    HonorVo honorVo = new HonorVo();
                    BeanUtils.copyProperties(honorList.get(i),honorVo);
                    honorVo.setIsHas(0);
                    honorVoList.add(honorVo);
                    // 计算达到本称号所需经验
                    Level level = levelService.getOne(new QueryWrapper<Level>().eq("level", (i + 1) * 10));
                    String condition = "达到本称号还需要经验：" + (level.getValue() - presentExperience);
                    honorVo.setCondition(condition);
                }
            }else{
                // 查询总的称号数
                List<Honor> honors = honorService.list(new QueryWrapper<Honor>());
                Set<Integer> userHonorSet = new LinkedHashSet<Integer>();
                for(UserHonor userHonor : userHonorList){
                    userHonorSet.add(userHonor.getHonorId());
                }
                for (int i = 0; i < honorList.size(); i++) {
                    HonorVo honorVo = new HonorVo();
                    BeanUtils.copyProperties(honorList.get(i),honorVo);
                    if(userHonorSet.contains(honorList.get(i).getId())){
                        honorVo.setIsHas(1);
                    } else {
                        honorVo.setIsHas(0);
                        // 计算达到本称号所需经验
                        Level level = levelService.getOne(new QueryWrapper<Level>().eq("level", (i + 1) * 10));
                        String condition = "达到本称号还需要经验：" + (level.getValue() - presentExperience);
                        honorVo.setCondition(condition);
                    }
                    honorVoList.add(honorVo);
                }
            }

            return Result.newSuccess(honorVoList);
        }catch (Exception e){
            log.error("获取用户称号段位信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }

}
