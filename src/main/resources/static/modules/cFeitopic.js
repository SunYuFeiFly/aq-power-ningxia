layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/feiBranchTopic/selectPages",
        where: {"companyType":3},
        method: "POST",
        cols: [[
            {type:'checkbox'},
            {field: 'id', title: 'ID', width: 100, sort:true, align:'center'},
            // {field: 'topicTypeName', title: '问题种类', width: 200},
            {field: 'topic_type', title: '题目类型', width: 100, align:'center',templet:function(d){
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
            {field: 'title', title: '问题标题', width: 1000},
            {title: '操作', width: 200, toolbar: '#table-useradmin-webuser', align:'center'}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    //监听工具条
    table.on('tool(LAY-user-manage)', function (obj) {
        var data = obj.data;
        console.log(data)
        console.info('data.id:' + data.id);
        if (obj.event === 'search') {
            layer.open({
                type: 2
                , area: ['1100px', '700px']
                , maxmin: true
                , title: "查看问题"
                , content: 'system/feiBranchTopic/insertOrEditInit?topicId=' + data.id +"&companyType=3"
            });
        } else if (obj.event === 'del') {
            layer.confirm('确认删除？', function () {
                $.ajax({
                    url: "system/feiBranchTopic/delTopic",
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

    exports('cFeitopic', {});
});