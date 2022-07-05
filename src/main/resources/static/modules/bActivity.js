layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/activity/selectPages",
        where: {"companyType":2},
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 150, align:'center'},
            {field: 'name', title: '赛事标题', width: 650},
            {field: 'startTime', title: '活动开始时间', width: 250, align:'center'},
            {field: 'endTime', title: '活动结束时间', width: 250, align:'center'},
            {field: 'deleted', title: '状态', width: 100, align:'center', templet:function(d){
                var msg = "-";
                if (d.deleted==0){
                    msg="使用中";
                }else if(d.deleted==1){
                    msg="已删除";
                }else if(d.deleted==2){
                    msg="待上线";
                }
                return msg;
            }},
            {title: '操作', width: 280, align:'center', toolbar: '#table-useradmin-webuser'}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    //监听工具条
    table.on('tool(LAY-user-manage)', function (obj) {
        var data = obj.data;
        console.info(data);
        if (obj.event === 'edit') {
            layer.open({
                type: 2
                , area: ['900px', '320px']
                , maxmin: true
                , title: "编辑"
                , content: 'system/activity/edit?companyType=2&id=' + data.id
            });
        }else if (obj.event === 'select') {
            layer.open({
                type: 2
                , area: ['900px', '800px']
                , maxmin: true
                , title: "查看题目"
                , content: 'system/activity/select?companyType=2&activityId=' + data.id
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

    exports('bActivity', {});
});