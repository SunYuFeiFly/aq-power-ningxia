package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.entity.Notice;
import com.wangchen.entity.User;
import com.wangchen.service.NoticeService;
import com.wangchen.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告信息
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
@RequestMapping("/api/notice")
public class NoticeApi {

    @Autowired
    private UserService userService;

    @Autowired
    private NoticeService noticeService;


    /**
     * 查询公告信息（二期）
     * @param openId 用户id
     * @return 公告信息
     */
    @PostMapping("/getNoticeList")
    @ResponseBody
    public Result getAlertTipsList(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }

            // 获取所属公司分类下公告信息集合
            List<Notice> noticeList = noticeService.getAlertTipsList(user.getType());
            return Result.newSuccess(noticeList);
        }catch (Exception e){
            log.error("获取员工所属公司分类下公告信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

}