layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/activity/selectPages",
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 100},
            {field: 'name', title: '赛事标题', width: 150},
            {field: 'startTime', title: '活动开始时间', width: 150},
            {field: 'endTime', title: '活动结束时间', width: 150},
            {field: 'deleted', title: '状态', width: 100,templet:function(d){
                var msg = "-";
                if (d.deleted==0){
                    msg="使用中";
                }else if(d.deleted==1){
                    msg="已删除";
                }
                return msg;
            }},
            {title: '操作', width: 400, toolbar: '#table-useradmin-webuser'}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    //监听工具条
    table.on('tool(LAY-user-manage)', function (obj) {
        var data = obj.data;
        console.info(data);
        if (obj.event === 'editEnter') {
            layer.open({
                type: 2
                , area: ['900px', '700px']
                , maxmin: true
                , title: "移入题目"
                , content: 'system/activity/editEnter?id=' + data.id
            });
        }else if (obj.event === 'editCome') {
            layer.open({
                type: 2
                , area: ['900px', '700px']
                , maxmin: true
                , title: "移出题目"
                , content: 'system/activity/editCome?id=' + data.id
            });
        }
        else if (obj.event === 'edit') {
            layer.open({
                type: 2
                , area: ['900px', '800px']
                , maxmin: true
                , title: "编辑"
                , content: 'system/activity/edit?id=' + data.id
            });
        }else if (obj.event === 'rank') {
            layer.open({
                type: 2
                , area: ['900px', '800px']
                , maxmin: true
                , title: "排行榜"
                , content: 'system/activity/activityRankInit?id=' + data.id
            });
        } else if (obj.event === 'del') {
            layer.confirm('确认删除？', function () {
                $.ajax({
                    url: "system/activity/deleteTeam",
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

    exports('activity', {});
});