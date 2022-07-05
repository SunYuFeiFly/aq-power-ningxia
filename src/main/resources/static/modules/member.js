layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/user/selectPages",
        method: "POST",
        cols: [[
            // {field: 'id', title: '序号', width: 100},
            // {field: 'name', title: '公司名称', width: 200},
            // {field: 'allNum', title: '公司总人数', width: 200},
            // {field: 'registerNum', title: '已注册', width: 200},
            // {field: 'signNum', title: '今日签到人数', width: 200},
            // {field: 'dayGameNum', title: '完成今日答题人数', width: 200},
            // {title: '操作', width: 200, toolbar: '#table-useradmin-webuser'}
            {field: 'id', title: '序号', width: 100},
            {field: 'companyName', title: '公司名称', width: 200},
            {field: 'countNum', title: '总人数', width: 100},
            {field: 'registerNum', title: '注册人数', width: 100},
            {field: 'allCoinNum', title: '塔币', width: 150},
            {field: 'allAchievementNum', title: '成就值', width: 150},
            {field: 'allExperienceNum', title: '经验值', width: 150},
            {field: 'allSignNum', title: '签到人数', width: 100},
            {field: 'allSignRate', title: '签到率（%）', width: 160},
            {field: 'dayGameRate', title: '每日答题参与度（%）', width: 170},
            {field: 'dayGameTrueRate', title: '每日答题正确率（%）', width: 170},
            {field: 'oneVsOneRate', title: '个人赛参与度', width: 150},
            {field: 'threeVsThreeRate', title: '团队赛参与度', width: 150},
            {title: '操作', width: 100, toolbar: '#table-useradmin-webuser'}

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
                , area: ['1650px', '665px']
                , maxmin: true
                , title: "查看详情"
                , content: 'system/user/edit?id=' + data.id
            });
        }
    });


    exports('member', {});
});