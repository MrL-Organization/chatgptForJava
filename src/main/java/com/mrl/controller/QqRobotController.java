package com.mrl.controller;

import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.Result;
import com.mrl.service.QqRobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
        log.info("接收到机器人的请求：{}", request.getRequestURI());
        Result result = new Result();
        try {
            robotService.QqRobotEvenHandle(request);
            result.successResult();
        } catch (Exception e) {
            log.error("QqRobotController出错：{}",e.getMessage());
            e.printStackTrace();
            result.failResult(e.getMessage());
        }
        log.info("机器人处理接口返回：{}",result);
        return result;
    }

    @RequestMapping("/testRobot")
    public void testRobot(@RequestParam("message") String message){
        log.info("测试与cqhttp的连通性：/testRobot");
        try {
            robotService.sendPrivateMsg(message,"");
        }catch (Exception e){
            log.error("测试与cqhttp的连通性出错：{}",e.getMessage());
            e.printStackTrace();
        }
    }
}
