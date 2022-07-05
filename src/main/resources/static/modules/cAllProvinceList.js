layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/rank/selectPagesAll",
        where: {"type":2,"companyType":3},
        method: "POST",
        cols: [[
            {field: 'rankNo', title: '排名', width: 100, align:'lift'},
            {field: 'name', title: '用户名称', width: 200, align:'lift'},
            {field: 'honorName', title: '段位名称', width: 200, align:'lift'},
            {field: 'companyName', title: '公司名称', width: 200, align:'lift'},
            {field: 'branchName', title: '部门', width: 200, align:'lift'},
            {field: 'allAch', title: '成就点数', width: 200, align:'lift', sort:true},
            {field: 'allExp', title: '经验值', width: 200, align:'lift', sort:true},
            {field: 'compositeScore', title: '综合分数', width: 200, align:'lift', sort:true}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    exports('cAllProvinceList', {});
});