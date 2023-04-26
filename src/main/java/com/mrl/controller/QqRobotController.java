package com.mrl.controller;

import com.alibaba.fastjson.JSONObject;
import com.mrl.service.QqRobotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
    public HashMap<String,Object> QqRobotEven(HttpServletRequest request){

        log.debug("接收到机器人的请求：{}", request.getRequestURI());
        HashMap<String,Object> result = null;
        try {
            JSONObject jsonParam = this.getJSONParam(request);
            String post_type = jsonParam.getString("post_type");
            if ("message".equals(post_type)){
                //消息事件
                log.info("消息事件-接收参数：{}",jsonParam);
                result = robotService.QqRobotMessageHandle(jsonParam);
                log.info("消息事件-返回结果：{}",result);
            }else if ("request".equals(post_type)){
                //请求事件
                log.info("请求事件-接收参数：{}",jsonParam);
                result = robotService.QqRobotRequestHandle(jsonParam);
                log.info("请求事件-返回结果：{}",result);
            }
        } catch (Exception e) {
            log.error("QqRobotController出错：{}",e.getMessage());
            e.printStackTrace();
        }
        log.debug("机器人处理接口返回：{}",result);
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

    /**
     * 获取Request对象中的参数转为JSONObject
     */
    private JSONObject getJSONParam(HttpServletRequest request) {
        log.debug("QqRobotServiceImpl.getJSONParam开始");
        JSONObject jsonParam = null;
        try {
            // 获取输入流
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));

            // 数据写入String builder
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = streamReader.readLine()) != null) {
                sb.append(line);
            }
            jsonParam = JSONObject.parseObject(sb.toString());
            log.debug("request参数转为json：{}", jsonParam);
        } catch (Exception e) {
            log.error("request参数转为json出错：{}", e.getMessage());
        }
        log.debug("QqRobotServiceImpl.getJSONParam结束");
        return jsonParam;
    }
}
