package com.wangchen.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.entity.User;
import com.wangchen.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能：额外小功能接口（获取塔币区间分布数据）
 */

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/temporary")
public class ExtraController {

    @Autowired
    private UserService userService;

    /**
     * 获取塔币区间分布数据
     * @param startCoinNumber 塔币分布区间查询 开始值
     * @param section 塔币分布区间查询 区间差值
     * @return 塔币区间分布数据
     */
    @RequiresPermissions("tsystem:user:view")
    @RequestMapping("/testCoinStatistics")
    @ResponseBody
    public String testCoinStatistics(@RequestParam(value = "startCoinNumber", required = true, defaultValue = "0") String startCoinNumber,
                                     @RequestParam(value = "section", required = true, defaultValue = "100") String section) {
        if (Integer.parseInt(startCoinNumber) < 0) {
            System.out.println("塔币分布查询开始值不能小于零");
        }
        if (Integer.parseInt(section) < 1) {
            System.out.println("塔币分布查询区间值不能小于1");
        }
        // 对开始结束数据做判断
        List<User> userList = userService.list(new QueryWrapper<User>().eq("deleted", 0));
        if (CollUtil.isNotEmpty(userList)) {
            List<Integer> coilList = userList.stream().map(User::getAllCoin).collect(Collectors.toList());
            // 计算区间范围最大值最小值
            double min = Math.floor(Double.valueOf(startCoinNumber) / Double.valueOf(section) * Double.valueOf(section)) ;
            double max = Math.floor(Double.valueOf(Collections.max(coilList)) / Double.valueOf(section) * Double.valueOf(section)) ;

            Map<String, String> map =  new LinkedHashMap<String, String>();
            for (; min < max; ) {
                int count = 0;
                int minS = (int) min;
                int maxS = (int) (min + Double.valueOf(section));
                for (User user : userList) {
                    if (user.getAllCoin() >= (minS + 1) && user.getAllCoin() <= maxS) {
                        count ++;
                    }
                }
                min += Double.valueOf(section);
                map.put((minS + 1) + "-" + maxS, count + "\n");
            }

            String str = new String();
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String index = (String) iterator.next();
                str += index + " " + map.get(index) + "\n";
            }

            return str;
        }

        return null;
    }
}
