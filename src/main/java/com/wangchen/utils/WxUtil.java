package com.wangchen.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class WxUtil {

    // Web_access_tokenhttps请求地址
    private static String WEB_ACCESS_TOKENHTTPS = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 获取Web_access_tokenhttps请求地址
     * @param appId
     * @param appSecret
     * @param code
     * @Return: java.lang.String
     * @Auth: zhaowu
     * @Date: 2019/8/8 18:51
     */
    public static String getWebAccess(String appId, String appSecret, String code){
        return String.format(WEB_ACCESS_TOKENHTTPS, appId, appSecret, code);
    }

    /**
     * 获取客户端IP
     * @param request
     * @Return: java.lang.String
     * @Auth: zhaowu
     * @Date: 2019/8/8 18:53
     */
    public static String getIP(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 动态遍历获取所有收到的参数
     *
     * @param request
     * @Return: java.util.TreeMap<java.lang.String,java.lang.String>
     * @Auth: zhaowu
     * @Date: 2019/8/8 18:55
     */
    private TreeMap<String, String> getParams(HttpServletRequest request){
        TreeMap<String, String> map = new TreeMap<String, String>();
        Map reqMap = request.getParameterMap();
        for(Object key : reqMap.keySet()){
            String value = ((String[])reqMap.get(key))[0];
            System.out.println(key+";"+value);
            map.put(key.toString(),value);
        }
        return map;
    }

    /**
     * get请求
     * @param appId
     * @param appSecret
     * @param code
     * @Return: java.lang.String
     * @Auth: zhaowu
     * @Date: 2019/8/9 11:24
     */
    public static String doGet(String appId, String appSecret, String code) throws Exception {
        String url = getWebAccess(appId, appSecret, code);
        HttpClient client = HttpClientBuilder.create().build();//构建一个Client
        HttpGet get = new HttpGet(url);    //构建一个GET请求
        HttpResponse response = client.execute(get);//提交GET请求
        HttpEntity result = response.getEntity();//拿到返回的HttpResponse的"实体"
        String content = EntityUtils.toString(result);
        return content;
    }
}
