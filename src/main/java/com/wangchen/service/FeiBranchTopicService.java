package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.FeiBranchTopic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.vo.BranchTopicHouTaiVo;
import com.wangchen.vo.WisdomLibraryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 非专业部门题库表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
public interface FeiBranchTopicService extends IService<FeiBranchTopic> {

    /**
     * 随机获取特定数量非专业题库题目（二期）
     */
    List<FeiBranchTopic> listTopicRandom(@Param("num") Integer num);

    /**
     * 删除题目（二期）
     */
    void delTopic(@Param("id") Integer id);

    /**
     * 批量删除部门下选定题目（二期）
     */
    void deleteTopics(@Param("ids") Integer[] ids);

    /**
     * 删除对应公司类型下所有题目（二期）
     */
    void deleteTopicAll(@Param("companyType") Integer companyType);

    /**
     * 有条件导出选择题(二期)
     */
    void exportXuanZeToExcel(@Param("path") String path, @Param("feiBranchTopicList") List<FeiBranchTopic> feiBranchTopicList) throws IOException;

    /**
     * 有条件导出填空题(二期)
     */
    void exportTianKongToExcel(@Param("path") String path, @Param("feiBranchTopicList") List<FeiBranchTopic> feiBranchTopicList) throws IOException;

    /**
     * 有条件导出判断题(二期)
     */
    void exportPanDuanToExcel(@Param("path") String path, @Param("feiBranchTopicList") List<FeiBranchTopic> feiBranchTopicList) throws IOException;

    /**
     * 获取人员所属公司分类下非专业题库题目集合（二期）
     */
    List<WisdomLibraryVo> getFeiTiKuTopicList(@Param("page") Integer page, @Param("type") Integer type, @Param("topicName") String topicName);

    /**
     * 新增或修改必知必会题目 （二期）
     */
    Result editBranchTopic(@Param("branchTopicHouTaiVo") BranchTopicHouTaiVo branchTopicHouTaiVo);

    /**
     * 获取所属公司分类下必知必会题目合集(二期)
     */
    Result selectPages(@Param("page") int page, @Param("limit") int limit, @Param("title") String title, @Param("companyType") int companyType);

    /**
     * 保存导入选择题信息(二期)
     */
    String inputXuanZeExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 保存导入填空题信息（二期）
     */
    String inputTianKongExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 保存导入判断题信息（二期）
     */
    String inputPanDuanExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 从所属公司分类下必知必会题库获取特定道题（二期）
     */
    List<FeiBranchTopic> listTopicRandomByCompanyType(@Param("num") int num, @Param("companyType") Integer companyType);
}
