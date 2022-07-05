package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.api.GoodsApi;
import com.wangchen.common.Result;
import com.wangchen.entity.*;
import com.wangchen.mapper.UserGoodsMapper;
import com.wangchen.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class UserGoodsServiceImpl extends ServiceImpl<UserGoodsMapper, UserGoods> implements UserGoodsService {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private UserGoodsAddressService userGoodsAddressService;

    @Autowired
    private UserGoodsService userGoodsService;


    /**
     * 用户兑换奖品 只包括头像和皮肤（二期 主要添加已兑换数量字段）
     * @param openId 用户id
     * @param goodsId 商品id
     * @return 兑换结果
     */
    @Override
    @Transactional
    public Result userConvertGoods(String openId, Integer goodsId) {
        User user = userService.getUserByOpenId(openId);
        Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",goodsId).eq("status",1));
        if(null == goods){
            return Result.newFail("商品不存在");
        }
        if (2 == goods.getType()) {
            return Result.newFail("兑换商品类型错误");
        }
        // 男性人员只能兑换男性专用皮肤，女性人员只能兑换女性专用皮肤
        if (0 == goods.getType() && !user.getSex().equals(goods.getSex())) {
            return Result.newFail("您不能兑换与您性别通用性不符合商品");
        }
        UserGoods userGoods = this.getOne(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_id",goodsId));
        if(null != userGoods){
            return Result.newFail("该奖品您已经兑换过一次");
        }
        if(goods.getScore().intValue() > user.getAllCoin().intValue()){
            return Result.newFail("塔币不足");
        }
        // 用户兑换记录
        userGoods = new UserGoods();
        userGoods.setOpenId(user.getOpenId());
        userGoods.setGoodsId(goodsId);
        userGoods.setGoodsType(goods.getType());
        userGoods.setGoodsName(goods.getName());
        userGoods.setGoodsUrl(goods.getUrl());
        userGoods.setScoreNow(goods.getScore());
        userGoods.setIsFlag(0);
        userGoods.setDeleted(0);
        userGoods.setIsUsed(0);
        userGoods.setCreateTime(new Date());
        userGoods.setUpdateTime(new Date());
        this.save(userGoods);

        // 用户塔币扣除、兑换奖品次数更新
        user.setAllCoin(user.getAllCoin().intValue() - goods.getScore().intValue());
        user.setPrizeCount(user.getPrizeCount() + 1);
        user.setUpdateDate(new Date());
        userService.updateById(user);

        // 更改商品已兑换数量
        goods.setGoodsExchangeNum(goods.getGoodsExchangeNum() + 1);
        goods.setUpdateTime(new Date());
        goodsService.updateById(goods);

        //每日公告上面的恭喜
        HotLog hotLog1 = new HotLog();
        hotLog1.setOpenId(openId);
        hotLog1.setRemarks("恭喜"+ user.getName() +"获得了"+ userGoods.getGoodsName() +"商品");
        hotLog1.setCreateDate(new Date());
        hotLogService.save(hotLog1);

        return Result.newSuccess("兑换成功");
    }


    /**
     * 用户兑换实物奖品 (二期)
     * @param openId 用户id
     * @param goodsId 商品id
     * @return 实物商品兑换结果
     */
    @Override
    @Transactional
    public Result userConvertShiWuGoods(String openId, Integer goodsId) {
        User user = userService.getUserByOpenId(openId);
        Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",goodsId).eq("status",1));
        if(null == goods){
            return Result.newFail("商品不存在");
        }
        if (goods.getScore() < 1) {
            return Result.newFail("该实物商品数量不足，无法兑换");
        }
        if(goods.getScore().intValue() > user.getAllCoin().intValue()){
            return Result.newFail("塔币不足");
        }
        UserGoods userGoods = this.getOne(new QueryWrapper<UserGoods>().eq("open_id",openId).eq("goods_id",goodsId));
        if(null != userGoods){
            return Result.newFail("该奖品您已经兑换过一次");
        }
        // 用户兑换记录
        userGoods = new UserGoods();
        userGoods.setOpenId(user.getOpenId());
        userGoods.setGoodsId(goodsId);
        userGoods.setGoodsName(goods.getName());
        userGoods.setGoodsType(goods.getType());
        userGoods.setGoodsUrl(goods.getUrl());
        userGoods.setScoreNow(goods.getScore());
        userGoods.setIsFlag(0);
        userGoods.setCreateTime(new Date());
        userGoods.setDeleted(0);
        this.save(userGoods);

        // 用户塔币扣除、兑换奖品次数更新
        user.setAllCoin(user.getAllCoin().intValue() - goods.getScore().intValue());
        user.setPrizeCount(user.getPrizeCount() + 1);
        user.setUpdateDate(new Date());
        userService.updateById(user);

        // 更改商品已兑换数量、可兑换数量
        goods.setScore(goods.getStore() - 1);
        goods.setGoodsExchangeNum(goods.getGoodsExchangeNum() + 1);
        goodsService.updateById(goods);

        //每日公告上面的恭喜
        HotLog hotLog1 = new HotLog();
        hotLog1.setOpenId(openId);
        hotLog1.setRemarks("恭喜" +user.getName() +"获得了"+ userGoods.getGoodsName() +"商品");
        hotLog1.setCreateDate(new Date());
        hotLogService.save(hotLog1);

        return Result.newSuccess("兑换成功");
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
    @Override
    @Transactional
    public Result saveAddress(String openId, Integer goodsId, String name, String phone, String address) {
        User user = userService.getUserByOpenId(openId);
        Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",goodsId).eq("status",1));
        if(null == goods){
            return Result.newFail("商品不存在");
        }
        if(goods.getScore().intValue() > user.getAllCoin().intValue()){
            return Result.newFail("塔币不足");
        }
        if (2 == goods.getType()) {
            if (1 != user.getType()) {
                return Result.newFail("非A类人员不可以兑换实物商品");
            } else {
                synchronized (GoodsApi.class) {
                    goods = goodsService.getOne(new QueryWrapper<Goods>().eq("id",goodsId));
                    if (goods.getStore() < 1) {
                        return Result.newFail("商品数量不足，无法兑换");
                    } else {
                        // 保存用户收货地址信息
                        UserGoodsAddress userGoodsAddress = new UserGoodsAddress();
                        userGoodsAddress.setOpenId(openId);
                        userGoodsAddress.setName(name);
                        userGoodsAddress.setPhone(phone);
                        userGoodsAddress.setAddress(address);
                        userGoodsAddress.setCreateTime(new Date());
                        userGoodsAddressService.save(userGoodsAddress);

                        // 保存用户商品信息
                        UserGoods userGoods = new UserGoods();
                        userGoods.setOpenId(user.getOpenId());
                        userGoods.setGoodsId(goodsId);
                        userGoods.setGoodsType(goods.getType());
                        userGoods.setGoodsName(goods.getName());
                        userGoods.setGoodsUrl(goods.getUrl());
                        userGoods.setScoreNow(goods.getScore());
                        userGoods.setIsFlag(0);
                        userGoods.setAddressId(userGoodsAddress.getId());
                        userGoods.setCreateTime(new Date());
                        userGoods.setUpdateTime(new Date());
                        userGoods.setDeleted(0);
                        userGoods.setIsUsed(1);
                        userGoodsService.save(userGoods);

                        // 用户塔币扣除、兑换奖品次数更新
                        user.setAllCoin(user.getAllCoin().intValue() - goods.getScore().intValue());
                        user.setPrizeCount(user.getPrizeCount() + 1);
                        user.setUpdateDate(new Date());
                        userService.updateById(user);

                        // 更改商品已兑换数量、可兑换数量(默认每次兑换一件)
                        goods.setStore(goods.getStore() - 1);
                        goods.setGoodsExchangeNum(goods.getGoodsExchangeNum() + 1);
                        goods.setUpdateTime(new Date());
                        goodsService.updateById(goods);

                        //每日公告上面的恭喜
                        HotLog hotLog = new HotLog();
                        hotLog.setOpenId(openId);
                        hotLog.setRemarks("恭喜"+user.getName()+"获得了"+userGoods.getGoodsName()+"商品");
                        hotLog.setCreateDate(new Date());
                        hotLogService.save(hotLog);
                    }
                }
            }
        } else {
            return Result.newFail("兑换商品不是实物商品");
        }

        return Result.newSuccess("兑换成功");
    }

}
