layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        defaultToolbar: '',
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/activity/findEnterList",
        method: "POST",
        toolbar: '<div><button class="layui-btn" type="button" lay-event="saveArticle">确认添加</button></div>',
        cols: [[
            {type: 'checkbox'},
            {field: 'id', title: '序号', width: 100},
            {field: 'type', title: '部门', width: 100,templet:function(d){
                    var msg = "-";
                    if (d.type==1){
                        msg="通信发展";
                    }else if(d.type==2){
                        msg="运营维护";
                    }else if(d.type==3){
                        msg="行业拓展";
                    }else if(d.type==4){
                        msg="能源经营";
                    }else if(d.type==5){
                        msg="财务管理";
                    }else if(d.type==6){
                        msg="人力资源";
                    }else if(d.type==7){
                        msg="党群纪检";
                    }else if(d.type==8){
                        msg="综合管理";
                    }
                    return msg;
                }},
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
            url: "system/activity/topicEnter",
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

    exports('activityEnterTopic', {})
});