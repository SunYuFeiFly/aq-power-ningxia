/**

 @Name：layuiAdmin（iframe版） 设置
 @Author：贤心
 @Site：http://www.layui.com/admin/
 @License: LPPL

 */

layui.define(['form', 'upload'], function(exports){
  var $ = layui.$
  ,layer = layui.layer
  ,form = layui.form;

  var $body = $('body');

  //网站设置
  form.on('submit(set_website)', function(obj){
    var setType = $(this).attr('data-t');
    var param = {
      "data":obj.field,
      "type":setType
    };
  var data = JSON.stringify(param);
    console.log(data);
    $.ajax({url:"gladmin/setting/save",method:"post",data:data,
      headers: {
        "Accept": "application/json; charset=utf-8",
        "Content-Type":"application/json; charset=utf-8"
      },success:function(res){
      console.log(res);
        if (res.code==="0000"){
          layer.msg('保存成功',{time:1000});
        }else{
          layer.msg(res.msg,{time:1000});
        }
      }});
    return true;
  });

  //设置密码
  form.on('submit(setmypass)', function(obj){

    $.ajax({url:"gladmin/doUpdatePassword",method:"post",data:obj.field,success:function(res){
        if (res.code==="0000"){
          layer.close(layer.index);
          layer.msg('修改成功,请重新登录',{time:1000});
          setInterval(function() {
            var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
            parent.layer.close(index); //再执行关闭
            parent.location.href="gladmin/login"
          }, 1000);
        }else if(res.code === "9996"){
          layer.msg('未登陆，请重新登陆',{time:1000});
          setInterval(function() {
            var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
            parent.layer.close(index); //再执行关闭
            parent.location.href="gladmin/login"
          }, 1000);
        }else {
          layer.close(layer.index);
          layer.msg(res.msg,{time:1000});
        }
      }});
    return false;
  });

  //对外暴露的接口
  exports('set', {});
});