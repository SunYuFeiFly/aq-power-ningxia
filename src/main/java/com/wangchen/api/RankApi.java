package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.AllProvinceRank;
import com.wangchen.entity.CompanyPersonalRank;
import com.wangchen.entity.CompanyRank;
import com.wangchen.entity.User;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.vo.AllProvinceRankQianDuanLookVo;
import com.wangchen.vo.CompanyPersonalRankQianDuanLookVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 排行榜（全省个人排行榜、公司个人排行榜、公司排行榜）
 * @Package: com.wangchen.api
 * @ClassName: RankApi
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/23 14:20
 * @Version: 1.0
 */

@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/rank")
public class RankApi {

    @Autowired
    private UserService userService;

    @Autowired
    private AllProvinceRankService allProvinceRankService;

    @Autowired
    private CompanyPersonalRankService companyPersonalRankService;

    @Autowired
    private CompanyRankService companyRankService;

    @Autowired
    private UserHonorService userHonorService;


    /**
     * 全省个人排行列表页面数据 (二期 小程序)
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 查询全省个人排行榜数据集合
     */
    @PostMapping("/getAllProvinceRankList")
    @ResponseBody
    public Result getAllProvinceRankList(@RequestParam(value = "openId",required = false) String openId,
                                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                         @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                         @RequestParam(value = "type",required = true,defaultValue = "1") Integer type,
                                         @RequestParam(value = "cType",required = false,defaultValue = "1") Integer cType){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2");
            }
            if (cType > 3 && cType < 1) {
                throw new BusinessException("查看排行榜需求分类,只能为1/2/3");
            }
            if (user.getType() != 1) {
                cType = user.getType();
            }

            Map<String,Object> map = allProvinceRankService.getAllProvinceRankList(openId, page, limit, type, cType);
            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("小程序端获取全省个人排行列表页面数据出错，错误信息: {}",e);
            return Result.newFail();
        }
    }


    /**
     * 查询公司个人排行榜信息 （二期 小程序）
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 公司个人排行榜数据集合
     */
    @PostMapping("/getCompanyPersonalRankList")
    @ResponseBody
    public Result getCompanyPersonalRankList(@RequestParam(value = "openId",required = false) String openId,
                                             @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                             @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                             @RequestParam(value = "type",required = true,defaultValue = "1") Integer type,
                                             @RequestParam(value = "cType",required = false,defaultValue = "1") Integer cType){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2");
            }
            if (cType > 3 && cType < 1) {
                throw new BusinessException("查看排行榜需求分类,只能为1/2/3");
            }
            if (user.getType() != 1) {
                cType = user.getType();
            }
            // 查询所属公司总的排行信息
            Map<String,Object> map = companyPersonalRankService.getCompanyPersonalRankList(openId, page, limit, type, cType);

            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("小程序端获取公司个人排行榜信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }


    /**
     * 查询公司排行榜信息 (二期，小程序)
     * @param openId 用户id
     * @param page 页码
     * @param limit 每页数据量
     * @param type 排行类型：1：历史排行  2：年内排行
     * @param cType 查看排行榜需求分类（1:自有公司，2代维公司，3设计和监理公司）
     * @return 公司排行榜数据集合
     */
    @PostMapping("/getCompanyRankList")
    @ResponseBody
    public Result getCompanyRankList(@RequestParam(value = "openId",required = false) String openId,
                                     @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                     @RequestParam(value = "type",required = true,defaultValue = "1") Integer type,
                                     @RequestParam(value = "cType",required = true,defaultValue = "1") Integer cType){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if (!(1== type || 2 == type)) {
                throw new BusinessException("请传递合适的排行类型数据,只能为1/2");
            }
            if (cType > 3 && cType < 1) {
                throw new BusinessException("查看排行榜需求分类,只能为1/2/3");
            }
            if (user.getType() != 1) {
                cType = user.getType();
            }
            // 公司排行榜信息查询
            Map<String,Object> map = companyRankService.getCompanyRankList(openId, page, limit, type, cType);

            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("getCompanyRankList error:{}", e);
            log.error("小程序端获取公司排行榜信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

}