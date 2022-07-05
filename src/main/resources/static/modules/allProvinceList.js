layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/rank/selectPagesAll",
        method: "POST",
        totalRow: true,
        cols: [[
            {field: 'rankNo', title: '排名', width: 100, sort:true, align:'center'},
            {field: 'name', title: '用户名称', width: 200, align:'center'},
            {field: 'honorName', title: '经验值', width: 200, totalRow: true, sort:true, align:'center'},
            {field: 'companyName', title: '经验值', width: 200, totalRow: true, sort:true, align:'center'},
            {field: 'branchName', title: '经验值', width: 200, totalRow: true, sort:true, align:'center'},
            {field: 'allAch', title: '成就点数', width: 200, totalRow: true, sort:true, align:'center'},
            {field: 'allExp', title: '经验值', width: 200, totalRow: true, sort:true, align:'center'},
            {field: 'compositeScore', title: '综合分数', width: 200, totalRow: true, sort:true, align:'center'}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    exports('allProvinceList', {});
});