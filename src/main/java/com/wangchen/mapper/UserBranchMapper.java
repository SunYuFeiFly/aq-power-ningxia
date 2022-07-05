package com.wangchen.mapper;

import com.wangchen.entity.UserBranch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 用户部门表 Mapper 接口
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Component
@Mapper
public interface UserBranchMapper extends BaseMapper<UserBranch> {

}
