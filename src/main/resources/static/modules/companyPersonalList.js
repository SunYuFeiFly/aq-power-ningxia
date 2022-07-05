layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/rank/selectPagesPersonal",
        method: "POST",
        cols: [[
            {field: 'rankNo', title: '排名', width: 100},
            {field: 'name', title: '用户名称', width: 200},
            {field: 'honorName', title: '经验值', width: 200},
            {field: 'companyName', title: '经验值', width: 200},
            {field: 'branchName', title: '经验值', width: 200},
            {field: 'allAch', title: '成就点数', width: 200},
            {field: 'allExp', title: '经验值', width: 200},
            {field: 'compositeScore', title: '综合分数', width: 200}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    exports('companyPersonalList', {});
});