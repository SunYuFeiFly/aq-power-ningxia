package com.wangchen.service.impl;

import com.wangchen.entity.SysRole;
import com.wangchen.entity.SysRoleDept;
import com.wangchen.entity.SysUserRole;
import com.wangchen.mapper.SysRoleDeptMapper;
import com.wangchen.service.SysRoleDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 角色和部门关联表 服务实现类
 * </p>
 *
 * @author yinguang
 * @since 2020-06-19
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements SysRoleDeptService {

    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        return null;
    }

    @Override
    public Set<String> selectRoleKeys(Long userId) {
        return null;
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return null;
    }

    @Override
    public List<SysRole> selectRoleAll() {
        return null;
    }

    @Override
    public SysRole selectRoleById(Long roleId) {
        return null;
    }

    @Override
    public boolean deleteRoleById(Long roleId) {
        return false;
    }

    @Override
    public int deleteRoleByIds(String ids) throws Exception {
        return 0;
    }

    @Override
    public int insertRole(SysRole role) {
        return 0;
    }

    @Override
    public int updateRole(SysRole role) {
        return 0;
    }

    @Override
    public int authDataScope(SysRole role) {
        return 0;
    }

    @Override
    public String checkRoleNameUnique(SysRole role) {
        return null;
    }

    @Override
    public String checkRoleKeyUnique(SysRole role) {
        return null;
    }

    @Override
    public void checkRoleAllowed(SysRole role) {

    }

    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return 0;
    }

    @Override
    public int changeStatus(SysRole role) {
        return 0;
    }

    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        return 0;
    }

    @Override
    public int deleteAuthUsers(Long roleId, String userIds) {
        return 0;
    }

    @Override
    public int insertAuthUsers(Long roleId, String userIds) {
        return 0;
    }
}
