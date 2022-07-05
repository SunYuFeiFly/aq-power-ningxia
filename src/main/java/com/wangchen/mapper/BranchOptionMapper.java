package com.wangchen.mapper;

import com.wangchen.entity.BranchOption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 部门题库答案表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-08
 */
@Component
@Mapper
public interface BranchOptionMapper extends BaseMapper<BranchOption> {

}
