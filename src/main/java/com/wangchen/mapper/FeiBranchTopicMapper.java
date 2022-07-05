package com.wangchen.mapper;

import com.wangchen.entity.FeiBranchTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 非专业部门题库表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
@Component
@Mapper
public interface FeiBranchTopicMapper extends BaseMapper<FeiBranchTopic> {

    /**
     * 分页查询问题设置
     */
    @Select("SELECT * FROM aq_fei_branch_topic ORDER BY RAND() LIMIT ${num}")
    List<FeiBranchTopic> listTopicRandoms(@Param("num") Integer num);

    /**
     * 从所属公司分类下必知必会题库获取特定道题(二期)
     */
    @Select("SELECT * FROM aq_fei_branch_topic a where a.company_type = #{companyType} ORDER BY RAND() LIMIT ${num}")
    List<FeiBranchTopic> listTopicRandomByCompanyType(@Param("num") int num, @Param("companyType") Integer companyType);
}
