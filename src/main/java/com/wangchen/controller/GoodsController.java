package com.wangchen.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.Goods;
import com.wangchen.entity.User;
import com.wangchen.entity.UserGoods;
import com.wangchen.entity.UserGoodsAddress;
import com.wangchen.mapper.GoodsMapper;
import com.wangchen.mapper.UserGoodsAddressMapper;
import com.wangchen.mapper.UserGoodsMapper;
import com.wangchen.service.GoodsService;
import com.wangchen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */

@Slf4j
@Controller
@RequestMapping("/system/goods")
public class GoodsController {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserGoodsMapper userGoodsMapper;

    @Autowired
    private UserGoodsAddressMapper userGoodsAddressMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 进入商品页面
     *
     * @return
     */
    @RequiresPermissions("system:goods:view")
    @RequestMapping("/list")
    public String list(Model model) {
        return "goods/list";
    }

    /**
     * 跳转兑换界面
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:07 2020/6/30
     */
    @RequiresPermissions("system:goods:view")
    @RequestMapping("/exchangeList")
    public String exchangeList(Model model) {
        return "exchange/list";
    }


    /**
     * 查询商品兑换列表（二期）
     * @param name 商品名称（用于模糊搜索）
     * @param page 页码
     * @param limit 每页数据量
     * @return
     */
    @RequiresPermissions("system:goods:view")
    @PostMapping("/findExchangeList")
    @ResponseBody
    public Result findExchangeList(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        try {
            Result result = goodsService.findExchangeList(name, page, limit);
            return result;
        } catch (Exception e) {
            log.error("查询商品兑换列表出错，错误信息: {}",e);
            return Result.newFaild("查询商品兑换列表出错");
        }
    }


    /**
     * 编辑商品
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/6/30
     */
    @RequiresPermissions("system:goods:view")
    @GetMapping("/edit")
    public String edit(Model model,
                       @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Goods goods = goodsMapper.selectById(id);
        model.addAttribute("isEdit", id != null);
        model.addAttribute("goods", goods);
        return "goods/edit";
    }

    /**
     * 查看详情
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:15 2020/6/30
     */
    @RequiresPermissions("system:goods:view")
    @GetMapping("/editExchange")
    public String editExchange(Model model,
                               @RequestParam(value = "id", required = false, defaultValue = "") Long id) {
        Goods goods = goodsMapper.selectById(id);
        model.addAttribute("goods", goods);
        return "exchange/edit";
    }

    /**
     * 兑换详情页面
     *
     * @param
     * @return:
     * @Author:yinguang
     * @Date:16:19 2020/6/30
     */
    @RequiresPermissions("system:goods:view")
    @PostMapping("selectExchangeByGoodsId")
    public @ResponseBody
    Result selectExchangeByGoodsId(Model model,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                   @RequestParam(value = "userGoodsId", required = false, defaultValue = "") Integer userGoodsId) {
        try {
            IPage<UserGoods> pages = userGoodsMapper.selectPage(new Page<>(page, limit),
                    new QueryWrapper<UserGoods>().eq("goods_id", userGoodsId));
            for (UserGoods record : pages.getRecords()) {
                User user = userService.getUserByOpenId(record.getOpenId());
                record.setNickName(user.getName());

                if(StringUtils.isEmpty(record.getAddress())){
                    record.setAddress("虚拟商品无地址");
                }
                if (record.getGoodsType().equals(2)) {
                    UserGoodsAddress userGoodsAddress = userGoodsAddressMapper.selectById(record.getAddressId());
                    record.setAddress(userGoodsAddress.getAddress());
                }
            }
            return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.newFaild("查询错误");
        }
    }


    /**
     * 商品列表页面（二期）
     * @param page 页码
     * @param limit 每页数据量
     * @param name 商品名称（用于模糊搜索）
     * @return 商品数据集合
     */
    @RequiresPermissions("system:goods:view")
    @PostMapping("selectPages")
    public @ResponseBody
    Result selectPages(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                       @RequestParam(value = "name", required = false, defaultValue = "") String name) {
        try {
            Result result = goodsService.selectPages(page, limit, name);
            return result;
        } catch (Exception e) {
            log.error("获取商品列表数据出错，错误信息: {}",e);
            return Result.newFaild("获取商品列表数据出错");
        }
    }


    /**
     * 商品上下架 (二期)
     * @param id 商品id
     * @param status 商品状态 (0下架 1上架)
     * @return 商品上下架是否成功
     */
    @RequiresPermissions("system:goods:view")
    @PostMapping("/isStatus")
    @ResponseBody
    public Result isStatus(@RequestParam(value = "id", required = false, defaultValue = "") Integer id,
                           @RequestParam(value = "status", required = false, defaultValue = "") Integer status) {
        try {
            if (null == id && 0 >= id) {
                return Result.newFaild("上下架商品id不能为空或负数");
            }
            if (0 != status && 1 != status) {
                return Result.newFaild("上下架商品状态值只能为0/1");
            }
            goodsService.isStatus(id, status);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("商品上下架出错，错误信息: {}",e);
            return Result.newFail("商品上下架出错");
        }
        return Result.newSuccess();
    }


    /**
     * 新增修改商品信息（二期）
     * @param goods 商品对象
     * @return 新增修改商品是否成功
     */
    @RequiresPermissions("system:goods:view")
    @RequestMapping("/editGoods")
    @ResponseBody
    public Result editGoods(Goods goods) {
        try {
            if (null == goods) {
                return Result.newFaild("新增或修改商品对象不能为空");
            }
            // 新增修改商品
            goodsService.editGoods(goods);
        } catch (Exception e) {
            log.error("新增修改商品信息出错，错误信息: {}",e);
            return Result.newFaild("新增修改商品信息出错");
        }

        return Result.newSuccess(null == goods.getId() ? "新增商品信息成功" : "修改商品信息成功");
    }


    /**
     * 删除商品信息
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:goods:view")
    @PostMapping("/delGoodsById")
    public @ResponseBody
    Result delUserPhoto(@RequestParam(value = "id", required = false, defaultValue = "") Integer id) {
        try {
            Goods goods = goodsMapper.selectOne(new QueryWrapper<Goods>().eq("id", id));
            goodsMapper.delete(new QueryWrapper<Goods>().eq("id", id));
        } catch (Exception e) {
            log.error("删除商品信息出错，错误信息: {}",e);
            return Result.newFail("删除商品信息");
        }
        return Result.newSuccess();
    }


}

