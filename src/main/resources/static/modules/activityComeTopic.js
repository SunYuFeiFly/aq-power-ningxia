layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        defaultToolbar: '',
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/activity/findComeList?id="+window.a_id,
        method: "POST",
        toolbar: '<div><button class="layui-btn" type="button" lay-event="saveArticle">确认移出</button></div>',
        cols: [[
            {type: 'checkbox'},
            {field: 'id', title: '序号', width: 100},
            // {field: 'topicTypeName', title: '问题种类', width: 200},
            {field: 'topic_type', title: '题目类型', width: 100,templet:function(d){
                    var msg = "-";
                    if (d.topicType==0){
                        msg="选择题";
                    }else if(d.topicType==1){
                        msg="填空题";
                    }else if(d.topicType==2){
                        msg="判断题";
                    }
                    return msg;
                }},
            {field: 'title', title: '问题标题', width: 400},
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

    // //修改 信息
    // form.on('submit(saveArticle)', function (data) {
    //
    //
    //
    //     console.log(ids);
    //     layer.confirm('确认保存吗？', function () {
    //         $.ajax({
    //             url: "admin/goods/",
    //             method: "POST",
    //             data: ids,
    //             success: function (res) {
    //                 if (res.code === "0000") {
    //                     layer.msg('操作成功', {time: 1000});
    //                     setInterval(function () {
    //                         this.location.reload();
    //                         var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
    //                         parent.layer.close(index); //再执行关闭
    //                     }, 1000);
    //                     table.reload("LAY-user-manage", {
    //                         page: {
    //                             curr: 1 //重新从第 1 页开始
    //                         }
    //                     });
    //                 } else {
    //                     layer.msg(res.msg, {time: 1000});
    //                 }
    //             }
    //         });
    //     });
    //     return true;
    // });

    exports('activityComeTopic', {})
});