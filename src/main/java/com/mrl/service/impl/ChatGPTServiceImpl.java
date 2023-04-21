package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mrl.service.ChatGPTService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:47
 * @Description: com.mrl.service.impl-chatgpt
 * @Version: 1.0
 */
@Service
@Slf4j
public class ChatGPTServiceImpl implements ChatGPTService {
    @Value("${openAI.domain}")
    String domain;

    @Value("${openAI.key}")
    String key;

    @Value("${openAI.protocol}")
    String protocol;

    @Value("${openAI.model}")
    String model;

    public JSONObject sendToAI(String message) {
        log.debug("ChatGPTService.sendToAI开始,参数message:{}",message);
        JSONObject result = new JSONObject();
        //接口返回信息
        String response = "";
        //请求参数
        HashMap param = new HashMap<String,Object>();
        param.put("model",model);
        ArrayList messages = new ArrayList<Map>();
        HashMap messageMap = new HashMap<String,Object>();
        messageMap.put("role", "user");
        messageMap.put("content",message);
        messages.add(messageMap);
        param.put("messages",messages);
        /*String param = "{\n" +
                "    \"model\": \"" + model + "\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "!\"}]\n" +
                "  }";*/
        //请求头
        HashMap headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + key);
        //拼接URL
        String url = protocol + "://" + domain + "/v1/chat/completions";
        try {
            log.info("发送post请求地址:{}", url);
            log.info("发送post请求头:{}", headers);
            log.info("发送post请求参数:{}",param);
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("发送post返回信息:{}", response);
            result = JSON.parseObject(response);
        } catch (JSONException e) {
            log.error("发送post请求接口返回格式不是json格式:{}", response);
            result.put("message", e.getMessage());
        }catch (Exception e){
            log.error("发送请求失败:{}", e.getMessage());
            e.printStackTrace();
            result.put("message", e.getMessage());
        }
        log.debug("ChatGPTService.sendToAI结束");
        return result;
    }

    @Override
    public JSONObject queryBalance() {
        log.debug("ChatGPTService.queryBalance开始");
        JSONObject result = null;
        String response = "";
        try {
            //拼接URL
            String url = protocol + "://" + domain + "/pro/balance?apiKey=" + key;
            response = HttpUtils.sendGet(url);
            result = JSON.parseObject(response);
        } catch (JSONException e) {
            log.error("发送post请求接口返回格式不是json格式:{}", response);
        } catch (IOException e){
            log.error("查询余额请求失败！");
            e.printStackTrace();
        }
        log.debug("ChatGPTService.queryBalance结束");
        return result;
    }
}
