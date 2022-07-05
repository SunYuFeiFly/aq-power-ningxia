package com.wangchen.mapper;

import com.wangchen.entity.Branch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 部门表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface BranchMapper extends BaseMapper<Branch> {

}
