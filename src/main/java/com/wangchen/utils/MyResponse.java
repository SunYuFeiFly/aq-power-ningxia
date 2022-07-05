package com.wangchen.utils;


import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.fasterxml.jackson.annotation.JsonInclude;

//import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义返回类
 *
 * @author liuan
 */
//@ApiModel(description = "自定义返回类")
@Getter
@Setter
@Component
public class MyResponse<T> {
    private int state ;//0失败;1成功;
    private String classname ;
    private String message;
    private Instant time;
    private int code ;//200 正常  401 neelogin
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    public MyResponse() {
    	code=200;
    	state=1;
    }
    /**
     * @param _state
     * @param _classname
     * @param msg 
     */
    public MyResponse(int _state, String _classname, String msg, T data) {
        this.time = ZonedDateTime.now().toInstant();
        state = _state;
        code=200;
        classname = _classname;
        message = msg;
        this.data = data;
    }

    /**
     * @return
     */
    public static MyResponse<Map<String,Integer>> success() {
    	 Map<String,Integer> map=new HashMap<String,Integer>();
    	map.put("code", 1);
        return new MyResponse<Map<String,Integer>>(1, "Response", "成功", map);
    }

    /**
     * 成功返回
     *
     * @return 成功时data.code=1
     */
    public static MyResponse<Map<String,Integer>> success(String _classname, String msg) {
    	 Map<String,Integer> map=new HashMap<String,Integer>();
     	map.put("code", 1);
        return new MyResponse<Map<String,Integer>>(1, _classname, msg, map);
    }

    /**
     * 成功返回
     *
     * @return  成功时data.code=1
     */
    public static MyResponse<Map<String,Integer>>  success(String msg) {
    	 Map<String,Integer> map=new HashMap<String,Integer>();
      	map.put("code", 1);
        return new MyResponse<Map<String,Integer>> (1, "Response", msg, map);
    }

    /**
     * 成功返回
     *
     * @return
     */
    public static MyResponse success(Object data) {
    	if(data==null) {
    	  	 Map<String,Integer> map=new HashMap<String,Integer>();
    	      	map.put("code", 1);
    		   return new MyResponse<>(1, "", "成功", map);
    	}
        return new MyResponse<>(1, data.getClass().getName(), "成功", data);
    }

    /**
     * 失败返回
     *
     * @param _classname
     * @param msg
     * @return  失败时data.code=0
     */
    public static MyResponse Faild(String _classname, String msg) {
     	 Map<String,String> map=new HashMap<String,String>();
     	map.put("code", "0");
      	map.put("message", msg);
        return new MyResponse<>(0, _classname, msg, map);
    }

    /**
     * 失败返回
     *
     * @param
     * @param msg
     * @return  失败时data.code=0
     */
    public static MyResponse Faild(String msg) {
     	 Map<String,String> map=new HashMap<String,String>();
	      	map.put("code", "0");
	      	map.put("message", msg);
        return new MyResponse<>(0, "Response", msg, map);
    }
//    @JsonIgnore
//    public JSONObject getJsonObjectData() {
//    	return JSONObject.parseObject(JSONObject.toJSONString(data));
//    }
//    @JsonIgnore
//    public JSONArray getJsonArrayData() {
//    	return JSONArray.parseArray(JSONObject.toJSONString(data));
//    }
//    @JsonIgnore
//    public int getIntData() {
//    	if(data==null||!NumberUtils.isNumber(data.toString())) {
//    		return -999999;
//    	}
//    	return Integer.parseInt(data.toString());
//    	 
//    }
//    @JsonIgnore
//    public String toString() {
//    	  String[] excludeProperties = {"intData","getIntData", "getJsonArrayData","jsonArrayData", "getJsonObjectData","jsonObjectData"};
//           
//          PropertyPreFilters filters = new PropertyPreFilters();
//          PropertyPreFilters.MySimplePropertyPreFilter excludefilter = filters.addFilter();
//          excludefilter.addExcludes(excludeProperties);
//    	return JSONObject.toJSONString(this,excludefilter);
//    }
}
