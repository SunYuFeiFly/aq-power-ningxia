package com.wangchen.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 因为每日答题一天只能进行一次,所以这个表在用户获取每日答题的题目的时候就记录下来，这样的话就可以保证一天只玩一次（如果用户答了一半也就是5道题退出了，我们捕捉正常能捕捉到的退出情况，记录下来，如果是什么关机的话， 那就不记录了） 前端控制器
 * </p>
 *
 * @author zhangcheng
 * @since 2020-06-22
 */
@Controller
@RequestMapping("/userDayGameLog")
public class UserDayGameLogController {

}

