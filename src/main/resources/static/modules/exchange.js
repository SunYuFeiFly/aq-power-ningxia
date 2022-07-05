layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/goods/findExchangeList",
        method: "POST",
        cols: [[
            {field: 'id', title: 'ID', width: 100},
            {field: 'name', title: '商品名称', width: 200},
            {
                field: 'type', title: '商品类型', width: 200, templet: function (d) {
                    var msg = "";
                    if (d.type == 0) {
                        msg = "皮肤";
                    } else if (d.type == 1) {
                        msg = "头像";
                    } else if (d.type == 2) {
                        msg = "实物";
                    }
                    return msg;
                }
            },
            {
                field: 'url', title: '商品图片',sort: true, width: 150,  templet: function(d){
                    return '<div  class="show_img" ><img src="'+d.url+'" alt="" width="50px" height="100px"></a></div>';
                }
            },
            {field: 'goodsExchangeNum', title: '兑换数量', width: 100},
            {title: '操作', width: 200, toolbar: '#table-useradmin-webuser'}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    //监听工具条
    table.on('tool(LAY-user-manage)', function (obj) {
        var data = obj.data;
        if (obj.event === 'edit') {
            layer.open({
                type: 2
                , area: ['800px', '700px']
                , maxmin: true
                , title: "查看详情"
                , content: 'system/goods/editExchange?id=' + data.id
            });
        }
    });

    $(document).on("click", ".show_img", function () {
        var t = $(this).find("img");
        //页面层
        layer.open({
            type: 1,
            skin: 'layui-layer-rim', //加上边框
            area: ['60%', '60%'], //宽高
            shadeClose: true, //开启遮罩关闭
            end: function (index, layero) {
                return false;
            },
            content: '<div style="text-align:center"><img src="' + $(t).attr('src') + '" /></div>'
        });
    });

    exports('exchange', {});
});