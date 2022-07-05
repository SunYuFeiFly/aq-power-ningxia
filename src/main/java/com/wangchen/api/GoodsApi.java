package com.wangchen.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.Result;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.vo.GoodsVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin(origins = "*")
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/api/goods")
public class GoodsApi {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserGoodsService userGoodsService;

    @Autowired
    private UserGoodsAddressService userGoodsAddressService;

    @Autowired
    private HotLogService hotLogService;


    /**
     * 查询我的显示上面 头像和皮肤中是否有未使用的
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/getGoodTypeIsNotUsed")
    @ResponseBody
    public Result getGoodTypeIsNotUsed(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            Map<Integer,Integer> map = new HashMap<Integer, Integer>();
            //商品类型(0皮肤 1头像 2实物)',
            List<UserGoods> userGoodsList = userGoodsService.list(new QueryWrapper<UserGoods>()
                    .eq("open_id",openId).eq("goods_type",0).eq("is_used",0));
            if(CollectionUtils.isEmpty(userGoodsList)){
                map.put(0,0);
            }else{
                map.put(0,1);
            }
            List<UserGoods> userGoodsList2 = userGoodsService.list(new QueryWrapper<UserGoods>()
                    .eq("open_id",openId).eq("goods_type",1).eq("is_used",0));
            if(CollectionUtils.isEmpty(userGoodsList2)){
                map.put(1,0);
            }else{
                map.put(1,1);
            }

            return Result.newSuccess(map);
        }catch (Exception e){
            log.error("查询我的头像和皮肤是否有未使用信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }

    /**
     * 查询用户购买的商品信息
     * @param openId 用户id
     * @param type 用户所属公司分类
     */
    @PostMapping("/getUserGoodsList")
    @ResponseBody
    public Result getUserGoods(@RequestParam(value = "openId",required = false) String openId,
                              @RequestParam(value = "type",required = false) Integer type){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }

            List<UserGoods> userGoodsList = userGoodsService.list(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_type",type));
            return Result.newSuccess(userGoodsList);
        }catch (Exception e){
            log.error("查询用户购买的商品信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }

    /**
     * 查询商品信息
     * @param openId 用户id
     * @param type 用户所属公司分类
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/getGoodsList")
    @ResponseBody
    public Result getGoodsList(@RequestParam(value = "openId",required = false) String openId,
                              @RequestParam(value = "type",required = false) Integer type){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            List<Goods> goodsList =null;
            if(0 == type){
                goodsList = goodsService.list(new QueryWrapper<Goods>()
                        .eq("type",type).eq("status",1).eq("sex",user.getSex()).orderByDesc("score"));
            }else{
                goodsList = goodsService.list(new QueryWrapper<Goods>()
                        .eq("type",type).eq("status",1).orderByDesc("score"));
            }

            List<UserGoods> userGoodsList = userGoodsService.list(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_type",type));
            //实物的话，可以无限兑换
            if(2 == type){
                List<GoodsVo> goodsVoList = new ArrayList<GoodsVo>();
                for(Goods goods : goodsList){
                    GoodsVo goodsVo= new GoodsVo();
                    BeanUtils.copyProperties(goods,goodsVo);
                    goodsVo.setIsDuiHuan(0);
                    goodsVoList.add(goodsVo);
                }
                return Result.newSuccess(goodsVoList);
            }

            //头像和皮肤

            //用户已经兑换过的商品信息
            Set<Integer> hasGoodSet = new HashSet<Integer>();
            for(UserGoods userGoods : userGoodsList){
                hasGoodSet.add(userGoods.getGoodsId());
            }
            List<GoodsVo> goodsVoList = new ArrayList<GoodsVo>();
            for(Goods goods : goodsList){
                GoodsVo goodsVo= new GoodsVo();
                BeanUtils.copyProperties(goods,goodsVo);
                if(hasGoodSet.contains(goods.getId())){
                    goodsVo.setIsDuiHuan(1);
                }else{
                    goodsVo.setIsDuiHuan(0);
                }

                goodsVoList.add(goodsVo);
            }
            return Result.newSuccess(goodsVoList);
        }catch (Exception e){
            log.error("查询商品信息出错，错误信息: {}",e);
            return Result.newFaild(e.getMessage());
        }
    }


    /**
     * 用户兑换奖品 只包括头像和皮肤（二期 主要添加已兑换数量字段）
     * @param openId 用户id
     * @param goodsId 商品id
     * @return 头像、皮肤兑换结果
     */
    @PostMapping("/userConvertGoods")
    @ResponseBody
    public Result userConvertGoods(@RequestParam(value = "openId",required = false) String openId,
                                   @RequestParam(value = "goodsId",required = false) Integer goodsId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFaild("用户不是内部员工");
            }
            if (null == goodsId || goodsId < 0) {
                return Result.newFaild("兑换商品id非空且大于0");
            }
            // 头像、皮肤商品兑换
            Result result = userGoodsService.userConvertGoods(openId, goodsId);
            return result;
        }catch (Exception e){
            log.error("用户兑换奖品(只包括头像和皮肤)出错，错误信息: {}",e);
            return Result.newFaild("用户兑换奖品(只包括头像和皮肤)出错");
        }
    }


