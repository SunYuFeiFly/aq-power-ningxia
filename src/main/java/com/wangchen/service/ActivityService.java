package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.Activity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

/**
 * <p>
 * 活动赛表 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-19
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 获取赛事列表（二期）
     */
    Result selectPages(@Param("page") int page, @Param("limit") int limit, @Param("name") String name, @Param("companyType") Integer companyType);

    /**
     * 修改活动赛事（二期）
     */
    Result editActivity(@Param("id") Long id, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("name") String name, @Param("companyType") Integer companyType) throws ParseException;

    /**
     * 新增活动赛事（二期）
     */
    Result insertActivity(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("name") String name, @Param("type") Integer type, @Param("topicType") Integer topicType, @Param("companyType") Integer companyType, @Param("file") MultipartFile file) throws IOException, InvalidFormatException, ParseException;

    /**
     * 查看活动赛信息(二期)
     */
    Result getActivityInfo(@Param("openId") String openId) throws Exception;

    /**
     * 获取活动赛题目信息
     */
    Result getActivityTopicInfo(@Param("openId") String openId) throws Exception;

    /**
     * 活动赛用户提交答案(二期)
     */
    Result answerActivity(@Param("openId") String openId, @Param("score") Integer score, @Param("activityId") Integer activityId);

    /**
     * 获取活动赛下题目(二期)
     */
    Result selectActivityTopicPage(@Param("title") String title, @Param("page") int page, @Param("limit") int limit, @Param("activityId") Integer activityId, @Param("companyType") Integer companyType);

    /**
     * 手动新增或查看题目初始化（二期）
     */
    HashMap<String, Object> insertOrEditInit(@Param("activityId") Integer activityId, @Param("companyType") Integer companyType, @Param("topicId") Integer topicId);

    /**
     * 删除部门下特定题目（单一道题、二期）
     */
    void delTopic(@Param("id") Integer id);

    /**
     * 批量删除部门下选定题目（多道题、二期）
     */
    void deleteTopics(@Param("ids") Integer[] ids);
}
