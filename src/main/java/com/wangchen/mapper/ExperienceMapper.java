package com.wangchen.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangchen.entity.Experience;
import com.wangchen.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * 用户经验明细表
 * </p>
 *
 * @author LiJian
 * @since 2021-09-09
 */

@Component
@Mapper
public interface ExperienceMapper extends BaseMapper<Experience> {

    // 批量查询用户经验记录集合
    List<Experience> batchQueryExperience(@Param("month") int month, @Param("year") int year, @Param("openIds") ArrayList<String> openIds);

}
