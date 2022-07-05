package com.wangchen.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.entity.BranchOption;
import com.wangchen.entity.BranchTopic;
import com.wangchen.entity.FeiBranchOption;
import com.wangchen.entity.FeiBranchTopic;
import com.wangchen.mapper.FeiBranchOptionMapper;
import com.wangchen.mapper.FeiBranchTopicMapper;
import com.wangchen.service.FeiBranchTopicService;
import com.wangchen.vo.BranchTopicHouTaiVo;
import com.wangchen.vo.WisdomLibraryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 非专业部门题库表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */

@Slf4j
@Service
public class FeiBranchTopicServiceImpl extends ServiceImpl<FeiBranchTopicMapper, FeiBranchTopic> implements FeiBranchTopicService {

    @Autowired
    private FeiBranchTopicMapper feiBranchTopicMapper;

    @Autowired
    private FeiBranchOptionMapper feiBranchOptionMapper;

    @Override
    public List<FeiBranchTopic> listTopicRandom(Integer num) {
        return feiBranchTopicMapper.listTopicRandoms(num);
    }

    /**
     * 删除选定题目（一道题、二期）
     *
     * @param id 题目id
     */
    @Override
    @Transactional
    public void delTopic(Integer id) {
        // 先删除题目对应答案信息
        feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>().eq("topic_id", id.intValue()));
        // 获取对应题目
        FeiBranchTopic feiBranchTopic = feiBranchTopicMapper.selectById(id);
        if (null != feiBranchTopic) {
            if (!StringUtils.isEmpty(feiBranchTopic.getImageUrl())) {
                // 删除题目关联图片
                File file = new File(feiBranchTopic.getImageUrl());
                if (file.isFile() && file.exists()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目图片文件出错！");
                    }
                }
            }
            if (!StringUtils.isEmpty(feiBranchTopic.getVideoUrl())) {
                // 删除题目关联视频
                File file = new File(feiBranchTopic.getVideoUrl());
                if (file.isFile() && file.exists()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        throw new BusinessException("删除题目视频文件出错！");
                    }
                }
            }
        } else {
            throw new BusinessException("没有选中要删除的题目");
        }
        // 再删除题目
        feiBranchTopicMapper.delete(new QueryWrapper<FeiBranchTopic>().eq("id", id.intValue()));
    }


    /**
     * 批量删除部门下选定题目（多道题、二期）
     *
     * @param ids 题目id数组
     */
    @Override
    @Transactional
    public void deleteTopics(Integer[] ids) {
        // 获取选定题目合集
        List<FeiBranchTopic> feiBranchTopics = feiBranchTopicMapper.selectList(new QueryWrapper<FeiBranchTopic>().in("id", ids));
        if (CollUtil.isNotEmpty(feiBranchTopics)) {
            // 获取有对应图片地址题目合集
            List<FeiBranchTopic> imageCollect = feiBranchTopics.stream().filter(feiBranchTopic -> !StringUtils.isEmpty(feiBranchTopic.getImageUrl())).collect(Collectors.toList());
            // 获取有对应视频地址题目合集
            List<FeiBranchTopic> videoCollect = feiBranchTopics.stream().filter(feiBranchTopic -> !StringUtils.isEmpty(feiBranchTopic.getVideoUrl())).collect(Collectors.toList());
            // 删除题目关联图片、视频
            if (CollUtil.isNotEmpty(imageCollect)) {
                for (FeiBranchTopic feiBranchTopic : imageCollect) {
                    if (!StringUtils.isEmpty(feiBranchTopic.getImageUrl())) {
                        File file = new File(feiBranchTopic.getImageUrl());
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
                for (FeiBranchTopic feiBranchTopic : videoCollect) {
                    if (!StringUtils.isEmpty(feiBranchTopic.getVideoUrl())) {
                        File file = new File(feiBranchTopic.getVideoUrl());
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
        } else {
            throw new BusinessException("没有选中要删除的题目");
        }
        // 先删除题目对应答案信息
        feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>().in("topic_id", ids));
        // 再删除题目
        feiBranchTopicMapper.delete(new QueryWrapper<FeiBranchTopic>().in("id", ids));
    }


    /**
     * 删除对应公司类型下所有题目（二期）
     *
     * @param companyType 所属公司类型
     */
    @Override
    @Transactional
    public void deleteTopicAll(Integer companyType) {
        // 获取公司分类下必知必会的所有题目集合
        List<FeiBranchTopic> feiBranchTopics = feiBranchTopicMapper.selectList(new QueryWrapper<FeiBranchTopic>().eq("company_type", companyType));
        List<Integer> ids = null;
        if (CollUtil.isNotEmpty(feiBranchTopics)) {
            ids = feiBranchTopics.stream().map(FeiBranchTopic::getId).collect(Collectors.toList());
            // 获取有对应图片地址题目合集
            List<FeiBranchTopic> imageCollect = feiBranchTopics.stream().filter(feiBranchTopic -> !StringUtils.isEmpty(feiBranchTopic.getImageUrl())).collect(Collectors.toList());
            // 获取有对应视频地址题目合集
            List<FeiBranchTopic> videoCollect = feiBranchTopics.stream().filter(feiBranchTopic -> !StringUtils.isEmpty(feiBranchTopic.getVideoUrl())).collect(Collectors.toList());
            // 删除题目关联图片、视频
            if (CollUtil.isNotEmpty(imageCollect)) {
                for (FeiBranchTopic feiBranchTopic : imageCollect) {
                    if (!StringUtils.isEmpty(feiBranchTopic.getImageUrl())) {
                        File file = new File(feiBranchTopic.getImageUrl());
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
                for (FeiBranchTopic feiBranchTopic : videoCollect) {
                    if (!StringUtils.isEmpty(feiBranchTopic.getVideoUrl())) {
                        File file = new File(feiBranchTopic.getVideoUrl());
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
        } else {
            throw new BusinessException("没有选中要删除的题目");
        }

        // 批量删除题目相关答案信息
        feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>().in("topic_id", ids));
        // 批量删除题目
        feiBranchTopicMapper.delete(new QueryWrapper<FeiBranchTopic>().eq("company_type", companyType));
    }


    /**
     * 有条件导出选择题(二期)
     */
    @Override
    @Transactional
    public void exportXuanZeToExcel(String path, List<FeiBranchTopic> feiBranchTopicList) throws IOException {
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
        for (int i = 0; i < feiBranchTopicList.size(); i++) {
            String title = feiBranchTopicList.get(i).getTitle();
            String answer = null;
            String context1 = null;
            String context2 = null;
            String context3 = null;
            String context4 = null;
            String correctParse = feiBranchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (feiBranchTopicList.get(i).getImageUrl() != null) {
                imageUrl = feiBranchTopicList.get(i).getImageUrl();
            }
            if (feiBranchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = feiBranchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<FeiBranchOption> branchOptionList = feiBranchOptionMapper.selectList(new QueryWrapper<FeiBranchOption>().in("topic_id", feiBranchTopicList.get(i).getId()));
            if (branchOptionList.size() == 0) {
                throw new BusinessException("检索选择题答案出现错误！");
            }
            for (int j = 0; j < branchOptionList.size(); j++) {
                if (j == 0) {
                    context1 = branchOptionList.get(j).getContent();
                } else if (j == 1) {
                    context2 = branchOptionList.get(j).getContent();
                } else if (j == 2) {
                    context3 = branchOptionList.get(j).getContent();
                } else if (j == 3) {
                    context4 = branchOptionList.get(j).getContent();
                }
                if (branchOptionList.get(j).getId().equals(feiBranchTopicList.get(i).getCorrectOptionId())) {
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
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 500);
        sheet.setColumnWidth(2, 500);
        sheet.setColumnWidth(3, 500);
        sheet.setColumnWidth(4, 500);
        sheet.setColumnWidth(5, 500);
        sheet.setColumnWidth(6, 5000);
        sheet.setColumnWidth(7, 5000);
        sheet.setColumnWidth(8, 5000);

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
     * 有条件导出填空题(二期)
     */
    @Override
    @Transactional
    public void exportTianKongToExcel(String path, List<FeiBranchTopic> feiBranchTopicList) throws IOException {
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String[]> list = new ArrayList<>();
        String[] tianKongCellNames = {"题目", "填空项1", "填空项2", "题目解析(需要填写@来标注正确答案)", "相关图片地址路径", "相关视频地址路径"};
        // 表头数据加入list中
        list.add(tianKongCellNames);
        // 遍历获取需导出excel文件数据
        for (int i = 0; i < feiBranchTopicList.size(); i++) {
            String title = feiBranchTopicList.get(i).getTitle();
            String tContext1 = null;
            String tContext2 = null;
            String correctParse = feiBranchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (feiBranchTopicList.get(i).getImageUrl() != null) {
                imageUrl = feiBranchTopicList.get(i).getImageUrl();
            }
            if (feiBranchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = feiBranchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<FeiBranchOption> branchOptionList = feiBranchOptionMapper.selectList(new QueryWrapper<FeiBranchOption>().in("topic_id", feiBranchTopicList.get(i).getId()));
            if (branchOptionList.size() == 0) {
                throw new BusinessException("检索选择题答案出现错误！");
            }
            for (int j = 0; j < branchOptionList.size(); j++) {
                if (j == 0) {
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
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 2000);
        sheet.setColumnWidth(2, 2000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);

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
     * 有条件导出判断题(二期)
     */
    @Override
    public void exportPanDuanToExcel(String path, List<FeiBranchTopic> feiBranchTopicList) throws IOException {
        File file = new File(path);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String[]> list = new ArrayList<>();
        String[] panDuanCellNames = {"题目", "答案", "题目解析(需要填写@来标注正确答案)", "相关图片地址路径", "相关视频地址路径"};
        // 表头数据加入list中
        list.add(panDuanCellNames);
        // 遍历获取需导出excel文件数据
        for (int i = 0; i < feiBranchTopicList.size(); i++) {
            String title = feiBranchTopicList.get(i).getTitle();
            String answer = null;
            String correctParse = feiBranchTopicList.get(i).getCorrectParse();
            String imageUrl = null;
            String videoUrl = null;
            if (feiBranchTopicList.get(i).getImageUrl() != null) {
                imageUrl = feiBranchTopicList.get(i).getImageUrl();
            }
            if (feiBranchTopicList.get(i).getVideoUrl() != null) {
                videoUrl = feiBranchTopicList.get(i).getVideoUrl();
            }

            // 获取对应答案集合
            List<FeiBranchOption> branchOptionList = feiBranchOptionMapper.selectList(new QueryWrapper<FeiBranchOption>().in("topic_id", feiBranchTopicList.get(i).getId()));
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
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 500);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);

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
     * 查看非专业题库信息 (二期)
     *
     * @param page      页码
     * @param type      人员所属公司类型 （1:自有公司，2代维公司，3设计和监理公司）
     * @param topicName 题目名称（用于模糊搜索）
     * @return 非专业题库（必知必会）题目数据集合
     */
    @Override
    @Transactional
    public List<WisdomLibraryVo> getFeiTiKuTopicList(Integer page, Integer type, String topicName) {
        List<WisdomLibraryVo> wisdomLibraryVoArrayList = new ArrayList<WisdomLibraryVo>();
        // 由于现升级功能为根据题目解析搜索，并具有去重功能，所以不能直接按每页10条进行搜索，这样存在"重复题目"去重后智库当页题目不足10条，再搜索补足仍存在与前面题目重复的问题，所以现在采用一次搜索所有题目去重后根据页码获取题目
        List<FeiBranchTopic> feiBranchTopicList = feiBranchTopicMapper.selectList(new QueryWrapper<FeiBranchTopic>().eq("company_type",type).like(StringUtils.isNotEmpty(topicName),"correct_parse",topicName));
        // 重写equal() 与 hashcode()方法，利用strean流distinct()方法根据correctParse字段是否相等去重
        feiBranchTopicList = feiBranchTopicList.stream().distinct().collect(Collectors.toList());
        List<FeiBranchTopic> currFeiBranchTopicList = new ArrayList<>();
        if (CollUtil.isEmpty(feiBranchTopicList)) {
            return null;
        }
        // 默认每页数据量10条
        if ((page - 1) * 10 >= feiBranchTopicList.size()) {
            return null;
        } else {
            int endPage = feiBranchTopicList.size() > page * 10 ? page * 10 : feiBranchTopicList.size();
            for (int i = (page - 1) * 10; i < endPage; i++) {
                FeiBranchTopic feiBranchTopic = feiBranchTopicList.get(i);
                currFeiBranchTopicList.add(feiBranchTopic);
            }
        }
        for (FeiBranchTopic feiBranchTopic : currFeiBranchTopicList) {
            WisdomLibraryVo vo = new WisdomLibraryVo();
            vo.setCorrectParse(feiBranchTopic.getCorrectParse());
            List<String> optionList = new ArrayList<String>();
            // 选择题
            if (feiBranchTopic.getTopicType().intValue() == 0) {
                optionList.add(feiBranchOptionMapper.selectById(feiBranchTopic.getCorrectOptionId()).getContent());
                vo.setOptionList(optionList);
            }
            // 填空题
            if (feiBranchTopic.getTopicType().intValue() == 1) {
                List<FeiBranchOption> feiBranchOptionList = feiBranchOptionMapper.selectList(new QueryWrapper<FeiBranchOption>()
                        .eq("topic_id", feiBranchTopic.getId()));
                for (FeiBranchOption branchOption : feiBranchOptionList) {
                    optionList.add(branchOption.getContent());
                }
                vo.setOptionList(optionList);
            }
            // 判断题
            if (feiBranchTopic.getTopicType().intValue() == 2) {
                optionList.add(feiBranchOptionMapper.selectOne(new QueryWrapper<FeiBranchOption>()
                        .eq("topic_id", feiBranchTopic.getId())).getContent());
                vo.setOptionList(optionList);
            }
            wisdomLibraryVoArrayList.add(vo);
        }

        return wisdomLibraryVoArrayList;
    }


    /**
     * 新增修改必知必会题目(二期)
     *
     * @param branchTopicHouTaiVo 必知必会题目对象
     * @return 新增修改必知必会题目成功与否
     */
    @Override
    @Transactional
    public Result editBranchTopic(BranchTopicHouTaiVo branchTopicHouTaiVo) {
        //新增
        if (null == branchTopicHouTaiVo.getId()) {
            FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
            feiBranchTopic.setTopicType(branchTopicHouTaiVo.getTopicType());
            feiBranchTopic.setTitle(branchTopicHouTaiVo.getTitle());
            feiBranchTopic.setCreateTime(new Date());
            feiBranchTopic.setPoint(branchTopicHouTaiVo.getTitle());
            feiBranchTopic.setCorrectParse(branchTopicHouTaiVo.getCorrectParse());
            feiBranchTopic.setCompanyType(branchTopicHouTaiVo.getCompanyType());
            feiBranchTopic.setImageUrl(null);
            feiBranchTopic.setVideoUrl(null);
            if (!StringUtils.isEmpty(branchTopicHouTaiVo.getImageUrl())) {
                feiBranchTopic.setImageUrl(branchTopicHouTaiVo.getImageUrl());
            }
            if (!StringUtils.isEmpty(branchTopicHouTaiVo.getVideoUrl())) {
                feiBranchTopic.setVideoUrl(branchTopicHouTaiVo.getVideoUrl());
            }
            feiBranchTopicMapper.insert(feiBranchTopic);

            if (0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                Random random = new Random();
                int num = random.nextInt(4);
                boolean c2 = false;
                boolean c3 = false;
                boolean c4 = false;
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    c3 = true;
                    c4 = true;
                }
                if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for (int i = 0; i < 4; i++) {
                    if (i == num) {
                        // 保存正确选项
                        FeiBranchOption b1 = new FeiBranchOption();
                        b1.setTopicId(feiBranchTopic.getId());
                        b1.setContent(branchTopicHouTaiVo.getContext1());
                        b1.setCreateTime(new Date());
                        feiBranchOptionMapper.insert(b1);

                        feiBranchTopic.setCorrectOptionId(b1.getId());
                        feiBranchTopicMapper.updateById(feiBranchTopic);
                    } else {
                        if (!c2) {
                            FeiBranchOption b2 = new FeiBranchOption();
                            b2.setTopicId(feiBranchTopic.getId());
                            b2.setContent(branchTopicHouTaiVo.getContext2());
                            b2.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            FeiBranchOption b3 = new FeiBranchOption();
                            b3.setTopicId(feiBranchTopic.getId());
                            b3.setContent(branchTopicHouTaiVo.getContext3());
                            b3.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            FeiBranchOption b4 = new FeiBranchOption();
                            b4.setTopicId(feiBranchTopic.getId());
                            b4.setContent(branchTopicHouTaiVo.getContext4());
                            b4.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b4);
                            c4 = true;
                            continue;
                        }
                    }
                }
            } else if (1 == branchTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    FeiBranchOption b1 = new FeiBranchOption();
                    b1.setTopicId(feiBranchTopic.getId());
                    b1.setContent(branchTopicHouTaiVo.getTContext1());
                    b1.setCreateTime(new Date());
                    feiBranchOptionMapper.insert(b1);
                }
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    FeiBranchOption b2 = new FeiBranchOption();
                    b2.setTopicId(feiBranchTopic.getId());
                    b2.setContent(branchTopicHouTaiVo.getTContext2());
                    b2.setCreateTime(new Date());
                    feiBranchOptionMapper.insert(b2);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                FeiBranchOption b1 = new FeiBranchOption();
                b1.setTopicId(feiBranchTopic.getId());
                b1.setContent(branchTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                b1.setCreateTime(new Date());
                feiBranchOptionMapper.insert(b1);
            }
        } else {
            // 修改
            FeiBranchTopic feiBranchTopic = feiBranchTopicMapper.selectById(branchTopicHouTaiVo.getId());
            feiBranchTopic.setTitle(branchTopicHouTaiVo.getTitle());
            feiBranchTopic.setCorrectParse(branchTopicHouTaiVo.getCorrectParse());
            feiBranchTopic.setPoint(branchTopicHouTaiVo.getTitle());
            if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getImageUrl().trim())) {
                feiBranchTopic.setImageUrl(null);
            } else {
                feiBranchTopic.setImageUrl(branchTopicHouTaiVo.getImageUrl().trim());
            }
            if (com.wangchen.common.utils.StringUtils.isEmpty(branchTopicHouTaiVo.getVideoUrl().trim())) {
                feiBranchTopic.setVideoUrl(null);
            } else {
                feiBranchTopic.setVideoUrl(branchTopicHouTaiVo.getVideoUrl().trim());
            }
            feiBranchTopicMapper.updateById(feiBranchTopic);
            // 删除题目对应答案数据
            feiBranchOptionMapper.delete(new QueryWrapper<FeiBranchOption>().eq("topic_id", feiBranchTopic.getId()));

            if (0 == branchTopicHouTaiVo.getTopicType()) {
                // 选择题
                Random random = new Random();
                int num = random.nextInt(4);

                boolean c2 = false;
                boolean c3 = false;
                boolean c4 = false;
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext3())) {
                    c3 = true;
                    c4 = true;
                }
                if (StringUtils.isEmpty(branchTopicHouTaiVo.getContext4())) {
                    c4 = true;
                }
                for (int i = 0; i < 4; i++) {
                    if (i == num) {
                        // 新增正确答案
                        FeiBranchOption b1 = new FeiBranchOption();
                        b1.setTopicId(feiBranchTopic.getId());
                        b1.setContent(branchTopicHouTaiVo.getContext1());
                        b1.setCreateTime(new Date());
                        feiBranchOptionMapper.insert(b1);

                        feiBranchTopic.setCorrectOptionId(b1.getId());
                        feiBranchTopicMapper.updateById(feiBranchTopic);
                    } else {
                        if (!c2) {
                            FeiBranchOption b2 = new FeiBranchOption();
                            b2.setTopicId(feiBranchTopic.getId());
                            b2.setContent(branchTopicHouTaiVo.getContext2());
                            b2.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b2);
                            c2 = true;
                            continue;
                        }
                        if (!c3) {
                            FeiBranchOption b3 = new FeiBranchOption();
                            b3.setTopicId(feiBranchTopic.getId());
                            b3.setContent(branchTopicHouTaiVo.getContext3());
                            b3.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b3);
                            c3 = true;
                            continue;
                        }
                        if (!c4) {
                            FeiBranchOption b4 = new FeiBranchOption();
                            b4.setTopicId(feiBranchTopic.getId());
                            b4.setContent(branchTopicHouTaiVo.getContext4());
                            b4.setCreateTime(new Date());
                            feiBranchOptionMapper.insert(b4);
                            c4 = true;
                            continue;
                        }
                    }
                }
            } else if (1 == branchTopicHouTaiVo.getTopicType()) {
                // 填空题
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext1())) {
                    FeiBranchOption b1 = new FeiBranchOption();
                    b1.setTopicId(feiBranchTopic.getId());
                    b1.setContent(branchTopicHouTaiVo.getTContext1());
                    b1.setCreateTime(new Date());
                    feiBranchOptionMapper.insert(b1);
                }
                if (!StringUtils.isEmpty(branchTopicHouTaiVo.getTContext2())) {
                    FeiBranchOption b1 = new FeiBranchOption();
                    b1.setTopicId(feiBranchTopic.getId());
                    b1.setContent(branchTopicHouTaiVo.getTContext2());
                    b1.setCreateTime(new Date());
                    feiBranchOptionMapper.insert(b1);
                }
            } else if (2 == branchTopicHouTaiVo.getTopicType()) {
                // 判断题
                FeiBranchOption b1 = new FeiBranchOption();
                b1.setTopicId(feiBranchTopic.getId());
                b1.setContent(branchTopicHouTaiVo.getPContext().equals("正确") ? "对" : "错");
                b1.setCreateTime(new Date());
                feiBranchOptionMapper.insert(b1);
            }
        }

        return Result.newSuccess(null == branchTopicHouTaiVo.getId() ? "新增必知必会题目信息成功" : "修改必知必会题目信息成功");
    }


    /**
     * 获取所属公司分类下必知必会题目合集(二期)
     *
     * @param title       标题（用于题目模糊搜索）
     * @param page        页码
     * @param limit       每页数据量
     * @param companyType 所属公司类型
     * @return 获取所属公司分类下必知必会题目合集
     */
    @Override
    public Result selectPages(int page, int limit, String title, int companyType) {
        IPage<FeiBranchTopic> pages = feiBranchTopicMapper.selectPage(new Page<FeiBranchTopic>(page, limit),
                new QueryWrapper<FeiBranchTopic>().eq("company_type", companyType).like(!org.thymeleaf.util.StringUtils.isEmpty(title), "title", title).orderByDesc("id"));
        return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
    }


    /**
     * 保存导入选择题信息（二期）
     *
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber     读取excel文件的第几个单元表
     * @param rowStart        从第几行开始读取
     * @param topicType       题目所属类型
     * @param companyType     题目所属公司分类
     * @return 导入选择题信息是否成功
     */
    @Override
    @Transactional
    public String inputXuanZeExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        //获取内容信息
        for (int i = rowStart; i <= rowEnd; ++i) {
            try {
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = FeiBranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
                    // 二期后选择题答案不一定为4个，由导入题库选项个数决定，目前暂定校验时最少为2个
                    FeiBranchOption feiBranchOptionA = new FeiBranchOption();
                    FeiBranchOption feiBranchOptionB = new FeiBranchOption();
                    FeiBranchOption feiBranchOptionC = new FeiBranchOption();
                    FeiBranchOption feiBranchOptionD = new FeiBranchOption();

                    feiBranchTopic.setTopicType(topicType);
                    feiBranchTopic.setCompanyType(companyType);
                    feiBranchTopic.setImageUrl(null);
                    feiBranchTopic.setVideoUrl(null);
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
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目]内容不能为空！  ");
                            }
                            feiBranchTopic.setTitle(data.getStringCellValue().trim());
                            feiBranchTopic.setPoint(data.getStringCellValue().trim());
                            feiBranchTopic.setCreateTime(new Date());
                            feiBranchTopicMapper.insert(feiBranchTopic);
                        } else if (1 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[正确选项]内容不能为空！  ");
                            } else {
                                String choose = data.getStringCellValue().trim().toUpperCase();
                                if ("A".equals(choose) || "B".equals(choose) || "C".equals(choose) || "D".equals(choose)) {
                                    select = choose;
                                } else {
                                    topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[正确选项]内容只能为ABCD其中之一; ");
                                }
                            }
                        } else if (2 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[A选项]内容不能为空！  ");
                                aXZ = true;
                                cellCount--;
                            } else {
                                feiBranchOptionA.setContent(data.getStringCellValue().trim());
                                feiBranchOptionA.setCreateTime(new Date());
                                feiBranchOptionA.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionA);
                                if ("A".equals(select.trim())) {
                                    feiBranchTopic.setCorrectOptionId(feiBranchOptionA.getId());
                                    feiBranchTopicMapper.updateById(feiBranchTopic);
                                }
                            }
                        } else if (3 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[B选项]内容不能为空！  ");
                                cellCount--;
                            } else {
                                if (aXZ) {
                                    topicError.append("第" + (i + 1) + "行第" + (j) + "列[A选项]内容为空，应调整此单元格内容至[A选项]  ");
                                }
                                feiBranchOptionB.setContent(data.getStringCellValue().trim());
                                feiBranchOptionB.setCreateTime(new Date());
                                feiBranchOptionB.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionB);
                                if ("B".equals(select.trim())) {
                                    feiBranchTopic.setCorrectOptionId(feiBranchOptionB.getId());
                                    feiBranchTopicMapper.updateById(feiBranchTopic);
                                }
                            }
                        } else if (4 == j) {
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount == 2) {
                                    topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[C选项]之前AB选项均为空，此选项已经不能为空！");
                                }
                                cellCount--;
                            } else {
                                if (cellCount < 4) {
                                    topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[C选项]之前有列选项空白，应依次调整前移！  ");
                                }
                                feiBranchOptionC.setContent(data.getStringCellValue().trim());
                                feiBranchOptionC.setCreateTime(new Date());
                                feiBranchOptionC.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionC);
                                if ("C".equals(select.trim())) {
                                    feiBranchTopic.setCorrectOptionId(feiBranchOptionC.getId());
                                    feiBranchTopicMapper.updateById(feiBranchTopic);
                                }
                            }
                        } else if (5 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                if (cellCount < 3) {
                                    topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[D选项]之前至少有2列选项空白，此项不能为空且需依次调整前移！  ");
                                }
                            } else {
                                feiBranchOptionD.setContent(data.getStringCellValue().trim());
                                feiBranchOptionD.setCreateTime(new Date());
                                feiBranchOptionD.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionD);
                                if ("D".equals(select)) {
                                    feiBranchTopic.setCorrectOptionId(feiBranchOptionD.getId());
                                    feiBranchTopicMapper.updateById(feiBranchTopic);
                                }
                            }
                        } else if (6 == j) {
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目解析]内容不能为空！  ");
                            } else {
                                feiBranchTopic.setCorrectParse(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (7 == j) {
                            // 题目相关图片路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setImageUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (8 == j) {
                            // 题目相关视频路径
                            if (!StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setVideoUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("第" + i + "道题目导入错误:{}", e);
                topicError.append("第" + i + "道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入选择题总体数据存在错误:{}", topicError.toString());
            throw new BusinessException("批量导入选择题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入选择题信息成功";
    }


    /**
     * 保存导入填空题信息（二期）
     *
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber     读取excel文件的第几个单元表
     * @param rowStart        从第几行开始读取
     * @param topicType       题目所属类型
     * @param companyType     题目所属公司分类
     * @return 导入填空题信息是否成功
     */
    @Override
    @Transactional
    public String inputTianKongExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        //获取内容信息
        for (int i = rowStart; i <= rowEnd; ++i) {
            try {
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = FeiBranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 默认一道题最多两个填空选项
                    FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
                    FeiBranchOption feiBranchOptionA = new FeiBranchOption();
                    FeiBranchOption feiBranchOptionB = new FeiBranchOption();

                    feiBranchTopic.setTopicType(topicType);
                    feiBranchTopic.setCompanyType(companyType);
                    feiBranchTopic.setImageUrl(null);
                    feiBranchTopic.setVideoUrl(null);
                    // 标记填空题1内容是不是空
                    Boolean aTK = false;
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            // 题目内容
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目]内容不能为空！  ");
                                aTK = true;
                            } else {
                                feiBranchTopic.setTitle(data.getStringCellValue().trim());
                                feiBranchTopic.setPoint(data.getStringCellValue().trim());
                                feiBranchTopic.setCreateTime(new Date());
                                feiBranchTopicMapper.insert(feiBranchTopic);
                            }
                        } else if (1 == j) {
                            // 填空项1
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[填空项1]内容不能为空！  ");
                            } else {
                                if (aTK) {
                                    topicError.append("第" + (i + 1) + "行第" + (j) + "列[填空项1]内容内容为空，此内容应前移！  ");
                                }
                                feiBranchOptionA.setContent(data.getStringCellValue().trim());
                                feiBranchOptionA.setCreateTime(new Date());
                                feiBranchOptionA.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionA);
                            }
                        } else if (2 == j) {
                            // 填空项2
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchOptionB.setContent(data.getStringCellValue().trim());
                                feiBranchOptionB.setCreateTime(new Date());
                                feiBranchOptionB.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOptionB);
                            }
                        } else if (3 == j) {
                            // 题目解析
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目解析]内容不能为空！  ");
                            } else {
                                feiBranchTopic.setCorrectParse(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (4 == j) {
                            // 题目相关图片路径
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setImageUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (5 == j) {
                            // 题目相关视频路径
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setVideoUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("第" + i + "道题目导入错误:{}", e);
                topicError.append("第" + i + "道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入填空题总体数据存在错误:{}", topicError.toString());
            throw new BusinessException("批量导入填空题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入填空题信息成功";
    }


    /**
     * 保存导入判断题信息（二期）
     *
     * @param excelInputSteam 导入选择题excel文件输入流
     * @param sheetNumber     读取excel文件的第几个单元表
     * @param rowStart        从第几行开始读取
     * @param topicType       题目所属类型
     * @param companyType     题目所属公司分类
     * @return 导入判断题信息是否成功
     */
    @Override
    @Transactional
    public String inputPanDuanExcel(InputStream excelInputSteam, int sheetNumber, int rowStart, Integer topicType, Integer companyType) throws IOException, InvalidFormatException {
        //记录哪些题目保存错误
        StringBuffer topicError = new StringBuffer();
        //生成工作表
        Workbook workbook = WorkbookFactory.create(excelInputSteam);
        Sheet sheet = workbook.getSheetAt(sheetNumber);
        //最后一行数据
        int rowEnd = sheet.getLastRowNum();
        //获取内容信息
        for (int i = rowStart; i <= rowEnd; ++i) {
            try {
                Row currentRow = sheet.getRow(i);
                // 判断当前行内容是否为空
                Boolean isEmpty = FeiBranchTopicServiceImpl.isRowEmpty(currentRow);
                if (!isEmpty) {
                    // 一个题目一个判断选项
                    FeiBranchTopic feiBranchTopic = new FeiBranchTopic();
                    FeiBranchOption feiBranchOption = new FeiBranchOption();

                    feiBranchTopic.setTopicType(topicType);
                    feiBranchTopic.setCompanyType(companyType);
                    feiBranchTopic.setImageUrl(null);
                    feiBranchTopic.setVideoUrl(null);
                    // 遍历每行内容
                    for (int j = 0; j < currentRow.getLastCellNum(); ++j) {
                        //将null转化为Blank
                        Cell data = currentRow.getCell(j, Row.CREATE_NULL_AS_BLANK);
                        data.setCellType(Cell.CELL_TYPE_STRING);
                        if (0 == j) {
                            // 题目
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目]内容不能为空！  ");
                            } else {
                                feiBranchTopic.setTitle(data.getStringCellValue().trim());
                                feiBranchTopic.setPoint(data.getStringCellValue().trim());
                                feiBranchTopic.setCreateTime(new Date());
                                feiBranchTopicMapper.insert(feiBranchTopic);
                            }
                        } else if (1 == j) {
                            // 答案
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[答案]内容不能为空！  ");
                            } else {
                                if ("0".equals(data.getStringCellValue())) {
                                    feiBranchOption.setContent("错");
                                } else if ("1".equals(data.getStringCellValue())) {
                                    feiBranchOption.setContent("对");
                                } else {
                                    topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[答案]内容只能为0或1 ");
                                }
                                feiBranchOption.setCreateTime(new Date());
                                feiBranchOption.setTopicId(feiBranchTopic.getId());
                                feiBranchOptionMapper.insert(feiBranchOption);
                            }
                        } else if (2 == j) {
                            // 题目解析
                            if (com.wangchen.common.utils.StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                topicError.append("第" + (i + 1) + "行第" + (j + 1) + "列[题目解析]内容不能为空！  ");
                            } else {
                                feiBranchTopic.setCorrectParse(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (3 == j) {
                            // 题目相关图片路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setImageUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        } else if (4 == j) {
                            // 题目相关视频路径
                            if (StringUtils.isEmpty(data.getStringCellValue().trim())) {
                                feiBranchTopic.setVideoUrl(data.getStringCellValue().trim());
                                feiBranchTopicMapper.updateById(feiBranchTopic);
                            }
                        }
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("第" + i + "道题目导入错误:{}", e);
                topicError.append("第" + i + "道题目导入错误 \n");
                throw new BusinessException(topicError.toString());
            }
        }
        // 如果topicError为空，说明导入内容检测无问题，可以批量导入
        if (topicError.length() > 0) {
            // 导入存在错误
            log.error("批量导入判断题总体数据存在错误:{}", topicError.toString());
            throw new BusinessException("批量导入判断题总体数据存在错误：" + topicError.toString());
        }

        return "批量导入判断题信息成功";
    }


    /**
     * 从所属公司分类下必知必会题库获取特定道题
     *
     * @param num         需获取题目数量
     * @param companyType 所属公司分类
     * @return 获取必知必会题目集合
     */
    @Override
    public List<FeiBranchTopic> listTopicRandomByCompanyType(int num, Integer companyType) {
        return feiBranchTopicMapper.listTopicRandomByCompanyType(num, companyType);
    }


    /**
     * 判断读取行是否内容为空（存在格式设置）
     *
     * @param row 行数据
     * @return 该行是否存在数据
     */
    public static boolean isRowEmpty(Row row) {
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