    /**
     * 用户兑换实物奖品 (二期)
     * @param openId 用户id
     * @param goodsId 商品id
     * @return 实物商品兑换结果
     */
    @PostMapping("/userConvertShiWuGoods")
    @ResponseBody
    public Result userConvertShiWuGoods(@RequestParam(value = "openId",required = false) String openId,
                                        @RequestParam(value = "goodsId",required = false) Integer goodsId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFaild("未查到用户信息、openId为" + openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if (user.getType() != 1) {
                return Result.newFail("非A类人员不能兑换实物商品");
            }
            if (null == goodsId || goodsId < 0) {
                return Result.newFail("兑换商品id非空且大于0");
            }

            // 实物商品兑换
            Result result = userGoodsService.userConvertShiWuGoods(openId,goodsId);
            return result;
        } catch (Exception e) {
            log.error("用户兑换实物奖品出错，错误信息: {}",e);
            return Result.newFail("用户兑换实物奖品出错");
        }
    }


    /**
     * 用户保存收货地址、更新用户商品、用户、商品相关信息(二期，主要针对商品添加兑换数量字段)
     * @param openId 用户id
     * @param goodsId 商品id
     * @param name 用户名称
     * @param phone 用户电话号码
     * @param address 用户收货地址
     * @return 兑换商品是否成功
     */
    @PostMapping("/saveAddress")
    @ResponseBody
    public Result saveAddress(@RequestParam(value = "openId",required = false) String openId,
                              @RequestParam(value = "goodsId",required = false) Integer goodsId,
                              @RequestParam(value = "name",required = false) String name,
                              @RequestParam(value = "phone",required = false) String phone,
                              @RequestParam(value = "address",required = false) String address){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            if (StringUtils.isEmpty(name.trim()) || StringUtils.isEmpty(phone.trim()) || StringUtils.isEmpty(address.trim())) {
                return Result.newFail("用户姓名、电话、收货地址不能为空");
            }
            if (null == goodsId || goodsId < 1) {
                return Result.newFail("兑换商品id非空且大于0");
            }
            name = name.trim();
            phone = phone.trim();
            address = address.trim();
            Result result = userGoodsService.saveAddress(openId, goodsId, name, phone, address);
            return result;
        }catch (Exception e){
            log.error("用户保存兑换实物地址信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }


    /**
     * 用户使用商品
     * @param openId 用户id
     * @param goodsId 商品id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/applyGoods")
    @ResponseBody
    public Result applyGoods(@RequestParam(value = "openId",required = false) String openId,
                               @RequestParam(value = "goodsId",required = false) Integer goodsId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }
            UserGoods userGoods = userGoodsService.getOne(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_id",goodsId));
            if(null == userGoods){
                return Result.newFail("用户并未拥有这个商品");
            }
            if(1 == userGoods.getIsFlag()){
                return Result.newFail("该商品本身就是在使用的状态");
            }

            UserGoods oldUserGoods = userGoodsService.getOne(new QueryWrapper<UserGoods>().eq("open_id",openId)
                    .eq("goods_type",userGoods.getGoodsType()).eq("is_flag",1));
            if(null != oldUserGoods){
                //先把以前使用的 同一个类型的商品状态改为未使用
                oldUserGoods.setIsFlag(0);
                userGoodsService.updateById(oldUserGoods);
            }
            //把当前的改成使用中
            userGoods.setIsFlag(1);
            userGoods.setIsUsed(1);
            userGoodsService.updateById(userGoods);

            return Result.newSuccess("修改成功");
        }catch (Exception e){
            log.error("用户使用商品信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }


    /**
     * 用户查询了背包 就关掉右上角的红点
     * @param openId 用户id
     * @Auth: zhangcheng
     * @Date: 2020/6/2 14:38
     */
    @PostMapping("/updateAllGoodsUsed")
    @ResponseBody
    public Result updateAllGoodsUsed(@RequestParam(value = "openId",required = false) String openId){
        try {
            User user = userService.getUserByOpenId(openId);
            if(null == user){
                return Result.newFail("未查到用户信息、openId为：{}",openId);
            }
            if(StringUtils.isEmpty(user.getIdCard())){
                return Result.newFail("用户不是内部员工");
            }

            List<UserGoods> userGoodsList = userGoodsService.
                    list(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("is_used",0));
            for(UserGoods userGood : userGoodsList){
                userGood.setIsUsed(1);
                userGoodsService.updateById(userGood);
            }

            return Result.newSuccess();
        }catch (Exception e){
            log.error("用户查询具体商品使用情况信息出错，错误信息: {}",e);
            return Result.newFail();
        }
    }

}
