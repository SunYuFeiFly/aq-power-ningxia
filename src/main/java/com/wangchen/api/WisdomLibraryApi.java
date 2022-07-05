package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.BusinessErrorMsg;
import com.wangchen.common.Result;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.mapper.BranchTopicMapper;
import com.wangchen.mapper.FeiBranchOptionMapper;
import com.wangchen.mapper.FeiBranchTopicMapper;
import com.wangchen.mapper.WisdomLibraryMapper;
import com.wangchen.service.*;
import com.wangchen.vo.WisdomLibraryVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @ProjectName: 铁塔智库
 * @Package: com.wangchen.api
 * @ClassName: WisdomLibraryApi
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/18 13:39
 * @Version: 1.0
 */
@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/wisdom")
public class WisdomLibraryApi {

    @Autowired
    private UserService userService;

    @Autowired
    private WisdomLibraryService wisdomLibraryService;

    @Autowired
    private BranchTopicMapper branchTopicMapper;

    @Autowired
    private BranchTopicService branchTopicService;

    @Autowired
    private BranchOptionService branchOptionService;

    @Autowired
    private WisdomLibraryMapper wisdomLibraryMapper;

    @Autowired
    private FeiBranchTopicMapper feiBranchTopicMapper;

    @Autowired
    private FeiBranchTopicService feiBranchTopicService;

    @Autowired
    private FeiBranchOptionService feiBranchOptionService;

    @Autowired
    private FeiBranchOptionMapper feiBranchOptionMapper;

