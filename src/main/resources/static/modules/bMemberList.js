layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;
    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/user/selectUserList",
        where: {"companyType":2},
        method: "POST",
        cols: [[
            {field: 'openId', title: 'openId', width: 280},
            {field: 'companyName', title: '单位', width: 150},
            {field: 'name', title: '用户名称', width: 120},
            {field: 'experienceNum', title: '经验值', width: 100, sort:true},
            {field: 'coinNum', title: '塔币', width: 100, sort:true},
            {field: 'achievementNum', title: '成就', width: 100, sort:true},
            {
                field: 'signNum', title: '是否签到', width: 100, templet: function (d) {
                    var msg = "";
                    if (d.signNum == 0) {
                        msg = "未签到";
                    } else if (d.signNum == 1) {
                        msg = "已签到";
                    }
                    return msg;
                }
            },
            {field: 'dayGameNum', title: '每日答题总次数', width: 150, sort:true},
            {field: 'dayGameTrueNum', title: '每日答题正确题数', width: 180, sort:true},
            {field: 'dayGameTrueRate', title: '每日答题总正确率', width: 180, sort:true},
            {field: 'oneVsOneNum', title: '个人赛次数', width: 120, sort:true},
            {field: 'oneVsOneTrueNum', title: '个人赛正确题数', width: 150, sort:true},
            {field: 'oneVsOneTrueRate', title: '个人赛正确率', width: 150, sort:true},
            {field: 'teamVsTeamNum', title: '团队赛次数', width: 120, sort:true},
            {field: 'teamVsTeamTrueNum', title: '团队赛正确题数', width: 180, sort:true},
            {field: 'teamVsTeamTrueRate', title: '团队赛正确率', width: 180, sort:true},
            {field: 'allAnswerNum', title: '完成总题目数', width: 180, sort:true},
            {field: 'allAnswerTrueRate', title: '总正确率', width: 180, sort:true},
            {title: '所获得的成就', width: 200, toolbar: '#table-useradmin-webuser'}
        ]],
        where:{'id': $("#id").val()},
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
                , area: ['650px', '300px']
                , maxmin: true
                , title: "拥有成就"
                , content: 'system/user/editAchievement?openId=' + data.openId
            });
        }
    });

    exports('bMemberList', {});
});