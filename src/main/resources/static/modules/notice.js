layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/notice/selectPages",
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 100},
            {field: 'title', title: '标题', width: 200},
            {
                field: 'url', title: '图片',sort: true, width: 150,  templet: function(d){
                    return '<div  class="show_img" ><img src="'+d.url+'" alt="" width="50px" height="100px"></a></div>';
                }
            },
            {field: 'content', title: '内容', width: 100},
            {
                field: 'status', title: '状态', width: 100, templet: function (d) {
                    var msg = "";
                    if (d.status == 0) {
                        msg = "开启";
                    } else if (d.status == 1) {
                        msg = "禁用";
                    }
                    return msg;
                }
            },
            {title: '操作', width: 150, toolbar: '#table-useradmin-webuser'}
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
                , title: "编辑公告"
                , content: 'system/notice/edit?id=' + data.id
            });
        } else if (obj.event === 'del') {
            layer.confirm('确认删除？', function () {
                $.ajax({
                    url: "system/notice/deleteNotice",
                    method: "POST",
                    data: {id: data.id},
                    success: function (res) {
                        if (res.success) {
                            obj.del();
                            //登入成功的提示与跳转
                            layer.msg('删除成功', {
                                offset: '15px'
                                , icon: 1
                                , time: 1000
                            });
                            table.reload('LAY-user-manage');
                        } else {
                            layer.msg(res.msg, {time: 1000});
                        }
                    }
                });
                layer.closeAll();
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
    exports('notice', {});
});