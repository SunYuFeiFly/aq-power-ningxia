layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/goods/selectExchangeByGoodsId?userGoodsId="+window.userGoods_id,
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 100},
            {field: 'nickName', title: '兑换人姓名', width: 200},
            {field: 'createTime', title: '兑换时间', width: 200},
            {field: 'address', title: '收货地址', width: 200},

        ]],
        page: true,
        limit: 10,
        skin: 'line'
    });


    exports('exchangeList', {})
});