    /**
     * 查询用户所属公司分类下部门集合（二期）
     * @param openId 用户id
     * @return 用户所属公司类型下部门合集
     */
    @PostMapping("/getBuMenList")
    @ResponseBody
    public Result getBuMenList(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            // 获取员工所属公司类型
            Integer type = user.getType();
            // 员工所属公司部门集合
            Map<Integer,String> branchs = new LinkedHashMap<Integer,String>();
            // 获取所有部门信息
            Set<Integer> integers = Constants.newBuMenKu.keySet();
            Iterator<Integer> iterator = integers.iterator();
            while (iterator.hasNext()) {
                Integer index = iterator.next();
                String[] strings = Constants.newBuMenKu.get(index);
                if (type.equals(Integer.parseInt(strings[0]))) {
                    // 特殊化处理， 党群和纪委合二为一，技术支撑中心不需要
                    if (8 == index) {
                        branchs.put(index,"党群纪检");
                    } else if (9 == index || 11 == index) {
                        // 不做处理
                    } else {
                        branchs.put(index,strings[1]);
                    }
                }
            }

            return Result.newSuccess(branchs);
        }catch (Exception e){
            log.error("查询用户所属公司分类下部门集合出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }

    /**
     * 查看我的智库出题信息
     * @param openId 用户id
     * @param page 页码
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/getWisdomLibraryByOpenId")
    @ResponseBody
    public Result getWisdomLibraryByOpenId(@RequestParam(value = "openId",required = false) String openId,
                                           @RequestParam(value = "page", required = false, defaultValue = "1") int page){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }

            IPage<WisdomLibrary> wisdomLibraryIPage = wisdomLibraryMapper.selectPage(new Page<>(page, 10),
                    new QueryWrapper<WisdomLibrary>().eq("open_id",openId));

            return Result.newSuccess(wisdomLibraryIPage.getRecords());
        }catch (Exception e){
            log.error("查看我的智库出题信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 保存智库信息（选择题）
     * @param openId 用户id
     * @param buMenType 部门分类
     * @param tiKuType 题库类型
     * @param topicType 题目类型
     * @param title 题目
     * @param context1 选项一
     * @param context2 选项二
     * @param context3 选项三
     * @param context4 选项四
     * @return 保存智库信息（选择题）结果
     */
    @PostMapping("/saveWisdomLibrary")
    @ResponseBody
    public Result saveWisdomLibrary(@RequestParam(value = "openId",required = false) String openId,
                                    @RequestParam(value = "buMenType",required = false) Integer buMenType,
                                    @RequestParam(value = "tiKuType",required = false) Integer tiKuType,
                                    @RequestParam(value = "topicType",required = false) Integer topicType,
                                    @RequestParam(value = "title",required = false) String title,
                                    @RequestParam(value = "context1",defaultValue = "") String context1,
                                    @RequestParam(value = "context2",defaultValue = "") String context2,
                                    @RequestParam(value = "context3",defaultValue = "") String context3,
                                    @RequestParam(value = "context4",defaultValue = "") String context4){
        try {
            if(StringUtils.isBlank(openId) || StringUtils.isBlank(title)
                    || StringUtils.isBlank(context1) || null == buMenType
                    || null == topicType){
                return Result.newFail("参数不能为空");
            }
            if(0 == topicType){
                if(StringUtils.isBlank(context2) || StringUtils.isBlank(context3) ||StringUtils.isBlank(context4)){
                    return Result.newFail("参数不能为空");
                }
            }

            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }

            WisdomLibrary wisdomLibrary = new WisdomLibrary();
            wisdomLibrary.setOpenId(openId);
            wisdomLibrary.setBuMenType(buMenType);
            wisdomLibrary.setTikuType(tiKuType);
            wisdomLibrary.setTopicType(topicType);
            wisdomLibrary.setTitle(title);
            wisdomLibrary.setContext1(context1);
            wisdomLibrary.setContext2(context2);
            wisdomLibrary.setContext3(context3);
            wisdomLibrary.setContext4(context4);
            wisdomLibrary.setStatus(0);
            wisdomLibrary.setCreateTime(new Date());
            wisdomLibraryService.save(wisdomLibrary);

            return Result.newSuccess("上传成功,请等待审核");
        }catch (Exception e){
            log.error("保存智库信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 查看部门专业题库信息 (二期)
     * @param openId 用户id
     * @param buMenType 部门类型
     * @param topicName 题目名称（用于模糊搜索）
     * @param page 页码
     * @return 部门题目数据集合
     */
    @PostMapping("/getBuMenTopicList")
    @ResponseBody
    public Result getBuMenTopicList(@RequestParam(value = "openId",required = false) String openId,
                                    @RequestParam(value = "buMenType", required = true, defaultValue = "") int buMenType,
//                                @RequestParam(value = "topicType", required = false, defaultValue = "") int topicType,
                                    @RequestParam(value = "topicName", required = false, defaultValue = "") String topicName,
                                    @RequestParam(value = "page", required = false, defaultValue = "1") int page){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if ("".equals(topicName.trim()) && topicName.trim() == null) {
                return Result.newFail("缺少部门名称！");
            }
            // 获取人员所属公司类型
            Integer type = user.getType();
            // 获取部门下题目数据集合
            List<WisdomLibraryVo> wisdomLibraryVoArrayList = branchTopicService.getBuMenTopicList(page, buMenType, topicName, type);

            return Result.newSuccess(wisdomLibraryVoArrayList);
        }catch (Exception e){
            log.error("查看部门专业题库信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }


    /**
     * 查看非专业题库信息 (二期)
     * @param openId 用户id
     * @param topicName 题目名称（用于模糊搜索）
     * @param page（页码）
     * @return 非专业题库（必知必会）题目数据集合
     */
    @PostMapping("/getFeiTiKuTopicList")
    @ResponseBody
    public Result getFeiTiKuTopicList(@RequestParam(value = "openId",required = true) String openId,
                                      @RequestParam(value = "topicName", required = false, defaultValue = "") String topicName,
                                      @RequestParam(value = "page", required = false, defaultValue = "1") Integer page){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            // 获取人员所属公司类型
            Integer type = user.getType();
            // 获取人员所属公司分类下非专业题库题目集合
            List<WisdomLibraryVo> wisdomLibraryVoArrayList = feiBranchTopicService.getFeiTiKuTopicList(page, type, topicName);

            return Result.newSuccess(wisdomLibraryVoArrayList);
        }catch (Exception e){
            log.error("查看非专业题库信息出错，错误信息: {}",e);
            return Result.newFail(BusinessErrorMsg.BUSINESS_ERROR);
        }
    }

 }