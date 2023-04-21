package com.mrl.controller;

import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.Result;
import com.mrl.service.ChatGPTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-19-16:00
 * @Description: com.mrl.controller-chatgpt
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/gpt")
public class ChatGPTController {

    @Resource
    ChatGPTService ChatGPTService;

    @RequestMapping("/send")
    public Result send(@RequestParam("message") String message){
        log.info("/gpt/send接口入参：{}",message);
        Result result = new Result();
        JSONObject response = ChatGPTService.sendToAI(message);
        if(response.containsKey("message")) {
            result.failResult(response);
        }else {
            result.successResult(response);
        }
        log.info("/gpt/send出参:{}",result);
        return result;
    }
}
