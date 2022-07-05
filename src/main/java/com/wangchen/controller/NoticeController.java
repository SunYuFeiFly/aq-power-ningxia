package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.Notice;
import com.wangchen.mapper.NoticeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Date;

/**
 * <p>
 * 公告信息 - 前端控制器
 * </p>
 *
 * @author yinguang
 * @since 2020-07-01
 */

@Slf4j
@Controller
@RequestMapping("/system/notice")
public class NoticeController {
    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 进入页面
     *
     * @return
     */
    @RequiresPermissions("system:notice:view")
    @RequestMapping("/list")
    public String list(Model model) {
        return "notice/list";
    }


    /**
     * 编辑页面
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/6/30
     */
    @RequiresPermissions("system:notice:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Notice notice = noticeMapper.selectById(id);

        model.addAttribute("isEdit", id != null);
        model.addAttribute("notice", notice);
        return "notice/edit";
    }


    /**
     * 列表页面
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:19 2020/6/30
     */
    @RequiresPermissions("system:notice:view")
    @PostMapping("selectPages")
    public @ResponseBody
    Result selectPages(Model model,
                       @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                       @RequestParam(value = "title", required = false, defaultValue = "") String title) {
        try {
            IPage<Notice> pages = noticeMapper.selectPage(new Page<Notice>(page, limit),
                    new QueryWrapper<Notice>().like(StringUtils.isNotEmpty(title), "title", title).orderByDesc("id"));
            return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
        } catch (Exception e) {
            log.error("获取公告列表数据出错，错误信息: {}",e);
            return Result.newFaild("获取公告列表数据出错");
        }
    }


    /**
     * 新增修改公告列表数据
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:15:20 2020/6/30
     */
    @RequiresPermissions("system:notice:view")
    @RequestMapping("/editNotice")
    @ResponseBody
    public Result editNotice(Notice notice) {
        if (notice.getId() == null) {
            notice.setCreateTime(new Date());
            noticeMapper.insert(notice);
        } else {
            notice.setUpdateTime(new Date());
            noticeMapper.updateById(notice);
        }
        //实例化赛事，根据id是否为空判断新增或者修改
        return Result.newSuccess(null == notice.getId() ? "新增公告列表数据成功" : "修改公告列表数据成功");
    }

    /**
     * 删除信息
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:notice:view")
    @PostMapping("/deleteNotice")
    public @ResponseBody
    Result deleteNotice(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        try {
            Notice notice = noticeMapper.selectById(id);
            noticeMapper.delete(new QueryWrapper<Notice>().eq("id", id));
            File templateFile = new File(notice.getUrl());
            if(templateFile != null && templateFile.exists()) {
                templateFile.delete();
            }
        } catch (Exception e) {
            log.error("删除公告信息数据出错，错误信息: {}",e);
            return Result.newFail("删除公告信息数据出错");
        }
        return Result.newSuccess();
    }


}

