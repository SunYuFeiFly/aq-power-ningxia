layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/audit/selectPages",
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 100},
            {field: 'name', title: '提交人', width: 100},
            {field: 'createTime', title: '提交时间', width: 200},
            {field: 'buMenTypeTxt', title: '部门', width: 150},
            {
                field: 'tikuType', title: '题目专业', width: 150, templet: function (d) {
                    var msg = "";
                    if (d.tikuType == 0) {
                        msg = "专业";
                    } else if (d.tikuType == 1) {
                        msg = "应知应会";
                    }
                    return msg;
                }
            },
            {field: 'topicTypeTxtStr', title: '题目类型', width: 150},
            {
                field: 'status', title: '审核状态', width: 150, templet: function (d) {
                    var msg = "";
                    if (d.status == 0) {
                        msg = "待审核";
                    } else if (d.status == 1) {
                        msg = "已通过";
                    } else if (d.status == 2) {
                        msg = "已驳回";
                    }
                    return msg;
                }
            },
            {field: 'title', title: '题目详情', width: 200},
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
                , area: ['800px', '650px']
                , maxmin: true
                , title: "查看详情"
                , content: 'system/audit/edit?id=' + data.id
            });
        }
    });


    exports('audit', {});
});