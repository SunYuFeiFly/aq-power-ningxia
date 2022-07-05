//package com.wangchen.config;/**
// * @ClassName
// * @author cheng.zhang
// * @company wangcheng
// * @create 2019-11-04 15:14
// * @Version 1.0
// */
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * CORSFilter
// * @auther cheng.zhang
// * @create 2019/11/4
// *
// */
//@Configuration
//@Component
//@Slf4j
//public class CORSFilter implements Filter {
//
//    public CORSFilter(){
//        log.info("SimpleCORSFilter init");
//    }
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        //变成http的
//        HttpServletResponse resp = (HttpServletResponse) response;
//        // 添加参数，允许任意domain访问
//        resp.setContentType("text/html;charset=UTF-8");
//        //禁用缓存，确保网页信息是最新数据
//        resp.setHeader("Pragma","No-cache");
//        resp.setHeader("Cache-Control","no-cache");
//        resp.setHeader("Access-Control-Allow-Origin", "*");
//        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, HEAD, DELETE, PUT");
//        resp.setHeader("Access-Control-Max-Age", "3600");
//        resp.setHeader("Access-Control-Allow-Headers",
//                "X-Requested-With, Content-Type, Authorization, Accept, Origin, User-Agent, Content-Range, Content-Disposition, Content-Description");
//
//        resp.setDateHeader("Expires", -10);
//        chain.doFilter(request, resp);
//
////        HttpServletRequest request = (HttpServletRequest) servletRequest;
////        HttpServletResponse response = (HttpServletResponse) servletResponse;
////
////        String clientOrigin = request.getHeader("origin");
////        response.addHeader("Access-Control-Allow-Origin", clientOrigin);
////        if(request.getRequestURI().contains("/findMarGrpPage")
////                ||request.getRequestURI().contains("/itemquery/list")
////                ||request.getRequestURI().contains("/getPersonalizedSetting")
////                ||request.getRequestURI().contains("/manage/single")
////                ||request.getRequestURI().contains("findAllExpInfo")){
////
////        }else {
////            response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT");
////            response.setHeader("Access-Control-Allow-Credentials", "true");
////            response.setHeader("Access-Control-Max-Age", "3600");
////            response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Origin, Authorization, X-Auth-Tkoen");
////            response.setHeader("Access-Control-Expose-Headers", "X-Auth-Token");
////        }
////
////        if(request.getMethod().equals("OPTIONS")){
////            response.setStatus(HttpServletResponse.SC_OK);
////        }else{
////            filterChain.doFilter(request, response);
////        }
//    }
//
//    public void destroy() {}
//}