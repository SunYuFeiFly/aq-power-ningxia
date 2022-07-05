layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/user/selectPages",
        where: {"companyType":2},
        method: "POST",
        cols: [[
            {field: 'id', title: '序号', width: 60, align:true, align:'center'},
            {field: 'companyName', title: '公司名称', width: 140, align:true, align:'center'},
            {field: 'countNum', title: '总人数', width: 100,totalRow: true, align:true, align:'center', sort:true},
            {field: 'registerNum', title: '注册人数', width: 120,totalRow: true, align:true, align:'center', sort:true},
            {field: 'allCoinNum', title: '塔币', width: 150,totalRow: true, align:true, align:'center', sort:true},
            {field: 'allAchievementNum', title: '成就值', width: 120,totalRow: true, align:true, align:'center', sort:true},
            {field: 'allExperienceNum', title: '经验值', width: 150,totalRow: true, align:true, align:'center', sort:true},
            {field: 'allSignNum', title: '签到人数', width: 120, align:true, align:'center', sort:true},
            {field: 'allSignRate', title: '签到率（%）', width: 130, align:true, align:'center', sort:true},
            {field: 'dayGameRate', title: '每日答题参与度（%）', width: 190, align:true, align:'center', sort:true},
            {field: 'dayGameTrueRate', title: '每日答题正确率（%）', width: 190, align:true, align:'center', sort:true},
            {field: 'oneVsOneRate', title: '个人赛参与度', width: 150, align:true, align:'center', sort:true},
            {field: 'teamVsTeamRate', title: '团队赛参与度', width: 150, align:true, align:'center', sort:true},
            {title: '操作', width: 100, align:true, align:'center', toolbar: '#table-useradmin-webuser'}
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
                , content: 'system/user/edit?companyType=2&id=' + data.id
            });
        }
    });


    exports('bMember', {});
});