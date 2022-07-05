package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.FeiBranchOption;
import com.wangchen.entity.FeiBranchTopic;
import com.wangchen.mapper.FeiBranchOptionMapper;
import com.wangchen.mapper.FeiBranchTopicMapper;
import com.wangchen.service.FeiBranchOptionService;
import com.wangchen.service.FeiBranchTopicService;
import com.wangchen.vo.BranchTopicHouTaiVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 非专业部门题库表 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
@Slf4j
@Controller
@RequestMapping("/system/feiBranchTopic")
public class FeiBranchTopicController {

    @Autowired
    private FeiBranchTopicMapper feiBranchTopicMapper;

    @Autowired
    private FeiBranchOptionMapper feiBranchOptionMapper;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;


    /**
     * 进入题目页面初始化（二期）
     * @param model 模板
     * @param companyType 所属公司类型
     * @return 跳转页面
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @GetMapping("/list")
    public String index(Model model,
                        @RequestParam(value = "companyType", required = false, defaultValue = "1") int companyType) {
        model.addAttribute("companyType",companyType);
        if (1 == companyType) {
            return "feitopic/aList";
        } else if (2 == companyType) {
            return "feitopic/bList";
        } else if (3 == companyType) {
            return "feitopic/cList";
        }

        return null;
    }


    /**
     * 获取所属公司分类下必知必会题目集合(二期)
     * @param title 标题（用于题目模糊搜索）
     * @param page 页码
     * @param limit 每页数据量
     * @param companyType 所属公司类型
     * @return 获取所属公司分类下必知必会题目合集
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("selectPages")
    public @ResponseBody
    Result selectPages(@RequestParam(value = "title", required = false, defaultValue = "") String title,
                       @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                       @RequestParam(value = "companyType", required = false, defaultValue = "1") int companyType) {
        try {
            Result result = feiBranchTopicService.selectPages(page, limit, title, companyType);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取所属公司分类下必知必会题目集合出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR.getCode(), BusinessErrorMsg.BUSINESS_ERROR.getMsg());
        }
    }


    /**
     * 手动新增或查看题目初始化（二期）
     * @param model 数据模型
     * @param companyType 所属公司分类
     * @param topicId 题目id
     * @return 页面跳转及数据
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @GetMapping("/insertOrEditInit")
    public String insertOrEditInit(Model model,
                                   @RequestParam(value = "companyType", required = false, defaultValue = "1") Integer companyType,
                                   @RequestParam(value = "topicId", required = false, defaultValue = "") Integer topicId) {
        if(null != topicId && 0 != topicId){
            FeiBranchTopic feiBranchTopic = feiBranchTopicService.getById(topicId);
            BranchTopicHouTaiVo branchTopicHouTaiVo = new BranchTopicHouTaiVo();
            branchTopicHouTaiVo.setTitle(feiBranchTopic.getTitle());
            branchTopicHouTaiVo.setTopicType(feiBranchTopic.getTopicType());
            branchTopicHouTaiVo.setCorrectParse(feiBranchTopic.getCorrectParse());
            branchTopicHouTaiVo.setImageUrl(feiBranchTopic.getImageUrl());
            branchTopicHouTaiVo.setVideoUrl(feiBranchTopic.getVideoUrl());
            branchTopicHouTaiVo.setCompanyType(companyType);

            List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>().eq("topic_id", feiBranchTopic.getId()));

            if(0 == feiBranchTopic.getTopicType()){
                // 选择题
                for(FeiBranchOption feiBranchOption : feiBranchOptionList){
                    if(feiBranchTopic.getCorrectOptionId().intValue() == feiBranchOption.getId()){
                        branchTopicHouTaiVo.setContext1(feiBranchOption.getContent());
                        continue;
                    }else{
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext2())){
                            branchTopicHouTaiVo.setContext2(feiBranchOption.getContent());
                            continue;
                        }
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext3())){
                            branchTopicHouTaiVo.setContext3(feiBranchOption.getContent());
                            continue;
                        }
                        if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getContext4())){
                            branchTopicHouTaiVo.setContext4(feiBranchOption.getContent());
                            continue;
                        }
                    }
                }
            }else if(1 == feiBranchTopic.getTopicType()){
                // 填空题
                for(FeiBranchOption feiBranchOption : feiBranchOptionList){
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getTContext1())){
                        branchTopicHouTaiVo.setTContext1(feiBranchOption.getContent());
                        continue;
                    }
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getTContext2())){
                        branchTopicHouTaiVo.setTContext2(feiBranchOption.getContent());
                        continue;
                    }

                }
            }else if(2 == feiBranchTopic.getTopicType()){
                // 判断题
                for(FeiBranchOption feiBranchOption : feiBranchOptionList){
                    if(org.apache.commons.lang.StringUtils.isBlank(branchTopicHouTaiVo.getPContext())){
                        branchTopicHouTaiVo.setPContext(feiBranchOption.getContent());
                        continue;
                    }
                }
            }
            model.addAttribute("branchTopic",branchTopicHouTaiVo);
        }else{
            // 新增题目（默认选择题）
            BranchTopicHouTaiVo branchTopicHouTaiVo = new BranchTopicHouTaiVo();
            branchTopicHouTaiVo.setTopicType(0);
            branchTopicHouTaiVo.setCompanyType(companyType);
            model.addAttribute("branchTopic",branchTopicHouTaiVo);
        }

        model.addAttribute("isEdit",topicId!=null);
        model.addAttribute("topicId",topicId);
        model.addAttribute("companyType",companyType);
        return "feibranchtopicedit/insertOrEdit";
    }


    /**
     * 新增修改必知必会题目(二期)
     * @param branchTopicHouTaiVo 必知必会题目对象
     * @return 新增修改必知必会题目成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @RequestMapping("/editFeiBranchTopic")
    @ResponseBody
    public Result editBranchTopic(BranchTopicHouTaiVo branchTopicHouTaiVo) {
        try {
            // 公共参数校验
            if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getTitle())) {
                return Result.newFaild("添加或修改题目不能为空");
            }
            if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getCorrectParse())) {
                return Result.newFaild("添加或修改题目正确解析不能为空");
            }
            if (0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext1())) {
                    return Result.newFaild("选择题的正确答案选项不能为空");
                }
                // 选择题选项重新赋值，去除期间的空白项、重复项
                LinkedHashSet<String> contexts = new LinkedHashSet<>();
                contexts.add(branchTopicHouTaiVo.getContext1());
                if (!com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext2())) {
                    contexts.add(branchTopicHouTaiVo.getContext2());
                }
                if (!com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    contexts.add(branchTopicHouTaiVo.getContext3());
                }
                if (!com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
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
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1()) && com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    return Result.newFaild("填空题至少有一个选项");
                }
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    branchTopicHouTaiVo.setTContext1(branchTopicHouTaiVo.getTContext2());
                    branchTopicHouTaiVo.setTContext2(null);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getPContext())) {
                    return Result.newFaild("判断题答案不能为空");
                }
            } else {
                return Result.newFaild("题目类型只能为0/1/2");
            }

            // 新增或修改题目
            Result result = feiBranchTopicService.editBranchTopic(branchTopicHouTaiVo);
            return result;
        } catch (Exception e) {
            log.error("新增修改必知必会题目出错，错误信息: {}",e);
            return Result.newFaild("新增修改必知必会题目出错");
        }
    }


    /**
     * 删除所有题目
     * @return
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("/deleteTopicAll01")
    public @ResponseBody
    Result deleteTopicAll01() {
        try {
            feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>());
            feiBranchTopicMapper.delete(new QueryWrapper<FeiBranchTopic>());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.newFail();
        }
        return Result.newSuccess();
    }


    /**
     * 删除必知必会题目
     * @param id
     * @return
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("/delTopic01")
    public @ResponseBody
    Result delTopic01(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        try {
            feiBranchTopicMapper.delete(new QueryWrapper<FeiBranchTopic>().eq("id",id.intValue()));
            feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>().eq("topic_id",id.intValue()));
        } catch (Exception e) {
            log.error("删除必知必会题目出错，错误信息: {}",e);
            return Result.newFail("删除必知必会题目出错");
        }

        return Result.newSuccess();
    }


    /**
     * 保存导入选择题信息(二期)
     * @param file 导入选择题excel文件
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入选择题信息成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @RequestMapping("inputXuanZeExcel")
    @ResponseBody
    public Result inputXuanZeExcel(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "topicType",required = true) Integer topicType,
                                   @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入选择题接口、");
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                topicError = feiBranchTopicService.inputXuanZeExcel(is, 0,1, topicType, companyType);//这里使用输入流把数据拿到
            } catch (IOException | InvalidFormatException e) {
                log.error("保存导入必知必会选择题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 保存导入填空题信息（二期）
     * @param file 导入选择题excel文件
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入填空题信息成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @RequestMapping("inputTianKongExcel")
    @ResponseBody
    public Result inputTianKongExcel(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "topicType",required = true) Integer topicType,
                                     @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入填空题接口、");
        String topicError = "";
        if (!file.isEmpty()) {
            try {
                //获取原始的文件名
                String originalFilename = file.getOriginalFilename();
                String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
                //默认从第一行开始读取
                Integer startRows = 1;
                //获取输入流
                InputStream is = file.getInputStream();
                topicError = feiBranchTopicService.inputTianKongExcel(is, 0, 1, topicType, companyType);//这里使用输入流把数据拿到
            } catch (IOException | InvalidFormatException e) {
                log.error("保存导入必知必会填空题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 保存导入判断题信息（二期）
     * @param file 导入选择题excel文件
     * @param topicType 导入题目类型
     * @param companyType 题目所属公司类型
     * @return 导入判断题信息成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @RequestMapping("inputPanDuanExcel")
    @ResponseBody
    public Result inputPanDuanExcel(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "topicType",required = true) Integer topicType,
                                    @RequestParam(value = "companyType",required = true,defaultValue = "1") Integer companyType) {
        log.debug("进入导入判断题接口、");
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
                topicError = feiBranchTopicService.inputPanDuanExcel(is, 0, 1, topicType, companyType);//这里使用输入流把数据拿到
            } catch (IOException | InvalidFormatException e) {
                log.error("保存导入必知必会判断题出错，错误信息: {}",e);
                return Result.newFail(e.getMessage());
            }
        }

        return Result.newSuccess(topicError);
    }


    /**
     * 删除选定题目（一道题、二期）
     * @param id 题目id
     * @return 删除成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("/delTopic")
    @ResponseBody
    public Result delTopic(@RequestParam(value = "id", required = true, defaultValue = "") Integer id) {
        if (id == null) {
            throw new BusinessException("删除题目不能为空！");
        }
        try {
            feiBranchTopicService.delTopic(id);
        } catch (Exception e) {
            log.error("删除选定题目（必知必会）出错，错误信息: {}",e);
            return Result.newFail("删除选定题目（必知必会）出错");
        }

        return Result.newSuccess("删除选定题目成功");
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     * @param ids 题目id数组
     * @return 删除成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("/deleteTopics")
    @ResponseBody
    public Result deleteTopics(@RequestParam(value = "ids",required = true) Integer[] ids) {
        if (ids.length == 0) {
            throw new BusinessException("批量删除操作，题目勾选不能为空！");
        }
        try {
            feiBranchTopicService.deleteTopics(ids);
        } catch (Exception e) {
            log.error("批量删除部门下选定题目（必知必会）出错，错误信息: {}",e);
            return Result.newFail("批量删除部门下选定题目（必知必会）出错");
        }

        return Result.newSuccess("批量删除部门下选定题目成功");
    }


    /**
     * 删除对应公司类型下所有题目（二期）
     * @param companyType 所属公司类型
     * @return 删除成功与否
     */
    @RequiresPermissions("system:feiBranchTopic:view")
    @PostMapping("/deleteTopicAll")
    @ResponseBody
    public Result deleteTopicAll(@RequestParam(value = "companyType", required = true) Integer companyType) {
        if (companyType == 0) {
            throw new BusinessException("请选定需删除全部题目公司类型！");
        }
        try {
            feiBranchTopicService.deleteTopicAll(companyType);
        } catch (Exception e) {
            log.error("删除对应公司类型下所有题目（必知必会）出错，错误信息: {}",e);
            return Result.newFail("删除对应公司类型下所有题目（必知必会）出错");
        }

        return Result.newSuccess("删除对应公司类型下所有题目成功");
    }


