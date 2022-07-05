package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.ActivityTopic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.vo.ActivityTopicHouTaiVo;
import com.wangchen.vo.BranchTopicHouTaiVo;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * 活动赛题库 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
public interface ActivityTopicService extends IService<ActivityTopic> {

    /**
     * 新增或修改活动赛题目（二期）
     */
    Result editActivityTopic(@Param("activityTopicHouTaiVo") ActivityTopicHouTaiVo activityTopicHouTaiVo);

    /**
     * 批量导入活动赛题目（二期 选择题）
     */
    String inputXuanZeExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("type") Object type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType, @Param("activityId") Integer activityId) throws IOException, InvalidFormatException;

    /**
     * 批量导入活动赛题目（二期 填空题）
     */
    String inputTianKongExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("type") Object type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType, @Param("activityId") Integer activityId) throws IOException, InvalidFormatException;

    /**
     * 批量导入活动赛题目（二期 判断题）
     */
    String inputPanDuanExcel(@Param("excelInputSteam") InputStream excelInputSteam, @Param("sheetNumber") int sheetNumber, @Param("rowStart") int rowStart, @Param("type") Object type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType, @Param("activityId") Integer activityId) throws IOException, InvalidFormatException;
}
