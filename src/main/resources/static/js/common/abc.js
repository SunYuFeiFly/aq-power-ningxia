if (typeof jQuery === "undefined") {
    throw new Error("AntiFake's JavaScript requires jQuery")
}
//js工具类
var js = {
    Global: {TRUE: 1, FALSE: 0,enable:0,disable:1},
    index: 1,
    reqUrl: abcservice.baseurl + "abcService",
    table: null,
    table_filter: null,
    loading: function (msg, isclose) {
        index = layer.msg(msg || '数据加载中...', {icon: 16, shade: 0.01, time: (isclose || true) ? 3 : 0});
    }
    ,
    closeLoading: function () {
        layer.close(index || 1);
    }
    ,
    alert: function (message, options, closed) {
        if (typeof options != "object") {
            closed = options;
            options = {icon: 1}
        }

        if (!layer) {
            alert(message);
            if (typeof closed == "function") {
                closed()
            }
            return
        }
        layer.alert(message, options, function (index) {
            if (typeof closed == "function") {
                closed(index)
            }
            layer.close(index)
        })
    }
    ,
    msg: function (message) {
        if (!layer) {
            alert(message);
            return
        }
        layer.msg(message);
    },
    getSelectedRow: function () {
        var checkStatus = js.table.checkStatus(this.table_filter);
        var data = checkStatus.data[0];
        return data;
    },
    getSelectedRows: function () {
        var checkStatus = js.table.checkStatus(this.table_filter);
        var data = checkStatus.data;
        return data;
    },
    selectAll: function () {
        var isselected = $("#ck_selectall").prop("checked");
        $("[name='ckrow']").each(function () {
            $(this).prop("checked", isselected);
        });
    },
    initEnterpriseCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=enterpriseService&functionname=fetchEnterpriseList", "enterprise_name", "enterprise_code", defValue);
    },
    initOrginCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=enterpriseService&functionname=fetchOrginList", "enterprise_name", "enterprise_code", defValue);
    },
    initBrandCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=brandService&functionname=fetchBrandList", "brand_name", "brand_id", defValue);
    },
    initLabelCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=labelService&functionname=fetchLabelList", "name", "id", defValue);
    },
    initPackCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=packService&functionname=fetchPackList", "pack_name", "pack_id", defValue);
    },
    initDatasourceCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=datasourceService&functionname=datasourceCombox", "datasource_name", "datasource_id", defValue);
    },
    initScriptCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=scriptService&functionname=scriptCombox", "script_name", "script_id", defValue);
    },
    initNoticeTypeCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=noticeService&functionname=getNoticeTypeCombox", "note", "notice_type", defValue);
    },
    initTemplateCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=templateService&functionname=templateCombox", "template_name", "template_id", defValue);
    },
    initRuleCombox: function (ctlObj, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=regulationService&functionname=ruleCombox", "rule_name", "rule_id", defValue);
    },
    initProductCombox: function (ctlObj, brandId, defValue) {
        js.loadDataForCombox(ctlObj, abcservice.baseurl + "abcService?servicename=productService&functionname=fetchProductListByBrandId&brandId=" + brandId, "product_name", "product_id", defValue);
    },
    loadDataForCombox: function (ctlObj, reqUrl, display, value, defValue, excludeValue) {
    	var hostname= window.document.location.hostname;//获取域名

     
    	var Authorization= sessionStorage.getItem("access_token");

        var token = getQueryString("token");
        var jqxhr = $.ajax({
            url: reqUrl,
            type: 'POST',
            dataType: 'json',
            cache: false,
            async: true,
            global: false,
            timeout: 60000,
            headers: {'domain':hostname,'Authorization' : Authorization,"token":token},
            error: function (XMLHttpRequest, textStatus, errorThrown) {
            },
            success: function (data) {
                //console.log(data);
            	if(data.code=="401"){
            		 window.location.href=abcservice.loginPage;
            	return;	
            	}
                if (data && data.data && data.data.length > 0) {
                    innerhtml = "<option value=''>请选择</option>";
                    $(data.data).each(function (i, row) {
                        //console.log("def:" + excludeValue);
                        if (excludeValue && excludeValue != "" && row[value] == excludeValue) return;
                        if (defValue && defValue != "" && row[value] == defValue) {
                            //console.log(11);
                            innerhtml += "<option value='" + row[value] + "' selected>" + row[display] + "</option>";
                        } else {
                            //console.log(12);
                            innerhtml += "<option value='" + row[value] + "'>" + row[display] + "</option>";
                        }
                    });
                    $("#" + ctlObj).append(innerhtml);
                    if (layui && layui.form) {
                        layui.form.render('select');
                    }
                }
            }
        });
    }
    ,
    initDicLabelCombox: function (ctlObj, type, defaultValue) {
        var token = getQueryString("token");
        js.loadDataForCombox(ctlObj,  abcservice.gatewayurl + "/comminner-service/fetchDic?dictType=" + type  , "info_name", "info_code", defaultValue);
    }
    ,
    getDictLabel: function (type, value, defaultValue) {
        var val = sessionStorage.getItem(type + "#" + value);
        if (val != null) {
            return val;
        }
    	var hostname= window.document.location.hostname;//获取域名
     
    	 var tmpurl =  abcservice.gatewayurl+"/comminner-service/getDicLable";
    	   	var Authorization= sessionStorage.getItem("access_token");
    	      
            var token = getQueryString("token");
        var result = defaultValue;
        var timestr = (new Date()).getTime();
        var param = {  "dictType": type, "value": value};
        var md5str = timestr + obj_md5(param);
        var jqxhr = $.ajax({
            url: tmpurl,
            type: 'get',
            dataType: 'json',
            data:param,
            cache: false,
            async: false,
            global: false,
            timeout: 60000,
            headers: {'domain':hostname,'Authorization' : Authorization,"token":token},
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                result = defaultValue;
            },
            success: function (data) {
                //console.log(data);
                if (data && data.data && data.data["info_name"]) {
                    result = data.data["info_name"]
                }
            }
        });

        if (!result || result == "") result = defaultValue;
        sessionStorage.setItem(type + "#" + value, result);
        return result;
    }
    ,
    serializeObject: function (fromObj) {
        var o = {};
        var a = $("#" + fromObj).serializeArray();
        $.each(a, function () {
            if (o[this.name] !== undefined) {
                if (!o[this.name].push) {
                    o[this.name.toLowerCase()] = [o[this.name]];
                }
                o[this.name.toLowerCase()].push(this.value || '');
            } else {
                o[this.name.toLowerCase()] = this.value || '';
            }
        });
        return o;
    }
    ,
    initForm: function (fromObj, fromDatas) {
        var form = $("#" + fromObj);
        var jsonValue = fromDatas;
        //如果传入的json字符串，将转为json对象
        if ($.type(fromDatas) === "string") {
            jsonValue = $.parseJSON(fromDatas);
        }
        //如果传入的json对象为空，则不做任何操作
        if (!$.isEmptyObject(jsonValue)) {
            $.each(jsonValue, function (key, value) {
                var formField = form.find("[name='" + key.toLowerCase() + "']");
                if ($.type(formField[0]) != "undefined") {
                    var fieldTagName = formField[0].tagName.toLowerCase();
                    if (fieldTagName == "input") {
                        if (formField.attr("type") == "radio") {
                            $("input:radio[name='" + key.toLowerCase() + "'][value='" + value + "']").attr("checked", "checked");
                        } else {
                            formField.val(value);
                        }
                    } else if (fieldTagName == "select") {
                        formField.val(value);
                    } else if (fieldTagName == "textarea") {
                        formField.val(value);
                    } else {
                        formField.val(value);
                    }

                }
            });
        }
        return form;
    }
    ,
    initExtendElement: function (ctlObj, tableType) {

    },
    paramSerialize: function (a) {
        var s = [];

        function add(key, value) {
            s[s.length] = encodeURIComponent(key) + '=' + encodeURIComponent(value);
        };

        //验证参数是否为数组或对象
        if ($.isArray(a) || a.jquery) {
            $.each(a, function () {
                add(this.name, this.value);
            });
        } else {
            for (var j in a) {
                if ($.isArray(a[j])) {
                    jQuery.each(a[j], function () {
                        add(j, this);
                    });
                } else {
                    add(j, $.isFunction(a[j]) ? a[j]() : a[j]);
                }
            }
        }
        return s.join("&").replace(/%20/g, "+");
    },
    gridTable: function (ctlObj, cols, baseUrl, servicename, functionname, param, ispage) {
        if (ispage == null) ispage = true;
        this.table = layui.table;
    	 var tmpurl =  abcservice.gatewayurl+"/"+servicename+"/"+functionname;
    		var hostname= window.document.location.hostname;//获取域名

    	var Authorization= sessionStorage.getItem("access_token");
 
 
       
        var token = getQueryString("token");
        this.table.render({
            elem: '#' + ctlObj,
            url: tmpurl + '?' + js.paramSerialize(param),
            cols: [cols],
            page: ispage,
            response: {
                statusName: 'code', //规定数据状态的字段名称，默认：code
                statusCode: 200, //规定成功的状态码，默认：0
                msgName: 'message', //规定状态信息的字段名称，默认：msg
                dataName: 'data' //规定数据列表的字段名称，默认：data
            },
            request: {
                pageName: 'pageindex', //页码的参数名称，默认：page
                limitName: 'pagesize' //每页数据量的参数名，默认：limit
                	 
            }, headers: {'domain':hostname,'Authorization' : Authorization,"token":token},
            parseData: function (ores) { //将原始数据解析成 table 组件所规定的数据
            	var res=ores;
            	if(ores.data!=null&&ores.data.data!=null){
            		res=ores.data;
            		res.code=ores.code;
            		res.message=ores.message;
            	}
                if (res.data != null   && res. data["RecordCount"] != null) {
                    return {
                        "code": res.code, //解析接口状态
                        "msg": res.message, //解析提示文本
                        "count": res.data.RecordCount, //解析数据长度
                        "limit": res.data.pageSize,
                        "data": res.data.rs //解析数据列表
                    };
                } else if (res. data != null && res. data["count"] != null) {
                    return {
                        "code": res.code, //解析接口状态
                        "msg": res.message, //解析提示文本
                        "count": res.data.count, //解析数据长度
                        "limit": res.data.count,
                        "data": res.data.list //解析数据列表
                    };
                } else {
                    return {
                        "code": res.code, //解析接口状态
                        "msg": res.message, //解析提示文本
                        "count": res.data.data != null ? res.data.data.length : 0, //解析数据长度
                        "limit": res.data.data != null ? res.data.data.length : 0,
                        "data": res.data .data!= null ? res.data.data : [] //解析数据列表
                    };
                }
            },
            done: function () {
                js.refreshBtn();
            }
        });

        //定义表格容器编号
        this.table_filter = ctlObj;
        if (this.table.initchkbox == null || this.table.initchkbox[this.table_filter] != true) {
            if (this.table.initchkbox == null) {
                this.table.initchkbox = {};
            }
            this.table.initchkbox[this.table_filter] = true;
            this.table.on('checkbox(' + this.table_filter + ')', function (obj) {
                var index = $(obj.tr).attr('data-index');
                console.log(index);

                //    var tableBox = $(this).parents('.layui-table-box');
                //    //存在固定列
                //    if (tableBox.find(".layui-table-fixed.layui-table-fixed-l").length > 0) {
                //        tableDiv = tableBox.find(".layui-table-fixed.layui-table-fixed-l");
                //    } else {
                //        tableDiv = tableBox.find(".layui-table-body.layui-table-main");
                //    }
                //    var checkCell = tableDiv.find("tr[data-index=" + index + "]").find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
                //    if (checkCell && checkCell.length > 0) {
                // //var cc=$(checkCell).parent();
                //       $(checkCell).parent().parent().click();
                //    }
                js.refreshBtn();
            });
        }
        if (!this.table.initevent) {
            this.table.initevent = true;
            //监听选中事件

            /*this.table.on('row(' + this.table_filter + ')', function (obj) {
                // console.log(obj); //得到当前行元素对象
                //console.log(obj.data) //得到当前行数据

                var index = $(obj.tr).attr('data-index');
                // console.log(index);

                var tableBox = $(this).parents('.layui-table-box');
                //存在固定列
                if (tableBox.find(".layui-table-fixed.layui-table-fixed-l").length > 0) {
                    tableDiv = tableBox.find(".layui-table-fixed.layui-table-fixed-l");
                } else {
                    tableDiv = tableBox.find(".layui-table-body.layui-table-main");
                }
                var checkCell = tableDiv.find("tr[data-index=" + index + "]").find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
                if (checkCell && checkCell.length > 0 ) {

                    $(checkCell).parent().click();
                }
                js.refreshBtn();
            });*/
            $(document).on("click", ".layui-table-body table.layui-table tbody tr", function () {
                var obj = event ? event.target : event.srcElement;
                var tag = obj.tagName;
                var checkbox = $(this).find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
                if (checkbox.length != 0) {
                    if (tag == 'DIV') {
                        checkbox.click();
                    }
                }
                js.refreshBtn();
            });

            $(document).on("click", "td div.laytable-cell-checkbox div.layui-form-checkbox", function (e) {
                e.stopPropagation();
            });
        }
        js.refreshBtn();
        return this.table;
    },
    gridReload: function () {
        this.table.reload(this.table_filter);
    },
    refreshBtn: function () { //刷新按钮状态
        //根据记录刷新按钮状态
        var str = this.getSelectedRows();
        //console.log(str);

        if (!str || str.length <= 0) {
            $(".needselectrow").attr("disabled", "disabled");
            $(".selectonerow").attr("disabled", "disabled");
        } else if (str.length > 1) {
            $(".selectonerow").attr("disabled", "disabled");
            $(".needselectrow").removeAttr("disabled");
        } else {
            $(".selectonerow").removeAttr("disabled");
            var state = str[0].state;
            //console.log("state:" + state);
            if (state ==js.Global.enable) {
                $("#btn_enabled").html("<i class='glyphicon glyphicon-pause'></i>禁用");
            } else {
                $("#btn_enabled").html("<i class='glyphicon glyphicon-play'></i>启用");
            }
            $(".needselectrow").removeAttr("disabled");
        }
    },
    formatDate: function (d, format) {
        var date = new Date(d || new Date());
        var o = {
            "M+": date.getMonth() + 1, //月份
            "d+": date.getDate(), //日
            "h+": date.getHours(), //小时
            "m+": date.getMinutes(), //分
            "s+": date.getSeconds(), //秒
            "q+": Math.floor((date.getMonth() + 3) / 3), //季度
            "S": date.getMilliseconds() //毫秒
        };

        format = format || 'yyyy-MM-dd HH:mm:ss';

        if (/(y+)/.test(format)) format = format.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(format))
                format = format.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return format;
    },
    uploadFile: function (objCtrl, btnTxt, uploadStyle, defaultValue, option,notupload) {
        btnTxt = btnTxt || '文件上传';
        uploadStyle = uploadStyle || 'file';
        var opts = {ext: '*', size: 100};//限制文件大小，单位 KB
        if (option)
            $.extend(true, opts, option);

        var validatetip = $("#" + objCtrl).attr("validate-tip");//提示语
        var validatetype = $("#" + objCtrl).attr("validate-type");//验证类型
        var emptyvalue = $("#" + objCtrl).attr("emptyvalue");//空值

        baseUrl = abcservice.baseurl;
        srcValue = defaultValue || '';
        if (srcValue.indexOf("http://") < 0 && srcValue.indexOf("https://") < 0)
            defaultValue = baseUrl + 'upload/' + (defaultValue || 'nopic.png');

        var html = '';
        if (uploadStyle == 'file') {
            html = '<input type="text" data-accept="file" data-accept-mime="*" id="txt' + objCtrl + '" name="' + objCtrl + '"' +
                'class="abc-input-file" placeholder="只能上传jpg/png格式文件，文件大小不能超过500kb"/>' +
                '<input id="hid' + objCtrl + '" name="' + objCtrl + '" type="hidden" value="' + srcValue + '" validate-tip="'
                + validatetip + '" validate-type="' + validatetype + '" emptyvalue="' + emptyvalue + '"/>' +
                '<button type="button" id="btn' + objCtrl + '" data-input-id="' + objCtrl + '" class="layui-btn">' +
                '<i class="layui-icon">&#xe67c;</i>' + btnTxt + '</button>';
        } else {
            html = '<button type="button" class="layui-btn" id="btn' + objCtrl + '"><i class="layui-icon">&#xe67c;</i>' + btnTxt + '</button>' +
                '<input id="hid' + objCtrl + '" name="' + objCtrl + '" type="hidden" value="' + srcValue + '" validate-tip="'
                + validatetip + '" validate-type="' + validatetype + '" emptyvalue="' + emptyvalue + '"/><div class="layui-upload-list">' +
                '<img class="layui-upload-img" id="img' + objCtrl + '" src="' + defaultValue + '" style="width: 115px;height: 115px;"/><p id="msg' + objCtrl + '">'+((notupload ||option.size==null)?'':'文件不能超过'+option.size+'kb')+'</p></div>';
        }
        $("#" + objCtrl).append(html);
        $("#" + objCtrl).append('<div id="prv' + objCtrl + '" style="display:none;" class="hide"><img id="prvImg' + objCtrl + '" src="' + defaultValue + '"></div>');
if(notupload){
	$("#btn" + objCtrl ).hide();
	return;
}
        var accepttype = "file";
        var acceptMime = "*";
        layui.use('upload', function () {
            var upload = layui.upload;

            $("#img" + objCtrl).on("click", function () {
                layer.open({
                    type: 1,
                    title: false,
                    closeBtn: true,
                    skin: 'layui-layer-nobg', //没有背景色
                    shadeClose: true,
                    content: $('#prv' + objCtrl),
                    end: function () {
                        $("#prv" + objCtrl).hide();
                    }
                });
            });

            //执行实例
            var uploadInst = upload.render({
                elem: '#btn' + objCtrl //绑定元素
                , url: baseUrl + '/uploadclass/'
                , filed: "docfile"
                , accept: accepttype
                //, acceptMime: acceptMime
                , exts: opts.ext
                , size: opts.size
                , before: function (obj) {
                    js.loading("图片上传中...", false);
                    if (uploadStyle == 'image') {
                        //预读本地文件示例，不支持ie8
                        obj.preview(function (index, file, result) {
                            $('#img').attr('src', result); //图片链接（base64）
                        });
                    }
                }
                , done: function (res) {
                    //上传完毕回调
                    if (uploadStyle == 'file') {
                        $("#txt" + objCtrl).val(res.filename);
                        if (res.url && res.url != "") {
                            $("#hid" + objCtrl).val(res.url);
                        } else {
                            $("#hid" + objCtrl).val(res.filename);
                        }
                    } else {
                        if (res.url && res.url != "") {
                            $("#img" + objCtrl).attr("src", res.url);
                            $("#prvImg" + objCtrl).attr("src", res.url);
                            $("#hid" + objCtrl).val(res.url);
                        } else {
                            $("#img" + objCtrl).attr("src", baseUrl + 'upload/' + res.filename);
                            $("#prvImg" + objCtrl).attr("src", baseUrl + 'upload/' + res.filename);
                            $("#hid" + objCtrl).val(res.filename);
                        }
                    }

                    js.closeLoading();
                }, error: function () {
                    //请求异常回调
                    if (uploadStyle == 'image') {
                        var msgTxt = $('#msg' + objCtrl);
                        msgTxt.html('<span style="color: #FF5722;">上传失败</span> <a class="layui-btn layui-btn-xs img-reload">重试</a>');
                        msgTxt.find('.img-reload').on('click', function () {
                            uploadInst.upload();
                        });
                    }

                    js.closeLoading();
                }
            });
        });
    },
    trim: function (value) {
        value = value + " ";
        return value.replace(/(^\s*)|(\s*$)/g, "");
    },
    //表单数据验证
    validateFrm: function (frmCtrl) {
        return this.validateFun(frmCtrl);
    },
    //Dom数据验证
    validateObjs: function (objs) {
        return this.validateFun(null, objs);
    },
    //验证函数
    validateFun: function (frmCtrl, objs) {
        var frm = document.getElementById(frmCtrl);
        var formElements = objs || frm.elements;
        var baseJs = parent.js || js;

        for (var i = 0; i < formElements.length; i++) {
            if (formElements[i].type == "text" && formElements[i].value != "") {
                formElements[i].value = this.trim(formElements[i].value);
            }

            //获取控件属性
            var PromptName = formElements[i].getAttribute('validate-tip');//提示语
            var maxlength = formElements[i].getAttribute('validate-length');//数据长度
            var decimalDigits = formElements[i].getAttribute('validate-numericlength');//数字小数位
            var ValidateType = formElements[i].getAttribute('validate-type');//验证类型
            var emptyValue = formElements[i].getAttribute('emptyvalue');//空值
            var expression = formElements[i].getAttribute('validate-expression');//正则表达式
            if(PromptName==null||PromptName=="undefined"){
            	PromptName="";
            }
            if (ValidateType == null || typeof (ValidateType) == "undefined") continue;
            if (maxlength != null && typeof (maxlength) != "undefined") {
                if (!this.limitLength(formElements[i]))
                    return false;
            }

            if (ValidateType.toLowerCase() != "novalidate") {
                var validateTypes = ValidateType.split("|");
                for (var j = 0; j < validateTypes.length; j++) {
                    if (validateTypes[j].toLowerCase() == "required") {
                        if ((emptyValue == null && formElements[i].value.search(/^[\s　]*$/ig) >= 0)
                            || (emptyValue != null && formElements[i].value == emptyValue)) {
                            try {
                                formElements[i].focus();
                            } catch (e) {
                            }
                            formElements[i].value = "";

                            baseJs.alert("“" + PromptName + "”不能为空！");
                            return false;
                        }
                    } else if (emptyValue == null && formElements[i].value.search(/^[\s　]*$/ig) < 0 || emptyValue != null && formElements[i].value != emptyValue) {
                        switch (validateTypes[j].toLowerCase()) {
                            case 'number':
                                var tempDigits = 0;
                                if (decimalDigits != null || typeof (decimalDigits) != "undefined" && parseInt(decimalDigits) > 0) {
                                    tempDigits = decimalDigits;
                                }
                                eval("var reg =/^[-\\+]?\\d+\\.?\\d{0," + tempDigits.toString() + "}$/gi");
                                if (formElements[i].value.search(reg) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的数字，此处需输入小数位小于或等于" + tempDigits + "位的数字！");
                                    return false;
                                }
                                break;
                            case 'int':
                                if (formElements[i].value.search(/^[-\+]?\d+$/ig) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的整数！");
                                    return false;
                                }
                                break;
                            case 'nonnegativeint':
                                if (formElements[i].value.search(/^[\+]?\d+$/ig) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”必须是大于或等于0的整数！");
                                    return false;
                                }
                                break;
                            case 'email':
                                if (formElements[i].value.search(/([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{1,4}|[0-9]{1,3})(\]?)/ig) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的E-mail！");
                                    return false;
                                }
                                break;
                            case 'mobile'://手机号
                                if (formElements[i].value.search(/^0{0,1}(1[0-9][0-9])[0-9]{8}$/ig) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的手机号码！");
                                    return false;
                                }
                                break;
                            case 'regular'://正则表达式
                                eval("var reg1 =/^" + expression + "$/gi");
                                if (formElements[i].value.search(reg1) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert(PromptName);
                                    return false;
                                }
                                break;
                            case 'commonword'://特殊字符
                                if (this.trim(formElements[i].value).search(/['\"\\\/|；;,，～！!\*\?&\+<>。.%$#@￥]/ig) >= 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不能包含如下特殊字符：\n\n'  \"  \\  \/  |  ；  ;  ,  ，  ～  ！  !  \*  \?  &  \+ < > 。 .  %  $  #  @  ￥");
                                    return false;
                                }
                                break;
                            case 'date'://日期
                                if (this.trim(formElements[i].value).search(/^((\d{2}(([02468][048])|([13579][26]))[\-\/\s]((((0?[13578])|(1[02]))[\-\/\s]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\-\/\s]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\-\/\s]((0?[1-9])|([1-2][0-9])))))|(\d{2}(([02468][1235679])|([13579][01345789]))[\-\/\s]((((0?[13578])|(1[02]))[\-\/\s]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\-\/\s]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\-\/\s]((0?[1-9])|(1[0-9])|(2[0-8]))))))?$/gi) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的日期格式！\n日期格式例“2009 03 21”、“2009-03-21”、“2009\/03\/21”");
                                    return false;
                                }
                                break;
                            case 'datetime'://日期时间
                                if (this.trim(formElements[i].value).search(/^((\d{2}(([02468][048])|([13579][26]))[\-\/\s]((((0?[13578])|(1[02]))[\-\/\s]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\-\/\s]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\-\/\s]((0?[1-9])|([1-2][0-9])))))|(\d{2}(([02468][1235679])|([13579][01345789]))[\-\/\s]((((0?[13578])|(1[02]))[\-\/\s]((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\-\/\s]((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\-\/\s]((0?[1-9])|(1[0-9])|(2[0-8]))))))(\s(((0?[0-9])|(1[0-9])|(2[0-3])):([0-5]?[0-9])((\s)|(:([0-5]?[0-9])))))?$/gi) < 0) {
                                    try {
                                        formElements[i].focus();
                                    } catch (e) {
                                    }
                                    baseJs.alert("“" + PromptName + "”不是有效的日期时间格式！\n日期时间格式例“2009 03 21 23:59:59”、“2009-03-21 23:59:59”、“2009\/03\/21 23:59:59”");
                                    return false;
                                }
                                break;
                            case 'idcard'://身份证
                                if (!this.validateIdCard(this.trim(formElements[i].value))) {
                                    baseJs.alert("“" + PromptName + "”不是有效！");
                                    return false;
                                }
                                break;
                            case 'ip'://IP地址验证
                                if (!this.validateIP(this.trim(formElements[i].value))) {
                                    baseJs.alert("“" + PromptName + "”不是有效！");
                                    return false;
                                }
                                break;
                            case 'creditcode'://验证社会统一
                                if (!this.validateCreditCode(this.trim(formElements[i].value))) {
                                    baseJs.alert("“" + PromptName + "”不是有效！");
                                    return false;
                                }
                                break;
                        } // switch结束
                    }
                }
            } // if结束
        } // for　结束
        return true;
    },
    // 如果输入的字符串超长，则自动截取（1个汉字算两个字符）
    limitLength: function (varField) {
        var PromptName = varField.getAttribute('validate-tip');
        var maxlength = varField.getAttribute('validate-length');
        var baselayer = parent.layer || layer;
        var result = true;

        if (varField == null) return -1;
        if (varField.value.length > maxlength) {
            baselayer.confirm("“" + PromptName + "”已超过最大长度{" + maxlength + "}，是否需要系统自动截取？", {
                btn: ['是', '否'] //按钮
            }, function (index) {
                varField.value = varField.value.substring(0, maxlength);
                baselayer.close(index);
            });
            return false;
        }

        return true;
    },
    //验证IP地址
    validateIP: function (value) {
        var exp = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
        var reg = value.match(exp);
        if (reg == null) {
            return false;
        } else {
            return true;
        }
    },
    //验证身份证号
    validateIdCard: function (idcode) {
        idcode = idcode.toUpperCase();
        var weight_factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
        var check_code = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
        var code = idcode + "";
        var last = idcode[17];
        var seventeen = code.substring(0, 17);
        var arr = seventeen.split("");
        var len = arr.length;
        var num = 0;
        for (var i = 0; i < len; i++) {
            num = num + arr[i] * weight_factor[i];
        }
        var resisue = num % 11;
        var last_no = check_code[resisue];
        var idcard_patter = /^[1-9][0-9]{5}([1][9][0-9]{2}|[2][0][0|1][0-9])([0][1-9]|[1][0|1|2])([0][1-9]|[1|2][0-9]|[3][0|1])[0-9]{3}([0-9]|[X])$/;
        var format = idcard_patter.test(idcode);
        return last === last_no && format ? true : false;
    },
    //验证社会信用代码
    validateCreditCode: function (Code) {
        var patrn = /^[0-9A-Z]+$/;
        if ((Code.length != 18) || (patrn.test(Code) == false)) {
            return false;
        } else {
            var Ancode;
            var Ancodevalue;
            var total = 0;
            var weightedfactors = [1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28];//加权因子
            var str = '0123456789ABCDEFGHJKLMNPQRTUWXY';
            for (var i = 0; i < Code.length - 1; i++) {
                Ancode = Code.substring(i, i + 1);
                Ancodevalue = str.indexOf(Ancode);
                total = total + Ancodevalue * weightedfactors[i];
            }
            var logiccheckcode = 31 - total % 31;
            if (logiccheckcode == 31) {
                logiccheckcode = 0;
            }
            var Str = "0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,J,K,L,M,N,P,Q,R,T,U,W,X,Y";
            var Array_Str = Str.split(',');
            logiccheckcode = Array_Str[logiccheckcode];
            var checkcode = Code.substring(17, 18);
            if (logiccheckcode != checkcode) {
                return false;
            }
        }
        return true;
    },    //验证IP地址
    validateMail: function (value) {
        var exp = /^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{1,4}|[0-9]{1,3})(\]?)$/;
        var reg = value.match(exp);
        if (reg == null) {
            return false;
        } else {
            return true;
        }
    }
};
