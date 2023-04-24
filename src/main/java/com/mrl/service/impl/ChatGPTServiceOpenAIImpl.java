package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.Message;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.ChatGPTService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:47
 * @Description: com.mrl.service.impl-chatgpt
 * @Version: 1.0
 */
@Service
@Slf4j
public class ChatGPTServiceOpenAIImpl implements ChatGPTService {
    //配置文件类
    @Resource
    ConfigurationClass configurationClass;

    public String answerQuestion(String prompt) {
        log.debug("ChatGPTService.answerQuestion开始,参数message:{}",prompt);
        //返回结果
        String result = null;
        //接口响应信息
        String response = null;
        //请求地址
        String url = configurationClass.OPENAI_PROTOCOL + "://"
                + configurationClass.OPENAI_DOMAIN + "/v1/completions";
        log.info("post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.OPENAI_KEY);
        log.info("post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("model", configurationClass.OPENAI_TEXT_MODEL);
        param.put("prompt",prompt);
        if(!"".equals(configurationClass.OPENAI_TEXT_MAX_TOKENS)){
            param.put("max_tokens",Integer.parseInt(configurationClass.OPENAI_TEXT_MAX_TOKENS));
        }
        log.info("post请求参数:{}",param);
        try {
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("post请求相应信息:{}", response);
            result = ((JSONObject)JSON.parseObject(response)
                    .getJSONArray("choices").get(0)).getString("text");
        }catch (Exception e){
            log.error("与openai服务连接异常：{}",e.getMessage());
            result = "与openai服务连接异常：" + e.getMessage();
            e.printStackTrace();
        }
        log.debug("ChatGPTService.answerQuestion结束");
        return result;
    }

    @Override
    public ArrayList<String> generatIMG(String prompt) {
        log.debug("ChatGPTService.generatIMG开始,参数message:{}",prompt);
        //返回结果
        ArrayList<String> result = new ArrayList<>();
        //接口响应信息
        String response = null;
        //请求地址
        String url = configurationClass.OPENAI_PROTOCOL + "://"
                + configurationClass.OPENAI_DOMAIN + "/v1/images/generations";
        log.info("post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.OPENAI_KEY);
        log.info("post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<String,Object>();
        Integer.parseInt(configurationClass.OPENAI_IMG_NUM);
        param.put("n",configurationClass.OPENAI_IMG_NUM);
        if (!"".equals(configurationClass.OPENAI_IMG_SIZE)){
            param.put("size",Integer.parseInt(configurationClass.OPENAI_IMG_SIZE));

        }
        if (!"".equals(configurationClass.OPENAI_IMG_FORMAT)){
            param.put("response_format",configurationClass.OPENAI_IMG_FORMAT);

        }
        param.put("prompt",prompt);
        log.info("post请求参数:{}",param);
        try {
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("post请求响应信息:{}", response.substring(0,400));
            JSONObject responseJson = JSON.parseObject(response);
            JSONArray jsonArray = responseJson.getJSONArray("data");
            StringBuilder sb = new StringBuilder();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                if ("b64_json".equals(configurationClass.OPENAI_IMG_FORMAT)) {
                    sb.append("base64://").append(jsonObject.getString("b64_json"));
                    result.add(sb.toString());
                    sb.delete(0,sb.length());
                }else{
                    result.add(((JSONObject)o).getString("url"));
                }
            }
        }catch (Exception e){
            log.error("与openai服务连接异常：{}",e.getMessage());
            result.add("与openai服务连接异常：" + e.getMessage());
            e.printStackTrace();
        }
        log.debug("ChatGPTService.generatIMG结束");
        return result;
    }

    @Override
    public String queryBalance() {
        log.debug("ChatGPTService.queryBalance开始");
        String result = "";
        String response = "";
        try {
            //拼接URL
            String url = configurationClass.OPENAI_PROTOCOL + "://"
                    + configurationClass.OPENAI_DOMAIN + "/pro/balance?apiKey="
                    + configurationClass.OPENAI_KEY;
            response = HttpUtils.sendGet(url);
            JSONObject jsonObject = JSON.parseObject(response);
            JSONObject data = jsonObject.getJSONObject("data");
            StringBuilder sb = new StringBuilder();
            sb.append("总共：")
                    .append(data.getString("total"))
                    .append("$，已用：")
                    .append(data.getString("used"))
                    .append("$，剩余：")
                    .append(data.getString("balance"))
                    .append("$。");
            result = sb.toString();
        } catch (Exception e){
            log.error("与openai服务连接异常：{}",e.getMessage());
            result = "与openai服务连接异常：" + e.getMessage();
            e.printStackTrace();
        }
        log.debug("ChatGPTService.queryBalance结束");
        return result.toString();
    }

    public String chat(List<Message> messages) {
        log.debug("ChatGPTService.chat开始,参数message:{}",messages);
        //返回结果
        String result  = null;
        //接口响应信息
        String response = null;
        //拼接URL
        String url = configurationClass.OPENAI_PROTOCOL + "://" + configurationClass.OPENAI_DOMAIN + "/v1/chat/completions";
        log.info("post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.OPENAI_KEY);
       log.info("post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("model", configurationClass.OPENAI_CHAT_MODEL);
        param.put("messages",messages);
        if (!"".equals(configurationClass.OPENAI_TEXT_MAX_TOKENS)){
            param.put("max_tokens",Integer.parseInt(configurationClass.OPENAI_TEXT_MAX_TOKENS));
        }
        log.info("post请求参数:{}",param);
        try {
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("post请求响应:{}", response);
            JSONObject resultJSON = JSON.parseObject(response);
            result = ((JSONObject) resultJSON.getJSONArray("choices")
                    .get(0)).getString("text");
        } catch (Exception e){
            log.error("与openai服务连接异常：{}",e.getMessage());
            result = "与openai服务连接异常：" + e.getMessage();
            e.printStackTrace();
        }
        log.debug("ChatGPTService.answerQuestion结束");
        return result;
    }
}
