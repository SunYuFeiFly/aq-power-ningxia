package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.wangchen.vo.WisdomLibraryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门题库表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */

@Slf4j
@Service
public class BranchTopicServiceImpl extends ServiceImpl<BranchTopicMapper, BranchTopic> implements BranchTopicService {

    @Autowired
    private BranchTopicMapper branchTopicMapper;

    @Autowired
    private BranchOptionMapper branchOptionMapper;


    /**
     * 从所属公司分类下题库获取特定道题（二期）
     * @param num 所需获取题目数量
     * @param companyType 所属公司分类
     * @return 所属公司分类下题母合集
     */
    @Override
    public List<BranchTopic> listTopicRandomByCompanyType(Integer num, Integer companyType) {
        return branchTopicMapper.listTopicRandomByCompanyType(num, companyType);
    }

    @Override
    public List<BranchTopic> listTopicRandom(Integer branchType, Integer num) {
        return branchTopicMapper.listTopicRandom(branchType,num);
    }


    @Override
    public List<BranchTopic> listTopicRandom( Integer num) {
        return branchTopicMapper.listTopicRandoms(num);
    }


    @Override
    public List<BranchTopic> listTopicRandomsNotTianKong(Integer num) {
        return branchTopicMapper.listTopicRandomsNotTianKong(num);
    }


