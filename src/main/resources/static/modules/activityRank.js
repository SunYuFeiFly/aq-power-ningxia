layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        defaultToolbar: '',
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/activity/activityRankList?id="+window.a_id,
        method: "POST",
        // toolbar: '<div><button class="layui-btn" type="button" lay-event="saveArticle">确认移出</button></div>',
        cols: [[
            // {type: 'checkbox'},
            // {field: 'id', title: '序号', width: 100},
            // {field: 'topicTypeName', title: '问题种类', width: 200},
            {field: 'rankNo', title: '排名', width: 150},
            {field: 'name', title: '名称', width: 150},
            {field: 'score', title: '分数', width: 100},
        ]],
        page: true,
        limit: 10,
        skin: 'line'
    });
    table.on('toolbar', function (data) {
        var ids = [];
        var checkStatus = table.checkStatus(data.config.id);
        for (var i = 0; i < checkStatus.data.length; i++) {
            ids.push(checkStatus.data[i].id);
        }

        $.ajax({
            url: "system/activity/topicCome",
            method: "POST",
            data:{"ids":ids,"id":window.a_id},
            traditional: true,
            success: function (data) {
                if (data.code === "0000") {
                    layer.msg('操作成功', {time: 1000});
                    setInterval(function () {
                        this.parent.location.reload();
                        var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
                        parent.layer.close(index); //再执行关闭
                    }, 1000);
                    table.reload("LAY-user-manage", {
                        page: {
                            curr: 1 //重新从第 1 页开始
                        }
                    });
                } else {
                    layer.msg(data.msg, {time: 1000});
                }
            }
        });
    })

    exports('activityRank', {})
});