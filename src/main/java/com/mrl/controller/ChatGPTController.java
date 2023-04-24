package com.mrl.controller;

import com.mrl.bean.Result;
import com.mrl.conf.ChatGPTServiceFactory;
import com.mrl.service.ChatGPTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

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
    ChatGPTServiceFactory chatGPTServiceFactory;
    //ChatGPTService chatGPTService;

    @RequestMapping("/text")
    public Result answerQuestion(@RequestParam("message") String message){
        log.info("/gpt/text接口入参：{}",message);
        Result result = new Result();
        String response = chatGPTServiceFactory.getChatGPTService().answerQuestion(message);
        HashMap<String,String> map = new HashMap<>();
        map.put("message",response);
        result.successResult(map);
        log.info("/gpt/text出参:{}",result);
        return result;
    }

    @RequestMapping("/img")
    public Result generatIMG(@RequestParam("message") String message){
        log.info("/gpt/img接口入参：{}",message);
        Result result = new Result();
        ArrayList<String> response = chatGPTServiceFactory.getChatGPTService().generatIMG(message);
        result.successResult(response);
        log.info("/gpt/img出参:{}",result);
        return result;
    }

    /*@RequestMapping("/chat")
    public Result chat(@RequestParam("message") String message){
        log.info("/gpt/send接口入参：{}",message);
        Result result = new Result();
        String response = ChatGPTService.chat(message);
        HashMap<String,String> map = new HashMap<>();
        map.put("message",response);
        result.successResult(map);
        log.info("/gpt/send出参:{}",result);
        return result;
    }*/

    @RequestMapping("/balance")
    public Result queryBalance(){
        log.info("/gpt/img接口");
        Result result = new Result();
        String response = chatGPTServiceFactory.getChatGPTService().queryBalance();
        HashMap<String,String> map = new HashMap<>();
        map.put("message",response);
        result.successResult(map);
        log.info("/gpt/img接口出参:{}",result);
        return result;
    }
}
