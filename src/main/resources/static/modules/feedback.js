layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/feedback/selectPages",
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 100},
            {field: 'name', title: '提交人', width: 200},
            {field: 'phone', title: '联系电话', width: 200},
            {
                field: 'type', title: '类型', width: 120, templet: function (d) {
                    var msg = "";
                    if (d.type == 0) {
                        msg = "普通反馈";
                    } else if (d.type == 1) {
                        msg = "部门反馈";
                    }
                    return msg;
                }
            },
            {field: 'createTime', title: '提交时间', width: 200},
            {
                field: 'status',
                title: '上下架',
                width: 150,
                templet: function (d) {
                    var state = "";
                    if (d.status == "1") {
                        state = "<input type='checkbox' value='" + d.id + "' id='status' lay-filter='stat' checked='checked' name='status'  lay-skin='switch' lay-text='已处理|未处理' >";
                    } else {
                        state = "<input type='checkbox' value='" + d.id + "' id='status' lay-filter='stat'  name='status'  lay-skin='switch' lay-text='已处理|未处理' >";
                    }
                    return state;
                }
            },
            // {field: 'context', title: '反馈内容', width: 200},
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
                , area: ['800px', '450px']
                , maxmin: true
                , title: "查看反馈"
                , content: 'system/feedback/edit?id=' + data.id
            });
        } else if (obj.event === 'del') {
            layer.confirm('确认删除？', function () {
                $.ajax({
                    url: "system/feedback/deleteNotice",
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
    form.on('switch(stat)', function (data) {
        var contexts;
        var sta;
        var x = data.elem.checked;//判断开关状态
        if (x == true) {
            contexts = "标记为已处理";
            sta = 1;
        } else {
            contexts = "标记为未处理";
            sta = 0;
        }
        //自定义弹窗
        layer.open({
            content: "你确定要" + contexts + "?"
            , btn: ['确定', '取消']
            , yes: function (index, layero) {
                //按钮确定【按钮一】的回调
                data.elem.checked = x;
                //对商品进行上架或下架处理
                $.ajax({
                    type: "post",
                    url: 'system/feedback/isStatus',
                    data: {
                        //上下架的参数
                        "id": data.value,
                        "status": sta
                    },
                    success: function (data) {
                        console.info(data)
                        if (data.success) {
                            layer.msg(contexts + '成功',
                                // 提示的样式
                                {icon: 1, time: 2000,});
                            setInterval(function () {
                                this.parent.location.reload();
                                var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
                                parent.layer.close(index); //再执行关闭
                            }, 1000);
                            table.reload("LAY-user-manage", {
                                where: null,
                                page: {
                                    curr: 1 //重新从第 1 页开始
                                }
                            });
                        }
                    }
                });

                form.render();
                layer.close(index);

            }
            , btn2: function (index, layero) {
                //按钮【按钮二】的回调
                data.elem.checked = !x;
                form.render();
                layer.close(index);
                //return false 开启该代码可禁止点击该按钮关闭
            }
            , cancel: function () {
                //右上角关闭回调
                data.elem.checked = !x;
                form.render();
                // return false; //开启该代码可禁止点击该按钮关闭
            }
        });

        return true;

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
    exports('feedback', {});
});