    /**
     * 保存导入选择题信息（二期）
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @return 导入选择题信息是否成功
     */
    @Override
    @Transactional
    public String inputXuanZeExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer type, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        // 收集保存选择题对象
        // ArrayList<BranchTopic> branchTopicList = new ArrayList<>();
        //获取内容信息(导入表没有'公司分类这个选项'，导出表也没有)
        for (int i = rowStart; i <= rowEnd; ++i) {
            try{
                Row currentRow = sheet.getRow(i);
                Boolean isEmpty = BranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    BranchTopic branchTopic =  new BranchTopic();
                    // 二期后选择题答案不一定为4个，由导入题库选项个数决定，目前暂定校验时最少为2个
                    BranchOption branchOptionA = new BranchOption();
                    BranchOption branchOptionB = new BranchOption();
                    BranchOption branchOptionC = new BranchOption();
                    BranchOption branchOptionD = new BranchOption();

                    branchTopic.setType(type);
                    branchTopic.setTopicType(topicType);
                    branchTopic.setCompanyType(companyType);
                    branchTopic.setImageUrl(null);
                    branchTopic.setVideoUrl(null);
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
                            if(StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目]内容不能为空！  ");
                            } else {
                                branchTopic.setTitle(data.getStringCellValue().trim());
                                branchTopic.setPoint(data.getStringCellValue().trim());
                                branchTopic.setCreateTime(new Date());
                                branchTopicMapper.insert(branchTopic);
                            }
                        } else if (1 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[正确选项]内容不能为空！  ");
                            } else {
                                String choose = data.getStringCellValue().trim().toUpperCase();
                                if ("A".equals(choose) || "B".equals(choose) || "C".equals(choose) || "D".equals(choose)) {
                                    select = choose;
                                } else {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[正确选项]内容只能为ABCD其中之一;  ");
                                }
                            }
                        } else if (2 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[A选项]内容不能为空！  ");
                                aXZ = true;
                                cellCount--;
                            } else {
                                branchOptionA.setContent(data.getStringCellValue().trim());
                                branchOptionA.setCreateTime(new Date());
                                branchOptionA.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionA);
                                if("A".equals(select.trim())) {
                                    branchTopic.setCorrectOptionId(branchOptionA.getId());
                                    branchTopicMapper.updateById(branchTopic);
                                }
                            }
                        } else if (3 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[B选项]内容不能为空！  ");
                                cellCount--;
                            } else {
                                if (aXZ) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j) +"列[A选项]内容为空，应调整此单元格内容至[A选项]  ");
                                }
                                branchOptionB.setContent(data.getStringCellValue().trim());
                                branchOptionB.setCreateTime(new Date());
                                branchOptionB.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionB);
                                if("B".equals(select.trim())) {
                                    branchTopic.setCorrectOptionId(branchOptionB.getId());
                                    branchTopicMapper.updateById(branchTopic);
                                }
                            }
                        } else if (4 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount == 2) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[C选项]之前AB选项均为空，此选项已经不能为空！");
                                }
                                cellCount--;
                            } else {
                                if (cellCount < 4) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[C选项]之前有列选项空白，应依次调整前移！  ");
                                }
                                branchOptionC.setContent(data.getStringCellValue().trim());
                                branchOptionC.setCreateTime(new Date());
                                branchOptionC.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionC);
                                if("C".equals(select.trim())) {
                                    branchTopic.setCorrectOptionId(branchOptionC.getId());
                                    branchTopicMapper.updateById(branchTopic);
                                }
                            }
                        } else if (5 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount < 3) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[D选项]之前至少有2列选项空白，此项不能为空且需依次调整前移！  ");
                                }
                            } else {
                                branchOptionD.setContent(data.getStringCellValue().trim());
                                branchOptionD.setCreateTime(new Date());
                                branchOptionD.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionD);
                                if("D".equals(select)) {
                                    branchTopic.setCorrectOptionId(branchOptionD.getId());
                                    branchTopicMapper.updateById(branchTopic);
                                }
                            }
                        } else if (6 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目解析]内容不能为空！  ");
                            } else {
                                branchTopic.setCorrectParse(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (7 == j) {
                            // 题目相关图片路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setImageUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (8 == j) {
                            // 题目相关视频路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setVideoUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        }
                    }
                } else {
                    break;
                }
                // branchTopicList.add(branchTopic);
            }catch (Exception e) {
                log.error("第"+i+"道题目导入错误:{}",e);
                topicError.append("第"+i+"道题目导入错误; \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入选择题总体数据存在错误:{}",topicError.toString());
            throw new BusinessException("批量导入选择题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入选择题信息成功";
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


    /**
     * 保存导入填空题信息（二期）
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @return 导入填空题信息是否成功
     */
    @Override
    @Transactional
    public String inputTianKongExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer type, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
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
                Boolean isEmpty = BranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 默认一道题最多两个填空选项
                    BranchTopic branchTopic =  new BranchTopic();
                    BranchOption branchOptionA = new BranchOption();
                    BranchOption branchOptionB = new BranchOption();

                    branchTopic.setType(type);
                    branchTopic.setTopicType(topicType);
                    branchTopic.setCompanyType(companyType);
                    branchTopic.setImageUrl(null);
                    branchTopic.setVideoUrl(null);
                    // 标记填空题1内容是不是空
                    Boolean aTK = false;
                    // 编列每行列内容
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if(0 == j) {
                            // 题目内容
                            if(StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目]内容不能为空！  ");
                                aTK = true;
                            } else {
                                branchTopic.setTitle(data.getStringCellValue().trim());
                                branchTopic.setPoint(data.getStringCellValue().trim());
                                branchTopic.setCreateTime(new Date());
                                branchTopicMapper.insert(branchTopic);
                            }
                        } else if(1 == j) {
                            // 填空项1
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[填空项1]内容不能为空！  ");
                            } else {
                                if (aTK) {
                                    topicError.append("第"+ (i+1) +"行第"+ (j) +"列[填空项1]内容内容为空，此内容应前移！  ");
                                }
                                branchOptionA.setContent(data.getStringCellValue().trim());
                                branchOptionA.setCreateTime(new Date());
                                branchOptionA.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionA);
                            }
                        } else if(2 == j) {
                            // 填空项2
                            if(StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchOptionB.setContent(data.getStringCellValue().trim());
                                branchOptionB.setCreateTime(new Date());
                                branchOptionB.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOptionB);
                            }
                        } else if(3 == j) {
                            // 题目解析
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目解析]内容不能为空！  ");
                            } else {
                                branchTopic.setCorrectParse(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (4 == j) {
                            // 题目相关图片路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setImageUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (5 == j) {
                            // 题目相关视频路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setVideoUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        }
                    }
                    // branchTopicList.add(branchTopic);
                } else {
                    break;
                }
            }catch (Exception e) {
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
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber 读取excel文件的第几个单元表
     * @param rowStart 从第几行开始读取
     * @param type 题目所属部门
     * @param topicType 题目所属类型
     * @param companyType 题目所属公司分类
     * @return 导入判断题信息是否成功
     */
    @Override
    @Transactional
    public String inputPanDuanExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer type, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
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
                Boolean isEmpty = BranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 一个题目一个判断选项
                    BranchTopic branchTopic =  new BranchTopic();
                    BranchOption branchOption = new BranchOption();

                    branchTopic.setType(type);
                    branchTopic.setTopicType(topicType);
                    branchTopic.setCompanyType(companyType);
                    branchTopic.setImageUrl(null);
                    branchTopic.setVideoUrl(null);
                    // 遍历每行内容
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            // 题目
                            if(StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目]内容不能为空！  ");
                            } else {
                                branchTopic.setTitle(data.getStringCellValue().trim());
                                branchTopic.setPoint(data.getStringCellValue().trim());
                                branchTopic.setCreateTime(new Date());
                                branchTopicMapper.insert(branchTopic);
                            }
                        } else if (1 == j) {
                            // 答案
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[答案]内容不能为空！  ");
                            } else {
                                if("0".equals(data.getStringCellValue())) {
                                    branchOption.setContent("错");
                                } else if ("1".equals(data.getStringCellValue())) {
                                    branchOption.setContent("对");
                                } else {
                                    topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[答案]内容只能为0或1 ");
                                }
                                branchOption.setCreateTime(new Date());
                                branchOption.setTopicId(branchTopic.getId());
                                branchOptionMapper.insert(branchOption);
                            }
                        } else if (2 == j) {
                            // 题目解析
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第"+ (i+1) +"行第"+ (j+1) +"列[题目解析]内容不能为空！  ");
                            } else {
                                branchTopic.setCorrectParse(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (3 == j) {
                            // 题目相关图片路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setImageUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        } else if (4 == j) {
                            // 题目相关视频路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                branchTopic.setVideoUrl(data.getStringCellValue().trim());
                                branchTopicMapper.updateById(branchTopic);
                            }
                        }
                    }
                    // branchTopicList.add(branchTopic);
                } else {
                    break;
                }
            }catch (Exception e) {
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
     * 删除部门下特定题目（单一道题、二期）
     * @param id 题目id
     * @return 删除题目成功与否
     */
    @Override
    @Transactional
    public void delTopic(Integer id) {
        // 获取对应题目
        BranchTopic branchTopic = branchTopicMapper.selectById(id);
        if (null != branchTopic) {
            if (!StringUtils.isEmpty(branchTopic.getImageUrl())) {
                // 删除题目关联图片
                File file = new File(branchTopic.getImageUrl());
                if (file.exists() && file.isFile()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目图片文件出错！");
                    }
                }
            }
            if (!StringUtils.isEmpty(branchTopic.getVideoUrl())) {
                // 删除题目关联视频
                File file = new File(branchTopic.getVideoUrl());
                if (file.exists() && file.isFile()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目视频文件出错！");
                    }
                }
            }
        }
        // 删除选定题目关联答案
        branchOptionMapper.delete(new QueryWrapper<BranchOption>().eq("topic_id",id));
        // 删除选定题目
        branchTopicMapper.delete(new QueryWrapper<BranchTopic>().eq("id", id));
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     * @param ids 选中题目id数组
     * @return 删除选中多道题成功与否
     */
    @Override
    @Transactional
    public void deleteTopics(Integer[] ids) {
        // 获取对应题目集合
        List<BranchTopic> branchTopics = branchTopicMapper.selectList(new QueryWrapper<BranchTopic>().in("id", ids));
        if (CollUtil.isNotEmpty(branchTopics)) {
            // 获取有对应图片地址题目合集
            List<BranchTopic> imageCollect = branchTopics.stream().filter(branchTopic -> !StringUtils.isEmpty(branchTopic.getImageUrl())).collect(Collectors.toList());
            // 获取有对应视频地址题目合集
            List<BranchTopic> videoCollect = branchTopics.stream().filter(branchTopic -> !StringUtils.isEmpty(branchTopic.getVideoUrl())).collect(Collectors.toList());
            // 删除题目关联图片、视频
            if (CollUtil.isNotEmpty(imageCollect)) {
                for (BranchTopic branchTopic : imageCollect) {
                    if (!StringUtils.isEmpty(branchTopic.getImageUrl())) {
                        File file = new File(branchTopic.getImageUrl());
                        if (file.exists() && file.isFile()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目图片文件出错！");
                            }
                        }
                    }
                }
            }
            if (CollUtil.isNotEmpty(videoCollect)) {
                for (BranchTopic branchTopic : videoCollect) {
                    if (!StringUtils.isEmpty(branchTopic.getVideoUrl())) {
                        File file = new File(branchTopic.getVideoUrl());
                        if (file.exists() && file.isFile()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目视频文件出错！");
                            }
                        }
                    }
                }
            }
        }
        // 删除选定题目关联答案
        branchOptionMapper.delete(new QueryWrapper<BranchOption>().in("topic_id",ids));
        // 删除选定题目
        branchTopicMapper.delete(new QueryWrapper<BranchTopic>().in("id", ids));
    }


    /**
     * 删除部门下所有题目（二期）
     * @param type 所属部门
     * @return 删除特定部门下题目成功与否
     */
    @Override
    @Transactional
    public void deleteTopicAll(Integer type) {
        // 获取公司分类部门下所有题目合集
        List<BranchTopic> branchTopicList = branchTopicMapper.selectList(new QueryWrapper<BranchTopic>().eq("type",type));
        List<Integer> ids = null;
        if(CollUtil.isNotEmpty(branchTopicList)) {
            ids = branchTopicList.stream().map(BranchTopic :: getId).collect(Collectors.toList());
            // 获取有对应图片地址题目合集
            List<BranchTopic> imageCollect = branchTopicList.stream().filter(branchTopic -> !StringUtils.isEmpty(branchTopic.getImageUrl())).collect(Collectors.toList());
            // 获取有对应视频地址题目合集
            List<BranchTopic> videoCollect = branchTopicList.stream().filter(branchTopic -> !StringUtils.isEmpty(branchTopic.getVideoUrl())).collect(Collectors.toList());
            // 删除题目关联图片、视频
            if (CollUtil.isNotEmpty(imageCollect)) {
                for (BranchTopic branchTopic : imageCollect) {
                    if (!StringUtils.isEmpty(branchTopic.getImageUrl())) {
                        File file = new File(branchTopic.getImageUrl());
                        if (file.isFile() && file.exists()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目图片文件出错！");
                            }
                        }
                    }
                }
            }
            if (CollUtil.isNotEmpty(videoCollect)) {
                for (BranchTopic branchTopic : videoCollect) {
                    if (!StringUtils.isEmpty(branchTopic.getVideoUrl())) {
                        File file = new File(branchTopic.getVideoUrl());
                        if (file.isFile() && file.exists()) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                throw new BusinessException("删除题目视频文件出错！");
                            }
                        }
                    }
                }
            }

        }

        // 批量删除题目相关答案信息
        branchOptionMapper.delete(new QueryWrapper<BranchOption>().in("topic_id",ids));
        // 批量删除题目
        branchTopicMapper.delete(new QueryWrapper<BranchTopic>().eq("type",type));
    }


    /**
     * 有条件批量导出选择题目（二期）
     * @param path 导出路径
     * @param branchTopicList 导出题目集合
     */
    @Override
    @Transactional
    public void exportXuanZeToExcel(String path, List<BranchTopic> branchTopicList) throws IOException {
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] options = {"A", "B", "C", "D"};
        ArrayList<String[]> list = new ArrayList<>();
        String[] xuanZeCellNames = {"题目", "答案", "A", "B", "C", "D", "题目解析(需要填写@来标注正确答案)", "相关图片地址路径", "相关视频地址路径"};
        // 表头数据加入list中
        list.add(xuanZeCellNames);
        // 遍历获取需导出excel文件数据
        for (int i = 0; i < branchTopicList.size(); i++) {
            String title = branchTopicList.get(i).getTitle();
            String answer = null;
            String context1 = null;
            String context2 = null;
            String context3 = null;
            String context4 = null;
            String correctParse = branchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (branchTopicList.get(i).getImageUrl() != null) {
                imageUrl = branchTopicList.get(i).getImageUrl();
            }
            if (branchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = branchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().in("topic_id", branchTopicList.get(i).getId()));
            if (branchOptionList.size() == 0) {
                throw new BusinessException("检索选择题答案出现错误！");
            }
            for (int j = 0; j < branchOptionList.size(); j++) {
                if (j ==0) {
                    context1 = branchOptionList.get(j).getContent();
                } else if (j == 1) {
                    context2 = branchOptionList.get(j).getContent();
                } else if (j == 2) {
                    context3 = branchOptionList.get(j).getContent();
                } else if (j == 3) {
                    context4 = branchOptionList.get(j).getContent();
                }
                if (branchOptionList.get(j).getId().equals(branchTopicList.get(i).getCorrectOptionId())) {
                    answer = options[j];
                }
            }
            ArrayList<Object> arrayList = new ArrayList<>();
            arrayList.add(title);
            arrayList.add(answer);
            arrayList.add(context1);
            arrayList.add(context2);
            arrayList.add(context3);
            arrayList.add(context4);
            arrayList.add(correctParse);
            arrayList.add(imageUrl);
            arrayList.add(videoUrl);
            String[] str = arrayList.toArray(new String[arrayList.size()]);
            list.add(str);
        }

        // 写入excel
        // 创建一个内存excel对象
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建一个表格
        HSSFSheet sheet = workbook.createSheet("选择题");
        // 创建表头
        // 获取表头内容
        String[] headerStr = list.get(0);
        HSSFRow headerRow = sheet.createRow(0);
        // 设置列宽
        sheet.setColumnWidth(0,5000);
        sheet.setColumnWidth(1,500);
        sheet.setColumnWidth(2,500);
        sheet.setColumnWidth(3,500);
        sheet.setColumnWidth(4,500);
        sheet.setColumnWidth(5,500);
        sheet.setColumnWidth(6,5000);
        sheet.setColumnWidth(7,5000);
        sheet.setColumnWidth(8,5000);

        // 设置头单元格样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        // 水平居中
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表头字体
        HSSFFont heaherFont = workbook.createFont();
        heaherFont.setColor(HSSFColor.VIOLET.index);
        heaherFont.setFontName("宋体");
        headerStyle.setFont(heaherFont);

        // 遍历定义表头内容
        for (int i = 0; i < headerStr.length; i++) {
            // 创建一个单元格
            HSSFCell headerCell = headerRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerStr[i]);
        }

        // 设置表体样式
        HSSFCellStyle bodyStyle = workbook.createCellStyle();
        // 水平居中
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表体字体
        HSSFFont bodyFont = workbook.createFont();
        bodyFont.setColor(HSSFColor.BLUE.index);
        bodyFont.setFontName("微软雅黑");
        bodyStyle.setFont(heaherFont);

        // 遍历设置标体内容
        for (int row = 1; row < list.size(); row++) {
            // 输出的行数据
            String[] temp = list.get(row);
            // 创建行
            HSSFRow bodyRow = sheet.createRow(row);
            // 遍历创建改行列值
            for (int cell = 0; cell < temp.length; cell++) {
                HSSFCell bodyCell = bodyRow.createCell(cell);
                bodyCell.setCellStyle(bodyStyle);
                bodyCell.setCellValue(temp[cell]);
            }
        }

        // 将内存创建的excel对象，输出到Excel文件中
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 有条件批量导出填空题目（二期）
     * @param path 导出路径
     * @param branchTopicList 导出题目集合
     */
    @Override
    @Transactional
    public void exportTianKongToExcel(String path, List<BranchTopic> branchTopicList) throws IOException {
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String[]> list = new ArrayList<>();
        String[] tianKongCellNames = {"题目","填空项1","填空项2","题目解析(需要填写@来标注正确答案)","相关图片地址路径","相关视频地址路径"};
        // 表头数据加入list中
        list.add(tianKongCellNames);
        // 遍历获取需导出excel文件数据
        for (int i = 0; i < branchTopicList.size(); i++) {
            String title = branchTopicList.get(i).getTitle();
            String tContext1 = null;
            String tContext2 = null;
            String correctParse = branchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (branchTopicList.get(i).getImageUrl() != null) {
                imageUrl = branchTopicList.get(i).getImageUrl();
            }
            if (branchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = branchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().in("topic_id", branchTopicList.get(i).getId()));
            if (branchOptionList.size() == 0) {
                throw new BusinessException("检索选择题答案出现错误！");
            }
            for (int j = 0; j < branchOptionList.size(); j++) {
                if (j ==0) {
                    tContext1 = branchOptionList.get(j).getContent();
                } else if (j == 1) {
                    tContext2 = branchOptionList.get(j).getContent();
                }
            }
            ArrayList<Object> arrayList = new ArrayList<>();
            arrayList.add(title);
            arrayList.add(tContext1);
            arrayList.add(tContext2);
            arrayList.add(correctParse);
            arrayList.add(imageUrl);
            arrayList.add(videoUrl);
            String[] str = arrayList.toArray(new String[arrayList.size()]);
            list.add(str);
        }

        // 写入excel
        // 创建一个内存excel对象
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建一个表格
        HSSFSheet sheet = workbook.createSheet("选择题");
        // 创建表头
        // 获取表头内容
        String[] headerStr = list.get(0);
        HSSFRow headerRow = sheet.createRow(0);
        // 设置列宽
        sheet.setColumnWidth(0,5000);
        sheet.setColumnWidth(1,2000);
        sheet.setColumnWidth(2,2000);
        sheet.setColumnWidth(3,5000);
        sheet.setColumnWidth(4,5000);
        sheet.setColumnWidth(5,5000);

        // 设置头单元格样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        // 水平居中
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表头字体
        HSSFFont heaherFont = workbook.createFont();
        heaherFont.setColor(HSSFColor.VIOLET.index);
        heaherFont.setFontName("宋体");
        headerStyle.setFont(heaherFont);

        // 遍历定义表头内容
        for (int i = 0; i < headerStr.length; i++) {
            // 创建一个单元格
            HSSFCell headerCell = headerRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerStr[i]);
        }

        // 设置表体样式
        HSSFCellStyle bodyStyle = workbook.createCellStyle();
        // 水平居中
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表体字体
        HSSFFont bodyFont = workbook.createFont();
        bodyFont.setColor(HSSFColor.BLUE.index);
        bodyFont.setFontName("微软雅黑");
        bodyStyle.setFont(heaherFont);

        // 遍历设置标体内容
        for (int row = 1; row < list.size(); row++) {
            // 输出的行数据
            String[] temp = list.get(row);
            // 创建行
            HSSFRow bodyRow = sheet.createRow(row);
            // 遍历创建改行列值
            for (int cell = 0; cell < temp.length; cell++) {
                HSSFCell bodyCell = bodyRow.createCell(cell);
                bodyCell.setCellStyle(bodyStyle);
                bodyCell.setCellValue(temp[cell]);
            }
        }

        // 将内存创建的excel对象，输出到Excel文件中
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 有条件批量导出判断题目（二期）
     * @param path 导出路径
     * @param branchTopicList 导出题目集合
     */
    @Override
    @Transactional
    public void exportPanDuanToExcel(String path, List<BranchTopic> branchTopicList) throws IOException {
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String[]> list = new ArrayList<>();
        String[] panDuanCellNames = {"题目","答案","题目解析(需要填写@来标注正确答案)","相关图片地址路径","相关视频地址路径"};
        // 表头数据加入list中
        list.add(panDuanCellNames);
        // 遍历获取需导出excel文件数据
        for (int i = 0; i < branchTopicList.size(); i++) {
            String title = branchTopicList.get(i).getTitle();
            String answer = null;
            String correctParse = branchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (branchTopicList.get(i).getImageUrl() != null) {
                imageUrl = branchTopicList.get(i).getImageUrl();
            }
            if (branchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = branchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().in("topic_id", branchTopicList.get(i).getId()));
            if (branchOptionList.size() == 0) {
                throw new BusinessException("检索选择题答案出现错误！");
            }
            answer = branchOptionList.get(0).getContent().equals("正确") == true ? "1" : "0";
            ArrayList<Object> arrayList = new ArrayList<>();
            arrayList.add(title);
            arrayList.add(answer);
            arrayList.add(correctParse);
            arrayList.add(imageUrl);
            arrayList.add(videoUrl);
            String[] str = arrayList.toArray(new String[arrayList.size()]);
            list.add(str);
        }

        // 写入excel
        // 创建一个内存excel对象
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建一个表格
        HSSFSheet sheet = workbook.createSheet("选择题");
        // 创建表头
        // 获取表头内容
        String[] headerStr = list.get(0);
        HSSFRow headerRow = sheet.createRow(0);
        // 设置列宽
        sheet.setColumnWidth(0,5000);
        sheet.setColumnWidth(1,500);
        sheet.setColumnWidth(3,5000);
        sheet.setColumnWidth(4,5000);
        sheet.setColumnWidth(5,5000);

        // 设置头单元格样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        // 水平居中
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表头字体
        HSSFFont heaherFont = workbook.createFont();
        heaherFont.setColor(HSSFColor.VIOLET.index);
        heaherFont.setFontName("宋体");
        headerStyle.setFont(heaherFont);

        // 遍历定义表头内容
        for (int i = 0; i < headerStr.length; i++) {
            // 创建一个单元格
            HSSFCell headerCell = headerRow.createCell(i);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerStr[i]);
        }

        // 设置表体样式
        HSSFCellStyle bodyStyle = workbook.createCellStyle();
        // 水平居中
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 设置表体字体
        HSSFFont bodyFont = workbook.createFont();
        bodyFont.setColor(HSSFColor.BLUE.index);
        bodyFont.setFontName("微软雅黑");
        bodyStyle.setFont(heaherFont);

        // 遍历设置标体内容
        for (int row = 1; row < list.size(); row++) {
            // 输出的行数据
            String[] temp = list.get(row);
            // 创建行
            HSSFRow bodyRow = sheet.createRow(row);
            // 遍历创建该行列值
            for (int cell = 0; cell < temp.length; cell++) {
                HSSFCell bodyCell = bodyRow.createCell(cell);
                bodyCell.setCellStyle(bodyStyle);
                if (cell == 1) {
                    bodyCell.setCellValue("对".equals(temp[cell]) ? 1 : 0);
                }
                bodyCell.setCellValue(temp[cell]);
            }
        }

        // 将内存创建的excel对象，输出到Excel文件中
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
    }


    /**
     * 查看部门专业题库信息 (二期)
     * @param page 页码
     * @param buMenType 部门类型
     * @param topicName 题目名称（用于模糊搜索）
     * @param type 员工所属公司类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @return 部门题目数据集合
     */
    @Override
    @Transactional
    public List<WisdomLibraryVo> getBuMenTopicList(int page, int buMenType, String topicName, Integer type) {
        List<WisdomLibraryVo> wisdomLibraryVoArrayList = new ArrayList<WisdomLibraryVo>();
        // 特殊化处理，当搜索党群纪检时，即branch_id = 8 时要获取branch_id = 8和branch_id = 9两个部门题目
        List<BranchTopic> topicArrayList = new ArrayList<>();
        List<BranchTopic> currBranchTopics = new ArrayList<>();
        if (8 == buMenType) {
            // 现宁夏智库搜索直接针对正确解析搜索
            List<BranchTopic> branchTopics01 = branchTopicMapper.selectList(new QueryWrapper<BranchTopic>().eq("type", 8).eq("company_type", type).like(org.apache.commons.lang.StringUtils.isNotEmpty(topicName), "correct_parse", topicName));
            List<BranchTopic> branchTopics02 = branchTopicMapper.selectList(new QueryWrapper<BranchTopic>().eq("type", 9).eq("company_type", type).like(org.apache.commons.lang.StringUtils.isNotEmpty(topicName), "correct_parse", topicName));
            currBranchTopics.addAll(branchTopics01);
            currBranchTopics.addAll(branchTopics02);
        } else {
            // 由于在基于正确解析搜索基础上增加去重功能，所以不嫩直接按页进行搜索，需先基于正确解析搜索出所有题目，去重后再根据页码所在范围获取题目
            currBranchTopics = branchTopicMapper.selectList(new QueryWrapper<BranchTopic>().eq("type", buMenType).eq("company_type", type).like(org.apache.commons.lang.StringUtils.isNotEmpty(topicName), "correct_parse", topicName));
        }
        if (CollUtil.isEmpty(currBranchTopics)) {
            return null;
        }
        // 重写equal() 与 hashcode()方法，利用strean流distinct()方法根据correctParse字段是否相等去重
        currBranchTopics = currBranchTopics.stream().distinct().collect(Collectors.toList());
        if (currBranchTopics.size() > (page - 1 ) * 10) {
            int endPage = currBranchTopics.size() > page * 10 ? page * 10 : currBranchTopics.size();
            for (int i = (page - 1) * 10; i < endPage; i++) {
                BranchTopic branchTopic = currBranchTopics.get(i);
                topicArrayList.add(branchTopic);
            }
        }
        // 遍历公司部门下题库题目
        for(BranchTopic branchTopic : topicArrayList) {
            WisdomLibraryVo vo = new WisdomLibraryVo();
            vo.setCorrectParse(branchTopic.getCorrectParse());
            List<String> optionList = new ArrayList<String>();
            // 选择题
            if(branchTopic.getTopicType().intValue() == 0) {
                optionList.add(branchOptionMapper.selectById(branchTopic.getCorrectOptionId()).getContent());
                vo.setOptionList(optionList);
            }
            // 填空题
            if(branchTopic.getTopicType().intValue() == 1) {
                List<BranchOption> branchOptionList = branchOptionMapper.selectList(new QueryWrapper<BranchOption>().eq("topic_id", branchTopic.getId()));
                for(BranchOption branchOption : branchOptionList) {
                    optionList.add(branchOption.getContent());
                }
                vo.setOptionList(optionList);
            }
            // 判断题
            if(branchTopic.getTopicType().intValue() == 2) {
                optionList.add(branchOptionMapper.selectOne(new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId())).getContent());
                vo.setOptionList(optionList);
            }
            wisdomLibraryVoArrayList.add(vo);
        }

        return wisdomLibraryVoArrayList;
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
    @Override
    public Result selectPages(String title, Integer type, int page, int limit, Integer companyType) {
        IPage<BranchTopic> pages = branchTopicMapper.selectPage(new Page<BranchTopic>(page, limit),
                new QueryWrapper<BranchTopic>().eq(null !=type && 0!=type , "type", type).eq("company_type",companyType).like(!StringUtils.isEmpty(title), "title", title).orderByDesc("id"));
        return ResultLayuiTable.newSuccess(pages.getTotal(),pages.getRecords());
    }


    /**
     * 新增或修改题目（二期）
     * @param branchTopicHouTaiVo 部门题库题目对象
     * @return 新增或修改题目成功与否
     */
    @Override
    @Transactional
    public Result editBranchTopic(BranchTopicHouTaiVo branchTopicHouTaiVo) {
        if(null == branchTopicHouTaiVo.getId()) {
            // 新增
            BranchTopic branchTopic =new BranchTopic();
            branchTopic.setType(branchTopicHouTaiVo.getType());
            branchTopic.setTopicType(branchTopicHouTaiVo.getTopicType());
            branchTopic.setTitle(branchTopicHouTaiVo.getTitle());
            branchTopic.setCreateTime(new Date());
            branchTopic.setPoint(branchTopicHouTaiVo.getTitle());
            branchTopic.setCorrectParse(branchTopicHouTaiVo.getCorrectParse());
            branchTopic.setCompanyType(branchTopicHouTaiVo.getCompanyType());
            branchTopic.setImageUrl(null);
            branchTopic.setVideoUrl(null);
            if (!StringUtils.isEmpty(branchTopicHouTaiVo.getImageUrl())) {
                branchTopic.setImageUrl(branchTopicHouTaiVo.getImageUrl());
            }
            if (!StringUtils.isEmpty(branchTopicHouTaiVo.getVideoUrl())) {
                branchTopic.setVideoUrl(branchTopicHouTaiVo.getVideoUrl());
            }
            branchTopicMapper.insert(branchTopic);

            if(0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 =false;
                boolean c3 =false;
                boolean c4 =false;
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    c3 = true;
                }
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for (int i = 0;i < 4;i++) {
                    if (i == num) {
                        // 保存正确选项
                        BranchOption b1= new BranchOption();
                        b1.setTopicId(branchTopic.getId());
                        b1.setContent(branchTopicHouTaiVo.getContext1());
                        b1.setCreateTime(new Date());
                        branchOptionMapper.insert(b1);

                        branchTopic.setCorrectOptionId(b1.getId());
                        branchTopicMapper.updateById(branchTopic);
                    } else {
                        if (!c2) {
                            BranchOption b2= new BranchOption();
                            b2.setTopicId(branchTopic.getId());
                            b2.setContent(branchTopicHouTaiVo.getContext2());
                            b2.setCreateTime(new Date());
                            branchOptionMapper.insert(b2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            BranchOption b3= new BranchOption();
                            b3.setTopicId(branchTopic.getId());
                            b3.setContent(branchTopicHouTaiVo.getContext3());
                            b3.setCreateTime(new Date());
                            branchOptionMapper.insert(b3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            BranchOption b4= new BranchOption();
                            b4.setTopicId(branchTopic.getId());
                            b4.setContent(branchTopicHouTaiVo.getContext4());
                            b4.setCreateTime(new Date());
                            branchOptionMapper.insert(b4);
                            c4 = true;
                            continue;
                        }
                    }
                }
            } else if (1 == branchTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    BranchOption b1= new BranchOption();
                    b1.setTopicId(branchTopic.getId());
                    b1.setContent(branchTopicHouTaiVo.getTContext1());
                    b1.setCreateTime(new Date());
                    branchOptionMapper.insert(b1);
                }
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    BranchOption b2= new BranchOption();
                    b2.setTopicId(branchTopic.getId());
                    b2.setContent(branchTopicHouTaiVo.getTContext2());
                    b2.setCreateTime(new Date());
                    branchOptionMapper.insert(b2);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                BranchOption b1= new BranchOption();
                b1.setTopicId(branchTopic.getId());
                b1.setContent(branchTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                b1.setCreateTime(new Date());
                branchOptionMapper.insert(b1);
            }
        } else {
            // 修改
            BranchTopic branchTopic = branchTopicMapper.selectById(branchTopicHouTaiVo.getId());
            branchTopic.setTitle(branchTopicHouTaiVo.getTitle());
            branchTopic.setCorrectParse(branchTopicHouTaiVo.getCorrectParse());
            branchTopic.setPoint(branchTopicHouTaiVo.getTitle());
            if (StringUtils.isEmpty(branchTopicHouTaiVo.getImageUrl().trim())) {
                branchTopic.setImageUrl(null);
            } else {
                branchTopic.setImageUrl(branchTopicHouTaiVo.getImageUrl().trim());
            }
            if (StringUtils.isEmpty(branchTopicHouTaiVo.getVideoUrl().trim())) {
                branchTopic.setVideoUrl(null);
            } else {
                branchTopic.setVideoUrl(branchTopicHouTaiVo.getVideoUrl().trim());
            }
            branchTopicMapper.updateById(branchTopic);
            // 删除题目对应答案数据
            branchOptionMapper.delete(new QueryWrapper<BranchOption>().eq("topic_id",branchTopic.getId()));

            if (0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 =false;
                boolean c3 =false;
                boolean c4 =false;
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    c3 = true;
                }
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for(int i = 0;i < 4;i++) {
                    if(i == num) {
                        // 保存选择题正确选项
                        BranchOption b1= new BranchOption();
                        b1.setTopicId(branchTopic.getId());
                        b1.setContent(branchTopicHouTaiVo.getContext1());
                        b1.setCreateTime(new Date());
                        branchOptionMapper.insert(b1);

                        branchTopic.setCorrectOptionId(b1.getId());
                        branchTopicMapper.updateById(branchTopic);
                    } else {
                        if (!c2) {
                            BranchOption b2= new BranchOption();
                            b2.setTopicId(branchTopic.getId());
                            b2.setContent(branchTopicHouTaiVo.getContext2());
                            b2.setCreateTime(new Date());
                            branchOptionMapper.insert(b2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            BranchOption b3= new BranchOption();
                            b3.setTopicId(branchTopic.getId());
                            b3.setContent(branchTopicHouTaiVo.getContext3());
                            b3.setCreateTime(new Date());
                            branchOptionMapper.insert(b3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            BranchOption b4= new BranchOption();
                            b4.setTopicId(branchTopic.getId());
                            b4.setContent(branchTopicHouTaiVo.getContext4());
                            b4.setCreateTime(new Date());
                            branchOptionMapper.insert(b4);
                            c4 = true;
                            continue;
                        }
                    }

                }
            } else if (1 == branchTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    BranchOption b1= new BranchOption();
                    b1.setTopicId(branchTopic.getId());
                    b1.setContent(branchTopicHouTaiVo.getTContext1());
                    b1.setCreateTime(new Date());
                    branchOptionMapper.insert(b1);
                }
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    BranchOption b2= new BranchOption();
                    b2.setTopicId(branchTopic.getId());
                    b2.setContent(branchTopicHouTaiVo.getTContext2());
                    b2.setCreateTime(new Date());
                    branchOptionMapper.insert(b2);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                BranchOption b1= new BranchOption();
                b1.setTopicId(branchTopic.getId());
                b1.setContent(branchTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                b1.setCreateTime(new Date());
                branchOptionMapper.insert(b1);
            }
        }

        return Result.newSuccess(null == branchTopicHouTaiVo.getId() ? "新增活动赛题目信息成功" : "修改活动赛题目信息成功");
    }


}
