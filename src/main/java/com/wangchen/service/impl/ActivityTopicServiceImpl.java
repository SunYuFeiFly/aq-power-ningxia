package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.ActivityOption;
import com.wangchen.entity.ActivityTopic;
import com.wangchen.entity.BranchOption;
import com.wangchen.entity.BranchTopic;
import com.wangchen.mapper.ActivityOptionMapper;
import com.wangchen.mapper.ActivityTopicMapper;
import com.wangchen.service.ActivityTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.vo.ActivityTopicHouTaiVo;
import com.wangchen.vo.BranchTopicHouTaiVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * <p>
 * 活动赛题库 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */

@Slf4j
@Service
public class ActivityTopicServiceImpl extends ServiceImpl<ActivityTopicMapper, ActivityTopic> implements ActivityTopicService {

    @Autowired
    private ActivityTopicMapper activityTopicMapper;

    @Autowired
    private ActivityOptionMapper activityOptionMapper;


    /**
     * 添加或修改活动赛题目（单个 二期）
     * @param activityTopicHouTaiVo 活动赛题目对象
     * @return 添加或修改是否成功
     */
    @Override
    @Transactional
    public Result editActivityTopic(ActivityTopicHouTaiVo activityTopicHouTaiVo) {
        if (null == activityTopicHouTaiVo.getId()) {
            // 新增
            ActivityTopic activityTopic = new ActivityTopic();
            activityTopic.setType(null);
            activityTopic.setTopicType(activityTopicHouTaiVo.getTopicType());
            activityTopic.setTitle(activityTopicHouTaiVo.getTitle());
            activityTopic.setCreateTime(new Date());
            activityTopic.setPoint(activityTopicHouTaiVo.getTitle());
            activityTopic.setCorrectParse(activityTopicHouTaiVo.getCorrectParse());
            activityTopic.setCompanyType(activityTopicHouTaiVo.getCompanyType());
            activityTopic.setActivityId(activityTopicHouTaiVo.getActivityId());
            activityTopic.setImageUrl(null);
            activityTopic.setVideoUrl(null);
            if (!StringUtils.isEmpty(activityTopicHouTaiVo.getImageUrl())) {
                activityTopic.setImageUrl(activityTopicHouTaiVo.getImageUrl());
            }
            if (!StringUtils.isEmpty(activityTopicHouTaiVo.getVideoUrl())) {
                activityTopic.setVideoUrl(activityTopicHouTaiVo.getVideoUrl());
            }
            activityTopicMapper.insert(activityTopic);

            if (0 == activityTopicHouTaiVo.getTopicType()) {
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 =false;
                boolean c3 =false;
                boolean c4 =false;
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getContext3())) {
                    c3 = true;
                }
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for(int i = 0;i < 4;i++) {
                    if (i == num) {
                        // 保存正确选项
                        ActivityOption a1 = new ActivityOption();
                        a1.setTopicId(activityTopic.getId());
                        a1.setContent(activityTopicHouTaiVo.getContext1());
                        a1.setCreateTime(new Date());
                        activityOptionMapper.insert(a1);

                        activityTopic.setCorrectOptionId(a1.getId());
                        activityTopicMapper.updateById(activityTopic);
                    } else {
                        if(!c2){
                            ActivityOption a2 = new ActivityOption();
                            a2.setTopicId(activityTopic.getId());
                            a2.setContent(activityTopicHouTaiVo.getContext2());
                            a2.setCreateTime(new Date());
                            activityOptionMapper.insert(a2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            ActivityOption a3 = new ActivityOption();
                            a3.setTopicId(activityTopic.getId());
                            a3.setContent(activityTopicHouTaiVo.getContext3());
                            a3.setCreateTime(new Date());
                            activityOptionMapper.insert(a3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            ActivityOption a4 = new ActivityOption();
                            a4.setTopicId(activityTopic.getId());
                            a4.setContent(activityTopicHouTaiVo.getContext4());
                            a4.setCreateTime(new Date());
                            activityOptionMapper.insert(a4);
                            c4 = true;
                            continue;
                        }
                    }
                }
            } else if (1 == activityTopicHouTaiVo.getTopicType()) {
                // 填空题
                if(!StringUtils.isEmpty(activityTopicHouTaiVo.getTContext1())) {
                    ActivityOption a1 = new ActivityOption();
                    a1.setTopicId(activityTopic.getId());
                    a1.setContent(activityTopicHouTaiVo.getTContext1());
                    a1.setCreateTime(new Date());
                    activityOptionMapper.insert(a1);
                }
                if (!StringUtils.isEmpty(activityTopicHouTaiVo.getTContext2())) {
                    ActivityOption a2 = new ActivityOption();
                    a2.setTopicId(activityTopic.getId());
                    a2.setContent(activityTopicHouTaiVo.getTContext2());
                    a2.setCreateTime(new Date());
                    activityOptionMapper.insert(a2);
                }
            } else if (2 == activityTopicHouTaiVo.getTopicType()) {
                // 判断题
                ActivityOption a1 = new ActivityOption();
                a1.setTopicId(activityTopic.getId());
                a1.setContent(activityTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                a1.setCreateTime(new Date());
                activityOptionMapper.insert(a1);
            } else {
                throw new BusinessException("题目不可能是除了1/2/3之外的类型");
            }
        } else {
            // 修改(不可以修改视频或图片地址)
            ActivityTopic activityTopic = activityTopicMapper.selectById(activityTopicHouTaiVo.getId());
            activityTopic.setTitle(activityTopicHouTaiVo.getTitle());
            activityTopic.setCorrectParse(activityTopicHouTaiVo.getCorrectParse());
            activityTopic.setPoint(activityTopicHouTaiVo.getTitle());
            if (StringUtils.isEmpty(activityTopicHouTaiVo.getImageUrl().trim())) {
                activityTopic.setImageUrl(null);
            } else {
                activityTopic.setImageUrl(activityTopicHouTaiVo.getImageUrl().trim());
            }
            if (StringUtils.isEmpty(activityTopicHouTaiVo.getVideoUrl().trim())) {
                activityTopic.setVideoUrl(null);
            } else {
                activityTopic.setVideoUrl(activityTopicHouTaiVo.getVideoUrl().trim());
            }
            activityTopicMapper.updateById(activityTopic);
            // 删除题目对应答案数据
            activityOptionMapper.delete(new QueryWrapper<ActivityOption>().eq("topic_id",activityTopic.getId()));

            if (0 == activityTopicHouTaiVo.getTopicType()) {
                // 选择题
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 =false;
                boolean c3 =false;
                boolean c4 =false;
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getContext3())) {
                    c3 = true;
                }
                if (StringUtils.isEmpty(activityTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for (int i = 0;i < 4;i++) {
                    if (i == num) {
                        // 保存选择题正确选项
                        ActivityOption a1 = new ActivityOption();
                        a1.setTopicId(activityTopic.getId());
                        a1.setContent(activityTopicHouTaiVo.getContext1());
                        a1.setCreateTime(new Date());
                        activityOptionMapper.insert(a1);

                        activityTopic.setCorrectOptionId(a1.getId());
                        activityTopicMapper.updateById(activityTopic);
                    } else {
                        if (!c2) {
                            ActivityOption a2 = new ActivityOption();
                            a2.setTopicId(activityTopic.getId());
                            a2.setContent(activityTopicHouTaiVo.getContext2());
                            a2.setCreateTime(new Date());
                            activityOptionMapper.insert(a2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            ActivityOption a3 = new ActivityOption();
                            a3.setTopicId(activityTopic.getId());
                            a3.setContent(activityTopicHouTaiVo.getContext3());
                            a3.setCreateTime(new Date());
                            activityOptionMapper.insert(a3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            ActivityOption a4 = new ActivityOption();
                            a4.setTopicId(activityTopic.getId());
                            a4.setContent(activityTopicHouTaiVo.getContext4());
                            a4.setCreateTime(new Date());
                            activityOptionMapper.insert(a4);
                            c4 = true;
                            continue;
                        }
                    }
                }
            } else if (1 == activityTopicHouTaiVo.getTopicType()) {
                if(!StringUtils.isEmpty(activityTopicHouTaiVo.getTContext1())) {
                    ActivityOption a1 = new ActivityOption();
                    a1.setTopicId(activityTopic.getId());
                    a1.setContent(activityTopicHouTaiVo.getTContext1());
                    a1.setCreateTime(new Date());
                    activityOptionMapper.insert(a1);
                }
                if (!StringUtils.isEmpty(activityTopicHouTaiVo.getTContext2())) {
                    ActivityOption a2 = new ActivityOption();
                    a2.setTopicId(activityTopic.getId());
                    a2.setContent(activityTopicHouTaiVo.getTContext2());
                    a2.setCreateTime(new Date());
                    activityOptionMapper.insert(a2);
                }
            } else if (2 == activityTopicHouTaiVo.getTopicType()) {
                // 判断题
                ActivityOption a1 = new ActivityOption();
                a1.setTopicId(activityTopic.getId());
                a1.setContent(activityTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                a1.setCreateTime(new Date());
                activityOptionMapper.insert(a1);
            }
        }

        return Result.newSuccess(null == activityTopicHouTaiVo.getId()? "添加活动赛题目成功" : "修改活动赛题目成功");
    }


    /**
     * 保存导入选择题信息（二期）
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @param activityId 活动赛id
     * @return 导入选择题信息是否成功
     */
    @Override
    @Transactional
    public String inputXuanZeExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Object type, Integer topicType, Integer companyType, Integer activityId) throws IOException, InvalidFormatException {
        // 记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        // 收集保存选择题对象
        //获取内容信息(导入表没有'公司分类这个选项'，导出表也没有)
        for (int i = rowStart; i <= rowEnd; ++i) {
            try{
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = ActivityTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    ActivityTopic activityTopic = new ActivityTopic();
                    // 二期后选择题答案不一定为4个，由导入题库选项个数决定，目前暂定校验时最少为2个
                    ActivityOption activityOptionA = new ActivityOption();
                    ActivityOption activityOptionB = new ActivityOption();
                    ActivityOption activityOptionC = new ActivityOption();
                    ActivityOption activityOptionD = new ActivityOption();

                    activityTopic.setType(null);
                    activityTopic.setTopicType(topicType);
                    activityTopic.setCompanyType(companyType);
                    activityTopic.setActivityId(activityId);
                    activityTopic.setImageUrl(null);
                    activityTopic.setVideoUrl(null);
                    // 标记哪一项是正确选项
                    String select = "";
                    // 标记之前选项哪几项不为空(默认不为空)
                    Boolean aXZ = false;
                    // ABCD选项内容不为空数量
                    Integer cellCount = 4;
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            if(StringUtils.isEmpty(data.getStringCellValue().trim())){
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目]内容不能为空;  ");
                            } else {
                                activityTopic.setTitle(data.getStringCellValue().trim());
                                activityTopic.setPoint(data.getStringCellValue().trim());
                                activityTopic.setCreateTime(new Date());
                                activityTopicMapper.insert(activityTopic);
                            }
                        } else if (1 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[正确选项]内容不能为空  ");
                            } else {
                                String choose = data.getStringCellValue().trim().toUpperCase();
                                if ("A".equals(choose) || "B".equals(choose) || "C".equals(choose) || "D".equals(choose)) {
                                    select = choose;
                                } else {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[正确选项]内容只能为ABCD其中之一");
                                }
                            }
                        } else if (2 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[A选项]内容不能为空  ");
                                aXZ = true;
                                cellCount--;
                            } else {
                                activityOptionA.setContent(data.getStringCellValue().trim());
                                activityOptionA.setCreateTime(new Date());
                                activityOptionA.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionA);
                                if("A".equals(select.trim())){
                                    activityTopic.setCorrectOptionId(activityOptionA.getId());
                                    activityTopicMapper.updateById(activityTopic);
                                }
                            }
                        } else if (3 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[B选项]内容不能为空  ");
                                cellCount--;
                            } else {
                                if (aXZ) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j) +"列[A选项]内容为空，应调整此单元格内容至[A选项]  ");
                                }
                                activityOptionB.setContent(data.getStringCellValue().trim());
                                activityOptionB.setCreateTime(new Date());
                                activityOptionB.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionB);
                                if("B".equals(select.trim())){
                                    activityTopic.setCorrectOptionId(activityOptionB.getId());
                                    activityTopicMapper.updateById(activityTopic);
                                }
                            }
                        } else if (4 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount == 2) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[C选项]之前AB选项均为空，此选项已经不能为空 ");
                                }
                                cellCount--;
                            } else {
                                if (cellCount < 4) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[C选项]之前有列选项空白，应依次调整前移  ");
                                }
                                activityOptionC.setContent(data.getStringCellValue().trim());
                                activityOptionC.setCreateTime(new Date());
                                activityOptionC.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionC);
                                if("C".equals(select.trim())){
                                    activityTopic.setCorrectOptionId(activityOptionC.getId());
                                    activityTopicMapper.updateById(activityTopic);
                                }
                            }
                        } else if (5 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount < 3) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[D选项]之前至少有2列选项空白，此项不能为空且需依次调整前移  ");
                                }
                            } else {
                                activityOptionD.setContent(data.getStringCellValue().trim());
                                activityOptionD.setCreateTime(new Date());
                                activityOptionD.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionD);
                                if("D".equals(select)){
                                    activityTopic.setCorrectOptionId(activityOptionD.getId());
                                    activityTopicMapper.updateById(activityTopic);
                                }
                            }
                        } else if (6 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目解析]内容不能为空  ");
                            } else {
                                activityTopic.setCorrectParse(data.getStringCellValue().trim());
                            }
                        } else if (7 == j) {
                            // 题目相关图片路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setImageUrl(data.getStringCellValue().trim());
                            }
                        } else if (8 == j) {
                            // 题目相关视频路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setVideoUrl(data.getStringCellValue().trim());
                            }
                        }
                        activityTopicMapper.updateById(activityTopic);
                    }
                    // branchTopicList.add(branchTopic);
                } else {
                    break;
                }
            }catch (Exception e){
                log.error("第"+i+"道题目导入错误:{}",e);
                topicError.append("第"+i+"道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入选择题总体数据存出错，错误信息: {}",topicError.toString());
            throw new BusinessException("批量导入选择题总体数据存出错：" + topicError.toString());
        }

        return "批量导入选择题信息成功";
    }


    /**
     * 保存导入填空题信息（二期）
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @param activityId 活动赛id
     * @return 导入选择题信息是否成功
     */
    @Override
    @Transactional
    public String inputTianKongExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Object type, Integer topicType, Integer companyType, Integer activityId) throws IOException, InvalidFormatException {
        // 记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        // 生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        // 最后一行数据
        int rowEnd = sheet.getLastRowNum();
        // 收集保存填空题对象
        // ArrayList<BranchTopic> branchTopicList = new ArrayList<>();
        // 获取内容信息
        for (int i = rowStart; i <= rowEnd; ++i) {
            try{
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = ActivityTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 默认一道题最多两个填空选项
                    ActivityTopic activityTopic = new ActivityTopic();
                    ActivityOption activityOptionA = new ActivityOption();
                    ActivityOption activityOptionB = new ActivityOption();

                    activityTopic.setType(null);
                    activityTopic.setTopicType(topicType);
                    activityTopic.setCompanyType(companyType);
                    activityTopic.setActivityId(activityId);
                    activityTopic.setImageUrl(null);
                    activityTopic.setVideoUrl(null);
                    // 标记填空题1内容是不是空
                    Boolean aTK = false;
                    // 编列每行列内容
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            // 题目内容
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[题目]内容不能为空！  ");
                                aTK = true;
                            } else {
                                activityTopic.setTitle(data.getStringCellValue().trim());
                                activityTopic.setPoint(data.getStringCellValue().trim());
                                activityTopic.setCreateTime(new Date());
                                activityTopicMapper.insert(activityTopic);
                            }
                        } else if (1 == j) {
                            // 填空项1
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[填空项1]内容不能为空！  ");
                            } else {
                                if (aTK) {
                                    topicError.append("第" + (i+1) + "行第" + (j) + "列[填空项1]内容内容为空，此内容应前移！  ");
                                }
                                activityOptionA.setContent(data.getStringCellValue().trim());
                                activityOptionA.setCreateTime(new Date());
                                activityOptionA.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionA);
                            }
                        } else if (2 == j) {
                            // 填空项2
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityOptionB.setContent(data.getStringCellValue().trim());
                                activityOptionB.setCreateTime(new Date());
                                activityOptionB.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOptionB);
                            }
                        } else if (3 == j) {
                            // 题目解析
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[题目解析]内容不能为空！  ");
                            } else {
                                activityTopic.setCorrectParse(data.getStringCellValue().trim());
                            }
                        } else if (4 == j) {
                            // 题目相关图片路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setImageUrl(data.getStringCellValue().trim());
                            }
                        } else if (5 == j) {
                            // 题目相关视频路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setVideoUrl(data.getStringCellValue().trim());
                            }
                        }
                        activityTopicMapper.updateById(activityTopic);
                    }
                    // branchTopicList.add(branchTopic);
                } else {
                    break;
                }
            }catch (Exception e){
                log.error("第"+i+"道题目导入错误:{}",e);
                topicError.append("第"+i+"道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入填空题总体数据存在错误:{}",topicError.toString());
            throw new BusinessException("批量导入填空题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入填空题信息成功";
    }


    /**
     * 保存导入判断题信息（二期）
     * @param excelInputSteam 导入判断题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @param activityId 活动赛id
     * @return 导入选择题信息是否成功
     */
    @Override
    @Transactional
    public String inputPanDuanExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Object type, Integer topicType, Integer companyType, Integer activityId) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        // 收集保存判断题对象
        //ArrayList<BranchTopic> branchTopicList = new ArrayList<>();
        //获取内容信息
        for (int i = rowStart; i <= rowEnd; ++i) {
            try{
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = ActivityTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 一个题目一个判断选项
                    ActivityTopic activityTopic = new ActivityTopic();
                    ActivityOption activityOption = new ActivityOption();

                    activityTopic.setType(null);
                    activityTopic.setTopicType(topicType);
                    activityTopic.setCompanyType(companyType);
                    activityTopic.setActivityId(activityId);
                    activityTopic.setImageUrl(null);
                    activityTopic.setVideoUrl(null);
                    // 遍历每行内容
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            // 题目
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[题目]内容不能为空！  ");
                            } else {
                                activityTopic.setTitle(data.getStringCellValue().trim());
                                activityTopic.setPoint(data.getStringCellValue().trim());
                                activityTopic.setCreateTime(new Date());
                                activityTopicMapper.insert(activityTopic);
                            }
                        } else if (1 == j) {
                            // 答案
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[答案]内容不能为空！  ");
                            } else {
                                if ("0".equals(data.getStringCellValue())) {
                                    activityOption.setContent("错");
                                } else if ("1".equals(data.getStringCellValue())) {
                                    activityOption.setContent("对");
                                } else {
                                    topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[答案]内容只能为0或1 ");
                                }
                                activityOption.setCreateTime(new Date());
                                activityOption.setTopicId(activityTopic.getId());
                                activityOptionMapper.insert(activityOption);
                            }
                        } else if (2 == j) {
                            // 题目解析
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i+1) + "行第" + (j + 1) + "列[题目解析]内容不能为空！  ");
                            } else {
                                activityTopic.setCorrectParse(data.getStringCellValue().trim());
                            }
                        } else if (3 == j) {
                            // 题目相关图片路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setImageUrl(data.getStringCellValue().trim());
                            }
                        } else if (4 == j) {
                            // 题目相关视频路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                activityTopic.setVideoUrl(data.getStringCellValue().trim());
                            }
                        }
                        activityTopicMapper.updateById(activityTopic);
                    }
                    // branchTopicList.add(branchTopic);
                } else {
                    break;
                }
            }catch (Exception e){
                log.error("第"+i+"道题目导入错误:{}",e);
                topicError.append("第"+i+"道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入判断题总体数据存在错误:{}",topicError.toString());
            throw new BusinessException("批量导入判断题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入判断题信息成功";
    }


    /**
     * 判断读取行是否内容为空（存在格式设置）
     * @param row 行数据
     * @return 该行是否存在数据
     */
    public static boolean isRowEmpty(Row row){
        boolean isNull = true;
        // 只要行包含格式，则不为null，然后再根据内容判断该行是否为空
        if (null != row) {
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (!"".equals(cell.toString().trim())) {
                    isNull = false;
                    break;
                }
            }
        }

        return isNull;
    }

}
