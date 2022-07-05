package com.wangchen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangchen.common.Result;
import com.wangchen.common.ResultLayuiTable;
import com.wangchen.common.exception.BusinessException;
import com.wangchen.common.utils.StringUtils;
import com.wangchen.entity.Goods;
import com.wangchen.entity.UserGoods;
import com.wangchen.mapper.GoodsMapper;
import com.wangchen.mapper.UserGoodsMapper;
import com.wangchen.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-02
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserGoodsMapper userGoodsMapper;

    /**
     * 商品列表页面（二期）
     * @param page 页码
     * @param limit 每页数据量
     * @param name 商品名称（用于模糊搜索）
     * @return 商品数据集合
     */
    @Override
    @Transactional
    public Result selectPages(int page, int limit, String name) {
        IPage<Goods> pages = goodsMapper.selectPage(new Page<Goods>(page, limit),
                new QueryWrapper<Goods>().like(StringUtils.isNotEmpty(name), "name", name).orderByDesc("create_time"));
        return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
    }


    /**
     * 商品上下架 (二期)
     * @param id 商品id
     * @param status 商品状态 (0下架 1上架)
     */
    @Override
    public void isStatus(Integer id, Integer status) {
        Goods goods = goodsMapper.selectById(id);
        goods.setStatus(status);
        goods.setUpdateTime(new Date());
        goodsMapper.updateById(goods);
    }


    /**
     * 新增修改商品 （二期）
     * @param goods 商品对象
     */
    @Override
    public void editGoods(Goods goods) {
        if (null != goods) {
            if (null == goods.getId()) {
                // 新增操作
                if (2 == goods.getType()) {
                    if (goods.getScore() < 1) {
                        throw new BusinessException("商品信息新增操作，实物商品数量不能小于1");
                    }
                    goods.setSex(2);
                }
                goods.setCreateTime(new Date());
                goods.setUpdateTime(new Date());
                goods.setGoodsExchangeNum(0);
                goods.setStatus(1);
                goods.setDeleted(false);
                goodsMapper.insert(goods);
            } else {
                // 修改操作
                if (2 == goods.getType()) {
                    if (goods.getScore() < 0) {
                        throw new BusinessException("商品信息修改操作，实物商品数量不能小于0");
                    }
                }
                goods.setUpdateTime(new Date());
                goodsMapper.updateById(goods);
            }
        }
    }


    /**
     * 查询兑换列表（二期）
     * @param name 商品名称（用于模糊搜索）
     * @param page 页码
     * @param limit 每页数据量
     */
    @Override
    @Transactional
    public Result findExchangeList(String name, Integer page, Integer limit) {
        IPage<Goods> pages = goodsMapper.selectPage(new Page<Goods>(page, limit),
                new QueryWrapper<Goods>().like(StringUtils.isNotEmpty(name), "name", name).orderByDesc("create_time"));
        for (Goods record : pages.getRecords()) {
            record.setGoodsExchangeNum(userGoodsMapper.selectCount(new QueryWrapper<UserGoods>().eq("goods_id", record.getId()).eq("goods_name",record.getName())));
        }
        return ResultLayuiTable.newSuccess(pages.getTotal(), pages.getRecords());
    }

}
