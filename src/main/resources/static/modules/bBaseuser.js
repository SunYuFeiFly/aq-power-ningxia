layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/baseuser/selectPages",
        where: {"companyType":2},
        method: "POST",
        cols: [[
            {type:'checkbox'},
            {field: 'id', title: '序号', width: 100},
            {field: 'name', title: '用户名称', width: 150},
            {field: 'idCard', title: '身份证', width: 200},
            {field: 'phone', title: '电话', width: 150},
            {
                field: 'sex', title: '性别', width: 120, templet: function (d) {
                    var msg = "";
                    if (d.sex == 0) {
                        msg = "男";
                    } else if (d.sex == 1) {
                        msg = "女";
                    }
                    return msg;
                }
            },
            {field: 'company', title: '公司', width: 200},
            {field: 'branchOne', title: '部门1', width: 180},
            {field: 'branchTwo', title: '部门2', width: 180},
            {field: 'branchThree', title: '部门3', width: 180},
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
                , area: ['800px', '460px']
                , maxmin: true
                , title: "编辑商品"
                , content: 'system/baseuser/edit?companyType=2&id=' + data.id
            });
        } else if (obj.event === 'del') {
            layer.confirm('确认删除？(与该用户绑定的游戏用户所有数据都将被清理掉)', function () {
                $.ajax({
                    url: "system/baseuser/delBaseUser",
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



    exports('bBaseuser', {});
});