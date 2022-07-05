package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.entity.HotLog;
import com.wangchen.entity.User;
import com.wangchen.service.HotLogService;
import com.wangchen.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热点列表 (就是主页上的 比如 某某某等级提升到了 10级  获得了什么成就)
 * @Package: com.wangchen.api
 * @ClassName: RankApi
 * @Author: 2
 * @Description: zhangcheng
 * @Date: 2020/6/23 14:20
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/hotlog")
public class HotLogApi {

    @Autowired
    private UserService userService;

    @Autowired
    private HotLogService hotLogService;

    /**
     * 查询最新用户获取到的成就 等级提升等热点信息
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/getHotLog")
    @ResponseBody
    public Result getHotLog(@RequestParam(value = "openId",required = false) String openId,
                                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                         @RequestParam(value = "limit", required = false, defaultValue = "10") int limit){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            Map<String,Object> map = new HashMap<String,Object>();
            IPage<HotLog> hotLogIPage = hotLogService.page(new Page<HotLog>(page, limit),new QueryWrapper<HotLog>()
                    .orderByDesc("id"));
            List<String> stringList= new ArrayList<String>();
            for(HotLog hotLog: hotLogIPage.getRecords()){
                stringList.add(hotLog.getRemarks());
            }

            return Result.newSuccess(stringList);
        }catch (Exception e){
            log.error("查询最新用户获取到的成就、等级提升等热点信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

}