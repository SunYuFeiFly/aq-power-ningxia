package com.wangchen.mapper;

import com.wangchen.entity.BranchTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.vo.WisdomLibraryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 部门题库表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
@Component
@Mapper
public interface BranchTopicMapper extends BaseMapper<BranchTopic> {

    /**
     * 分页查询问题设置
     */
    @Select("SELECT * FROM aq_branch_topic where type = #{branchType} ORDER BY RAND() LIMIT ${num}")
    List<BranchTopic> listTopicRandom(@Param("branchType") Integer branchType,@Param("num") Integer num);

    /**
     * 分页查询问题设置
     */
    @Select("SELECT * FROM aq_branch_topic ORDER BY RAND() LIMIT ${num}")
    List<BranchTopic> listTopicRandoms(@Param("num") Integer num);

    /**
     * 分页查询问题设置
     */
    @Select("SELECT * FROM aq_branch_topic where topic_type !=1  ORDER BY RAND() LIMIT ${num}")
    List<BranchTopic> listTopicRandomsNotTianKong(@Param("num") Integer num);

    /**
     * 从所属公司分类下题库获取特定道题（二期）
     */
    @Select("SELECT * FROM aq_branch_topic a where a.company_type = #{companyType} ORDER BY RAND() LIMIT ${num}")
    List<BranchTopic> listTopicRandomByCompanyType(@Param("num") Integer num, @Param("companyType") Integer companyType);
}
