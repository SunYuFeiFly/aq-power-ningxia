layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/rank/selectPages",
        where: {"companyType":2,"type":2},
        method: "POST",
        cols: [[
            {field: 'rankNo', title: '排名', width: 100, sort: true},
            {field: 'companyName', title: '公司名称', width: 300},
            {field: 'count', title: '公司人数', width: 150, sort: true},
            // {field: 'compositeScore', title: '得分总和', width: 200},
            {field: 'compositeScore', title: '公司平均得分', width: 150, sort: true}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });


    exports('bCompanyList', {});
});