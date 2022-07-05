package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.BranchOption;
import com.wangchen.entity.BranchTopic;
import com.wangchen.mapper.BranchOptionMapper;
import com.wangchen.mapper.BranchTopicMapper;
import com.wangchen.service.BranchTopicService;
import com.wangchen.vo.BranchTopicHouTaiVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 部门题库表 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */

@Slf4j
@Controller
@RequestMapping("/system/branchTopic")
public class BranchTopicController {

    @Autowired
    private BranchTopicMapper branchTopicMapper;

    @Autowired
    private BranchOptionMapper branchOptionMapper;

    @Autowired
    private BranchTopicService branchTopicService;

    /**
     * 进入题目页面初始化(二期)
     * @param model 数据模型
     * @param type 部门id
     * @param companyType 所属公司分类
     * @return 跳转页面
     */
    @RequiresPermissions("system:branchTopic:view")
    @GetMapping("/list")
    public String index(Model model,
                        @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                        @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType) {
        model.addAttribute("companyType",companyType);
        model.addAttribute("type",type);
        if (1 == companyType) {
            if (1 == type) {
                return "tongxintopic/list";
            } else if (2 == type) {
                return "hangyetopic/list";
            } else if (3 == type) {
                return "nengyuantopic/list";
            } else if (4 == type) {
                return "yunyingtopic/list";
            } else if (5 == type) {
                return "zonghetopic/list";
            } else if (6 == type) {
                return "caiwutopic/list";
            } else if (7 == type) {
                return "renlitopic/list";
            } else if (8 == type) {
                return "jiweitopic/list";
            } else if (9 == type) {
                return "dangquntopic/list";
            } else if (10 == type) {
                return "shangwutopic/list";
            } else if (11 == type) {
                return "jishutopic/list";
            }
        } else if (2 == companyType) {
            // 暂时没有B类公司
        } else if (3 == companyType) {
            // 暂时没有C类公司
        }

        return null;
    }


