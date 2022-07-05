package com.wangchen.service;

import com.wangchen.common.Result;
import com.wangchen.entity.BaseUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
public interface BaseUserService extends IService<BaseUser> {

    /**
     * 批量删除基本员工信息(二期)
     */
    void deleteBaseUsers(@Param("ids") int[] ids);

    /**
     * 批量上传基本用户信息(二期)
     */
    String inputBaseUsersFromExcel(@Param("file") MultipartFile file, @Param("companyType") Integer companyType) throws IOException, InvalidFormatException;

    /**
     * 新增修改员工数据 (二期)
     */
    Result editBaseUser(@Param("info") BaseUser info);

    /**
     * 员工管理-基本员工信息列表数据（二期）
     */
    Result selectPages(@Param("name") String name, @Param("companyName") String companyName, @Param("page") Integer page, @Param("limit") Integer limit, @Param("companyType") Integer companyType);
}
