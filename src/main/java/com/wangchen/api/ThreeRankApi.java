package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.entity.User;
import com.wangchen.entity.UserThreeTeamLog;
import com.wangchen.entity.UserThreeTeamRank;
import com.wangchen.service.UserService;
import com.wangchen.service.UserThreeTeamLogService;
import com.wangchen.service.UserThreeTeamRankService;
import com.wangchen.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: 团队赛排行榜 3V3
 * @Package: com.wangchen.api
 * @ClassName: ThreeRankApi
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/8/4 16:28
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/threerank")
public class ThreeRankApi {

    @Autowired
    private UserService userService;
    @Autowired
    private UserThreeTeamRankService userThreeTeamRankService;
    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    /**
     * 查询团队赛排行榜信息
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/getThreeRankList")
    @ResponseBody
    public Result getAllProvinceRankList(@RequestParam(value = "openId",required = false) String openId,
                                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                         @RequestParam(value = "limit", required = false, defaultValue = "10") int limit){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }

            IPage<UserThreeTeamRank> userThreeTeamRankIPage =
                    userThreeTeamRankService.page(new Page<UserThreeTeamRank>(page, limit),
                            new QueryWrapper<UserThreeTeamRank>().eq("rank_date", DateUtils.getZuoTianDay()).orderByAsc("id"));
            Map<String,Object> map = new HashMap<String,Object>();

            List<UserThreeTeamRank> userThreeTeamRankList =userThreeTeamRankIPage.getRecords();


            map.put("rankList",userThreeTeamRankList);

            List<UserThreeTeamLog> userThreeTeamLogList =  userThreeTeamLogService.
                    list(new QueryWrapper<UserThreeTeamLog>().eq("create_date",DateUtils.getZuoTianDay()).eq("open_id",openId));

            if(CollectionUtils.isNotEmpty(userThreeTeamLogList)){
                UserThreeTeamRank userThreeTeamRank =  userThreeTeamRankService.getOne(new QueryWrapper<UserThreeTeamRank>()
                        .eq("room_id",userThreeTeamLogList.get(userThreeTeamLogList.size()-1).getRoomId()));
                map.put("userRankNo",null==userThreeTeamRank?1:userThreeTeamRank.getRankNo());
            }else{
                map.put("userRankNo","未参与");
            }
            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("查询团队赛排行榜信息出错，错误信息: {}",e);
            return Result.newFail(e.getMessage());
        }
    }


}