    /**
     * 条件查询获取对应公司部门题目合集 （二期）
     * @param title 题目内容，用于模糊搜索
     * @param type 部门类型
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 所属公司类型
     * @return 所属公司部门下题目合集
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("selectPages")
    @ResponseBody
    public Result selectPages(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                              @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                              @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                              @RequestParam(value = "companyType",required = false,defaultValue = "1") Integer companyType) {
        try {
            // 条件查询获取对应公司部门题目合集
            Result result = branchTopicService.selectPages(title, type, page, limit, companyType);
            return result;
        } catch (Exception e) {
            log.error("条件查询获取对应公司部门题目合集出错，错误信息: {}", e.getMessage());
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR.getCode(), BusinessErrorMsg.BUSINESS_ERROR.getMsg());
        }
    }


    /**
     * 手动新增或查看题目初始化（二期）
     * @param model 数据模型
     * @param type 所属部门类型
     * @param companyType 所属公司分类
     * @param topicId 题目id
     * @return 页面跳转及数据
     */
    @RequiresPermissions("system:branchTopic:view")
    @GetMapping("/insertOrEditInit")
    public String insertOrEditInit(Model model,
                                   @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                                   @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                   @RequestParam(value = "topicId", required = false, defaultValue = "") Integer topicId) {
        if(null != topicId && 0 != topicId){
            BranchTopic branchTopic = branchTopicMapper.selectById(topicId);
            BranchTopicHouTaiVo branchTopicHouTaiVo = new BranchTopicHouTaiVo();
            branchTopicHouTaiVo.setTitle(branchTopic.getTitle());
            branchTopicHouTaiVo.setTopicType(branchTopic.getTopicType());
            branchTopicHouTaiVo.setCorrectParse(branchTopic.getCorrectParse());
            branchTopicHouTaiVo.setImageUrl(branchTopic.getImageUrl());
            branchTopicHouTaiVo.setVideoUrl(branchTopic.getVideoUrl());
            branchTopicHouTaiVo.setType(branchTopic.getType());

            List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId()));
            if(branchTopic.getTopicType() == 0){
                // 选择题
                for(BranchOption branchOption : branchOptionList){
                    if(branchTopic.getCorrectOptionId().intValue() == branchOption.getId()){
                        branchTopicHouTaiVo.setContext1(branchOption.getContent());
                        continue;
                    }else{
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext2())){
                            branchTopicHouTaiVo.setContext2(branchOption.getContent());
                            continue;
                        }
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext3())){
                            branchTopicHouTaiVo.setContext3(branchOption.getContent());
                            continue;
                        }
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext4())){
                            branchTopicHouTaiVo.setContext4(branchOption.getContent());
                            continue;
                        }
                    }
                }
            }else if(branchTopic.getTopicType() == 1){
                // 填空题
                for(BranchOption branchOption : branchOptionList){
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getTContext1())){
                        branchTopicHouTaiVo.setTContext1(branchOption.getContent());
                        continue;
                    }
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getTContext2())){
                        branchTopicHouTaiVo.setTContext2(branchOption.getContent());
                        continue;
                    }

                }
            }else if(branchTopic.getTopicType() == 2){
                // 判断题
                for(BranchOption branchOption : branchOptionList){
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getPContext())){
                        branchTopicHouTaiVo.setPContext(branchOption.getContent());
                        continue;
                    }
                }
            }
            model.addAttribute("branchTopic",branchTopicHouTaiVo);
        }else{
            // 新增题目（默认选择题）
            BranchTopicHouTaiVo branchTopicHouTaiVo = new BranchTopicHouTaiVo();
            branchTopicHouTaiVo.setTopicType(0);
            model.addAttribute("branchTopic",branchTopicHouTaiVo);
        }

        model.addAttribute("isEdit",topicId!=null);
        model.addAttribute("buMenType",type);
        model.addAttribute("topicId",topicId);
        model.addAttribute("companyType",companyType);
        return "branchtopicedit/insertOrEdit";
    }


    /**
     * 新增或修改题目（二期）
     * @param branchTopicHouTaiVo 部门题库题目对象
     * @return 新增或修改题目成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @RequestMapping("/editBranchTopic")
    @ResponseBody
    public Result editBranchTopic(BranchTopicHouTaiVo branchTopicHouTaiVo) {
        try {
            // 公共参数校验
            if (StringUtils.isEmpty(branchTopicHouTaiVo.getTitle())) {
                return Result.newFaild("添加或修改题目不能为空");
            }
            if (StringUtils.isEmpty(branchTopicHouTaiVo.getCorrectParse())) {
                return Result.newFaild("添加或修改题目正确解析不能为空");
            }
            if (0 == branchTopicHouTaiVo.getType()) {
                return Result.newFaild("添加或修改题目所属部门不能为空");
            }
            if (0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext1())) {
                    return Result.newFaild("选择题的正确答案选项不能为空");
                }
                // 选择题选项重新赋值，去除期间的空白项、重复项
                LinkedHashSet<String> contexts = new LinkedHashSet<>();
                contexts.add(branchTopicHouTaiVo.getContext1());
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getContext2())) {
                    contexts.add(branchTopicHouTaiVo.getContext2());
                }
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    contexts.add(branchTopicHouTaiVo.getContext3());
                }
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
                    contexts.add(branchTopicHouTaiVo.getContext4());
                }
                if (contexts.size() < 2) {
                    return Result.newFaild("选择题的选项不能少于2个");
                }
                // 选项再赋值
                Iterator<String> iterator = contexts.iterator();
                int index = 1;
                while (iterator.hasNext()) {
                    if (1 == index) {
                        branchTopicHouTaiVo.setContext1(iterator.next());
                        branchTopicHouTaiVo.setContext2(null);
                        branchTopicHouTaiVo.setContext3(null);
                        branchTopicHouTaiVo.setContext4(null);
                    }
                    if (2 == index) {
                        branchTopicHouTaiVo.setContext2(iterator.next());
                    }
                    if (3 == index) {
                        branchTopicHouTaiVo.setContext3(iterator.next());
                    }
                    if (4 == index) {
                        branchTopicHouTaiVo.setContext4(iterator.next());
                    }
                    index++;
                }
            } else if (1 == branchTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1()) && StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    return Result.newFaild("填空题至少有一个选项");
                }
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    branchTopicHouTaiVo.setTContext1(branchTopicHouTaiVo.getTContext2());
                    branchTopicHouTaiVo.setTContext2(null);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getPContext())) {
                    return Result.newFaild("判断题答案不能为空");
                }
            } else {
                return Result.newFaild("题目类型只能为0/1/2");
            }

            // 新增或修改题目
            Result result = branchTopicService.editBranchTopic(branchTopicHouTaiVo);
            return result;
        } catch (Exception e) {
            log.error("新增或修改部门题目出错，错误信息: {}",e);
            return Result.newFaild("新增或修改部门题目出错");
        }
    }


    /**
     * 保存导入选择题信息（二期）
     * @param file 导入选择题excel文件
     * @param type 导入题目所属部门
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入选择题信息成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @RequestMapping("inputXuanZeFromExcel")
    @ResponseBody
    public Result inputXuanZeExcel(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "type",required = false,defaultValue = "1") Integer type,
                                   @RequestParam(value = "topicType",required = true,defaultValue = "0") Integer topicType,
                                   @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入选择题接口、type ：{}",type);
        if (type == null || topicType == null || companyType == null) {
            return Result.newFaild("题目所属部门、题目类型、题目所属公司参数均不能为空！");
        }
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //哪些题目保存出错
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                topicError = branchTopicService.inputXuanZeExcel(is, 0,1, type, topicType, companyType);
            } catch (IOException | InvalidFormatException e) {
                log.error("批量导入部门选择题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 保存导入填空题信息（二期）
     * @param file 导入选择题excel文件
     * @param type 导入题目所属部门
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入填空题信息成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @RequestMapping("inputTianKongFromExcel")
    @ResponseBody
    public Result inputTianKongExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                     @RequestParam(value = "type",required = false) Integer type,
                                     @RequestParam(value = "topicType",required = true) Integer topicType,
                                     @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入填空题接口、type ：{}",type);
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //哪些题目保存出错
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                topicError = branchTopicService.inputTianKongExcel(is, 0,1 ,type, topicType, companyType);//这里使用输入流把数据拿到
            } catch (Exception e) {
                log.error("批量导入部门填空题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 保存导入判断题信息（二期）
     * @param file 导入选择题excel文件
     * @param type 导入题目所属部门
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入判断题信息成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @RequestMapping("inputPanDuanFromExcel")
    @ResponseBody
    public Result inputPanDuanExcel(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "type",required = false) Integer type,
                                    @RequestParam(value = "topicType",required = true) Integer topicType,
                                    @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入判断题接口、type ：{}",type);
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //哪些题目保存出错
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                topicError = branchTopicService.inputPanDuanExcel(is, 0,1 ,type, topicType, companyType);//这里使用输入流把数据拿到
            } catch (Exception e) {
                log.error("批量导入部门判断题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 批量删除部门下题目
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("/deleteTopicByIds")
    @ResponseBody
    public Result deleteTopicByIds(@RequestParam(value = "ids",required = false) String ids) {
        try {
            // 如果批量删除id集合为空，则删除失败
            if (null == ids && ids.trim().length() == 0) {
                return Result.newFail();
            }
            ArrayList<Integer> integers = new ArrayList<>();
            String[] idLsit = ids.split(",");
            for (String s : idLsit) {
                integers.add(Integer.parseInt(s));
            }
            if(idLsit.length > 0){
                branchOptionMapper.delete(new QueryWrapper<BranchOption>().in("topic_id",integers));
                branchTopicMapper.delete(new QueryWrapper<BranchTopic>().in("id",integers));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.newFail();
        }
        return Result.newSuccess();
    }


    /**
     * 删除部门下特定题目（单一道题、二期）
     * @param id 题目id
     * @return 删除题目成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("/delTopic")
    @ResponseBody
    public Result delTopic(@RequestParam(value = "id", required = true) Integer id) {
        if (id == 0) {
            return Result.newFail("删除题目id不能为空！");
        }
        try {
            branchTopicService.delTopic(id);
        } catch (Exception e) {
            log.error("删除部门下特定题目出错，错误信息: {}",e);
            return Result.newFail("删除部门下特定题目出错");
        }

        return Result.newSuccess("删除部门下特定题目成功");
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     * @param ids 选中题目id数组
     * @return 删除选中多道题成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("/deleteTopics")
    @ResponseBody
    public Result deleteTopics(@RequestParam(value = "ids",required = true) Integer[] ids) {
        if (ids.length == 0) {
            return Result.newFail("批量删除操作，题目勾选不能为空！");
        }
        try {
            branchTopicService.deleteTopics(ids);
        } catch (Exception e) {
            log.error("批量删除部门下选定题目出错，错误信息: {}",e);
            return Result.newFail("批量删除部门下选定题目出错");
        }

        return Result.newSuccess("批量删除部门下选定题目成功");
    }


    /**
     * 删除部门下所有题目（二期）
     * @param type 所属部门
     * @return 删除特定部门下题目成功与否
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("/deleteTopicAll")
    @ResponseBody
    public Result deleteTopicAll(@RequestParam(value = "type", required = true, defaultValue = "0") Integer type) {
        try {
            branchTopicService.deleteTopicAll(type);
        } catch (Exception e) {
            log.error("删除部门下所有题目出错，错误信息: {}",e);
            return Result.newFail("删除部门下所有题目出错");
        }

        return Result.newSuccess("删除部门下所有题目成功");
    }


    /**
     * 有条件批量导出题目(选择题、填空题、判断题)（二期 由于前端layui不能指定导出位置，此方法暂时废除）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 题目所属部门
     * @param topicType 题目类型
     * @param content 题目内容（用于模糊搜索）
     * @param path 导出文件路径
     * @return 有条件批量导出题目是否成功
     */
    @RequiresPermissions("system:branchTopic:view")
    @PostMapping("/exportBranchToExcel")
    @ResponseBody
    public Result writeTitleExcel(@RequestParam(value = "startTime",required = false) String startTime,
                                  @RequestParam(value = "endTime",required = false) String endTime,
                                  @RequestParam(value = "type",required = true) Integer type,
                                  @RequestParam(value = "topicType",required = true) Integer topicType,
                                  @RequestParam(value = "content",required = false) String content,
                                  @RequestParam(value = "path",required = true) String path) throws ParseException, ParseException {
        QueryWrapper<BranchTopic> branchTopicQueryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;
        if(!StringUtils.isEmpty(startTime)){
            startDate = sdf.parse(startTime.trim());
            branchTopicQueryWrapper.gt("create_time",startDate);
        }
        if(!StringUtils.isEmpty(endTime)){
            endDate = sdf.parse(endTime.trim());
            branchTopicQueryWrapper.lt("create_time",endDate);
        }
        // 判断时间
        startTime = startTime.trim();
        endTime = endTime.trim();
        if (startTime != null && !startTime.equals("") && endTime != null && !endTime.equals("")) {
            if (startDate.compareTo(endDate) > 0) {
                throw new BusinessException("开始时间不能晚于结束时间！");
            }
        }
        // 内容模糊查询
        if (content != null && !content.equals("")) {
            branchTopicQueryWrapper.like("title",content.trim());
        }
        // 判断导出类型
        branchTopicQueryWrapper.eq("type",type);
        branchTopicQueryWrapper.eq("topic_type",topicType);

        // 查询
        List<BranchTopic> branchTopicList = branchTopicService.list(branchTopicQueryWrapper);
        String[] cellNames = null;
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            try {
                // 开始题目导出至Excel文件
                if (topicType == 0) {
                    // 导出选择题
                    branchTopicService.exportXuanZeToExcel(path, branchTopicList);
                } else if (topicType == 1) {
                    // 导出填空题
                    branchTopicService.exportTianKongToExcel(path, branchTopicList);
                } else if(topicType == 2) {
                    // 导出判断题
                    branchTopicService.exportPanDuanToExcel(path, branchTopicList);
                }
            } catch (Exception e) {
                log.error("有条件批量导出部门题目出错，错误信息: {}",e);
                throw new BusinessException("有条件批量导出题目出错");
            }
        }

        return Result.newSuccess();
    }
}

