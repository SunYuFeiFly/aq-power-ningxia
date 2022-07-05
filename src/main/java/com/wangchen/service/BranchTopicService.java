package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.BranchTopic;
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
 * 部门题库表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
public interface BranchTopicService extends IService<BranchTopic> {

    /**
     * 获取部门下特定数量题目
     */
    List<BranchTopic> listTopicRandom(@Param("branchType") Integer branchType, @Param("num") Integer num);

    /**
     * 获取所有部门下特定数量题目
     */
    List<BranchTopic> listTopicRandom(@Param("num") Integer num);

    /**
     *  获取所有部门下特定数量题目（非填空题）
     */
    List<BranchTopic> listTopicRandomsNotTianKong(@Param("num") Integer num);

    /**
     * 保存导入选择题信息（二期）
     */
    String inputXuanZeExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("type") Integer type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 保存导入填空题信息（二期）
     */
    String inputTianKongExcel(@Param("is") InputStream is, @Param("i") int i, @Param("i1") int i1, @Param("type") Integer type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 保存导入判断题信息（二期）
     */
    String inputPanDuanExcel(@Param("is") InputStream is, @Param("i") int i, @Param("i1") int i1, @Param("type") Integer type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 有条件导出选择题（二期）
     */
    void exportXuanZeToExcel(@Param("path") String path, @Param("branchTopicList") List<BranchTopic> branchTopicList) throws IOException;

    /**
     * 有条件导出填空题（二期）
     */
    void exportTianKongToExcel(@Param("path") String path, @Param("branchTopicList") List<BranchTopic> branchTopicList) throws IOException;

    /**
     * 有条件导出判断题（二期）
     */
    void exportPanDuanToExcel(@Param("path") String path, @Param("branchTopicList") List<BranchTopic> branchTopicList) throws IOException;

    /**
     * 删除部门下特定题目（单一道题、二期）
     */
    void delTopic(@Param("id") Integer id);

    /**
     * 批量删除部门下选定题目（二期）
     */
    void deleteTopics(@Param("ids") Integer[] ids);

    /**
     * 删除部门下所有题目（二期）
     */
    void deleteTopicAll(@Param("type") Integer type);

    /**
     * 获取部门下专题题目数据集合（二期）
     */
    List<WisdomLibraryVo> getBuMenTopicList(@Param("page") int page, @Param("buMenType") int buMenType, @Param("topicName") String topicName, @Param("type") Integer type);

    /**
     * 条件查询获取对应公司部门题目合集（二期）
     */
    Result selectPages(@Param("title") String title, @Param("type") Integer type, @Param("page") int page, @Param("limit") int limit, @Param("companyType") Integer companyType);

    /**
     * 新增或修改题目（二期）
     */
    Result editBranchTopic(@Param("branchTopicHouTaiVo") BranchTopicHouTaiVo branchTopicHouTaiVo);

    /**
     * 从所属公司分类下题库获取特定道题（二期）
     */
    List<BranchTopic> listTopicRandomByCompanyType(@Param("num") Integer num, @Param("companyType") Integer companyType);
}
