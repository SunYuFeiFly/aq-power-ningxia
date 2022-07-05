layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;
    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/user/selectUserHasAchievementList",
        method: "POST",
        cols: [[
            // {field: 'openId', title: 'openId', width: 0},
            {field: 'achievementName', title: '成就名称', width: 200},
            {field: 'createTime', title: '获取时间', width: 200}
        ]],
        where:{'openId': $("#openId").val()},
        page: true,
        limit: 10,
        skin: 'line'

    });


    exports('memberhasachievement', {});
});