    /**
     * 有条件批量导出题目(选择题、填空题、判断题)（二期 由于前端layui不能指定导出位置，此方法暂时废除）
     */
    public Result exportBranchToExcel(@RequestParam(value = "startTime",required = false) String startTime,
                                      @RequestParam(value = "endTime",required = false) String endTime,
                                      @RequestParam(value = "topicType",required = true) Integer topicType,
                                      @RequestParam(value = "content",required = false) String content,
                                      @RequestParam(value = "path",required = true) String path) throws ParseException, ParseException {
        QueryWrapper<FeiBranchTopic> feiBranchTopicQueryWrapper = new QueryWrapper<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = null;
        if(!com.wangchen.common.utils.StringUtils.isEmpty(startTime)){
            startDate = sdf.parse(startTime.trim());
            feiBranchTopicQueryWrapper.gt("create_time",startDate);
        }
        if(!com.wangchen.common.utils.StringUtils.isEmpty(endTime)){
            endDate = sdf.parse(endTime.trim());
            feiBranchTopicQueryWrapper.lt("create_time",endDate);
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
            feiBranchTopicQueryWrapper.like("title",content.trim());
        }
        // 判断导出类型
        feiBranchTopicQueryWrapper.eq("topic_type",topicType);
        // 查询
        List<FeiBranchTopic> feiBranchTopicList = feiBranchTopicService.list(feiBranchTopicQueryWrapper);
        String[] cellNames = null;
        File file = new File(path);
        if (file.isFile() && file.exists()) {
            try {
                // 开始题目导出至Excel文件
                if (topicType == 0) {
                    // 导出选择题
                    feiBranchTopicService.exportXuanZeToExcel(path, feiBranchTopicList);
                } else if (topicType == 1) {
                    // 导出填空题
                    feiBranchTopicService.exportTianKongToExcel(path, feiBranchTopicList);
                } else if(topicType == 2) {
                    // 导出判断题
                    feiBranchTopicService.exportPanDuanToExcel(path, feiBranchTopicList);
                }
            } catch (Exception e) {
                log.error("有条件批量导出题目（必知必会）出错，错误信息: {}",e);
                throw new BusinessException("有条件批量导出题目（必知必会）出错");
            }
        }

        return Result.newSuccess("有条件批量导出题目成功");
    }

}

