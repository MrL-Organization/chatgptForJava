package com.mrl.controller;

import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.Result;
import com.mrl.service.QqRobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:45
 * @Description: com.mrl.controller-chatgpt
 * @Version: 1.0
 */
@RestController
@Slf4j
public class QqRobotController {

    @Resource
    private QqRobotService robotService;

    @PostMapping
    public Result QqRobotEven(HttpServletRequest request){
        log.info("接收到机器人的请求：{}", request.getContextPath());
        Result result = new Result();
        JSONObject response = robotService.QqRobotEvenHandle(request);
        String message = response.getString("message");
        if ("success".equals(message)) {
            result.successResult(response);
        }else {
            result.failResult(response);
        }
        log.info("机器人处理接口返回：{}",result);
        return result;
    }
}
