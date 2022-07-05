package com.wangchen.mapper;

import com.wangchen.entity.FeiBranchOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 非专业部门题库答案表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-08-26
 */
@Component
@Mapper
public interface FeiBranchOptionMapper extends BaseMapper<FeiBranchOption> {

}
