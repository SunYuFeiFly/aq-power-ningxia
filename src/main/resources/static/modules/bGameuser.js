layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/gameuser/selectPages",
        where: {"companyType":2},
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 150, sort: true},
            {field: 'name', title: '用户名称', width: 200},
            {field: 'mobile', title: '电话', width: 200},
            {
                field: 'sex', title: '性别', width: 200, templet: function (d) {
                    var msg = "";
                    if (d.sex == 0) {
                        msg = "男";
                    } else if (d.sex == 1) {
                        msg = "女";
                    }
                    return msg;
                }
            },
            {field: 'companyName', title: '公司', width: 200},
            // {field: 'branchName', title: '部门', width: 200},
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
                , area: ['1350px', '800px']
                , maxmin: true
                , title: "查看详情"
                , content: 'system/gameuser/edit?id=' + data.id
            });
        }
    });


    exports('bGameuser', {});
});