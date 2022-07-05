layui.define(['table', 'form', "common", "layedit"], function (exports) {
    var $ = layui.$,
        form = layui.form,
        table = layui.table,
        layedit = layui.layedit;

    table.render({
        id: 'LAY-user-manage',
        elem: '#LAY-user-manage',
        url: "system/rank/selectPagesThreeVsThree",
        method: "POST",
        cols: [[
            {field: 'rankNo', title: '排名', width: 100},
            {field: 'score', title: '分数', width: 100},
            {field: 'name1', title: '成员名称1', width: 200},
            {
                field: 'avatar1', title: '头像',sort: true, width: 150,  templet: function(d){
                    return '<div  class="show_img" ><img src="'+d.avatar1+'" alt="" width="50px" height="100px"></a></div>';
                }
            },
            // {field: 'level1', title: '  等级', width: 200},

            {field: 'name2', title: '成员名称2', width: 200},
            {
                field: 'avatar2', title: '头像',sort: true, width: 150,  templet: function(d){
                    return '<div  class="show_img" ><img src="'+d.avatar2+'" alt="" width="50px" height="100px"></a></div>';
                }
            },
            // {field: 'level2', title: '  等级', width: 200},

            {field: 'name3', title: '成员名称3', width: 200},
            {
                field: 'avatar3', title: '头像',sort: true, width: 150,  templet: function(d){
                    return '<div  class="show_img" ><img src="'+d.avatar3+'" alt="" width="50px" height="100px"></a></div>';
                }
            }
            // {field: 'level3', title: '  等级', width: 200}
        ]],
        page: true,
        limit: 10,
        skin: 'line'

    });

    exports('threeVsThreeList', {});
});