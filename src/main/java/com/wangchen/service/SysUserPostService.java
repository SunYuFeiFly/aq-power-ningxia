package com.wangchen.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangchen.entity.SysUserOnline;
import com.wangchen.entity.SysUserPost;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户与岗位关联表 服务类
 * </p>
 *
 * @author yinguang
 * @since 2020-06-19
 */
public interface SysUserPostService extends IService<SysUserPost> {

    /**
     * <p>
     * 在线用户记录 服务类
     * </p>
     *
     * @author yinguang
     * @since 2020-06-19
     */
    interface SysUserOnlineService extends IService<SysUserOnline> {
        /**
         * 通过会话序号查询信息
         *
         * @param sessionId 会话ID
         * @return 在线用户信息
         */
        public SysUserOnline selectOnlineById(String sessionId);
    
        /**
         * 通过会话序号删除信息
         *
         * @param sessionId 会话ID
         * @return 在线用户信息
         */
        public void deleteOnlineById(String sessionId);
    
        /**
         * 通过会话序号删除信息
         *
         * @param sessions 会话ID集合
         * @return 在线用户信息
         */
        public void batchDeleteOnline(List<String> sessions);
    
        /**
         * 保存会话信息
         *
         * @param online 会话信息
         */
        public void saveOnline(SysUserOnline online);
    
        /**
         * 查询会话集合
         *
         * @param userOnline 分页参数
         * @return 会话集合
         */
        public List<SysUserOnline> selectUserOnlineList(SysUserOnline userOnline);
    
        /**
         * 强退用户
         *
         * @param sessionId 会话ID
         */
        public void forceLogout(String sessionId);
    
        /**
         * 查询会话集合
         *
         * @param expiredDate 有效期
         * @return 会话集合
         */
        public List<SysUserOnline> selectOnlineByExpired(Date expiredDate);
    }
}
