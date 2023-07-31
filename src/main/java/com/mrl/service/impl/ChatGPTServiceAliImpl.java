package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.ConstantClass;
import com.mrl.bean.Message;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.ChatGPTService;
import com.mrl.service.TaskImgChatGPTService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: MrL
 * @Date: 2023-04-24-12:15
 * @Description: com.mrl.service.impl-chatgptForJava
 * @Version: 1.0
 */
@Service
@Slf4j
public class ChatGPTServiceAliImpl implements TaskImgChatGPTService {

    @Autowired
    ConfigurationClass configurationClass;

    @Override
    public String answerQuestion(String prompt) {
        log.debug("ChatGPTService.answerQuestion开始,参数message:{}",prompt);
        //返回结果
        String result;
        //接口响应信息
        String response;
        //请求地址
        String url = configurationClass.ALIYUN_TYQW_URL + "?version-id=v1&task-group=aigc&task=text-generation&function-call=generation";
        log.info("文本生成post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.ALIYUN_API_KEY);
        log.info("文本生成post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("model", "qwen-v1");
        HashMap<String,String> inputMap = new HashMap<>();
        inputMap.put("prompt",prompt);
        param.put("input",inputMap);
        log.info("文本生成post请求参数:{}",JSON.toJSONString(param));
        try {
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("文本生成post请求相应信息:{}", response);
            result = JSON.parseObject(response)
                    .getJSONObject("output").getString("text");
        }catch (Exception e){
            log.error("与阿里云服务连接异常：{}",e.getMessage());
            result = "与阿里云服务连接异常：" + e.getMessage();
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
        JSONObject responseObject;
        //请求地址
        String url = configurationClass.ALIYUN_TYWX_URL;
        log.info("阿里通义万象post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.ALIYUN_API_KEY);
        headers.put("X-DashScope-Async", "enable");
        log.info("阿里通义万象post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("model","wanx-v1");
        HashMap<String,String> inputMap = new HashMap<>();
        inputMap.put("prompt",prompt);
        param.put("input",inputMap);
        HashMap<String,Object> parametersMap = new HashMap<>();
        if (!"".equals(configurationClass.ALIYUN_TYWX_STYLE)){
            parametersMap.put("style",configurationClass.ALIYUN_TYWX_STYLE);
        }
        if (!"".equals(configurationClass.ALIYUN_TYWX_SIZE)){
            parametersMap.put("size",configurationClass.ALIYUN_TYWX_SIZE);
        }
        if (!"".equals(configurationClass.ALIYUN_TYWX_NUM)){
            parametersMap.put("n",Integer.parseInt(configurationClass.ALIYUN_TYWX_NUM));
        }
        param.put("parameters",parametersMap);
        log.info("阿里通义万象post请求参数:{}",JSON.toJSONString(param));
        try {
            String response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("阿里通义万象post请求响应信息:{}", response);
            responseObject = JSON.parseObject(response);
            String taskId = responseObject.getJSONObject("output").getString("task_id");
            result.add(taskId);
        }catch (Exception e){
            log.error("阿里通义万象出错：{}",e.getMessage());
            e.printStackTrace();
        }
        log.debug("ChatGPTService.generatIMG结束");
        return result;
    }

    @Override
    public String queryBalance() {
        return "阿里云暂时用的免费额度，暂不支持余额查询。";
    }

    @Override
    public String chat(List<Message> messages) {
        log.debug("ChatGPTService.chat开始,参数message:{}",messages);
        //返回结果
        String result;
        //接口响应信息
        String response;
        //请求地址
        String url = configurationClass.ALIYUN_TYQW_URL + "?version-id=v1&task-group=aigc&task=text-generation&function-call=generation";
        log.info("文本生成post请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.ALIYUN_API_KEY);
        log.info("文本生成post请求头:{}", headers);
        //请求参数
        HashMap<String, Object> param = new HashMap<>();
        param.put("model", "qwen-v1");
        HashMap<String,String> inputMap = new HashMap<>();
        inputMap.put("prompt",messages.get(messages.size() - 1).getContent());
        ArrayList<HashMap<String,String>> historyList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < messages.size() - 1; i++) {
            HashMap<String,String> contentMap = new HashMap<>();
            String role = messages.get(i).getRole();
            if ("user".equals(role)){
                contentMap.put("user",messages.get(i).getContent());
            }else if ("assistant".equals(role)) {
                contentMap.put("bot",messages.get(i).getContent());
            }
            historyList.add(contentMap);
        }
        param.put("input",inputMap);
        param.put("history",historyList);
        log.info("文本生成post请求参数:{}",JSON.toJSONString(param));
        try {
            response = HttpUtils.sendPost(url, headers, JSON.toJSONString(param));
            log.info("文本生成post请求相应信息:{}", response);
            result = JSON.parseObject(response)
                    .getJSONObject("output").getString("text");
        }catch (Exception e){
            log.error("与阿里云服务连接异常：{}",e.getMessage());
            result = "与阿里云服务连接异常：" + e.getMessage();
            e.printStackTrace();
        }
        log.debug("ChatGPTService.chat结束");
        return result;
    }

    @Override
    public ArrayList<String> getImg(String taskId) {
        log.debug("ChatGPTService.getImg开始,参数taskId:{}",taskId);
        //返回结果
        ArrayList<String> result = new ArrayList<>();
        //接口响应信息
        JSONObject responseObject;
        //请求地址
        String url = configurationClass.ALIYUN_TYWX_GETIMG_URL + "/" + taskId;
        log.info("阿里通义万象获取图片get请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.ALIYUN_API_KEY);
        log.info("阿里通义万象获取图片get请求头:{}", headers);
        try {
            String response = HttpUtils.sendGet(url, headers);
            log.info("阿里通义万象获取图片get请求响应信息:{}", response);
            responseObject = JSON.parseObject(response);
            JSONArray imgs = responseObject.getJSONObject("output").getJSONArray("results");
            for (Object o : imgs) {
                result.add(((JSONObject)o).getString(url));
            }
            result.add(taskId);
        }catch (Exception e){
            log.error("阿里通义万象获取图片出错：{}",e.getMessage());
            e.printStackTrace();
        }
        log.debug("ChatGPTService.getImg结束");
        return result;
    }

    @Override
    public String getImgTaskStatus(String taskId) {
        log.debug("ChatGPTService.getImgTaskStatus开始,参数taskId:{}",taskId);
        //返回结果
        String result = ConstantClass.TASK_STATUS_UNKNOWN;
        //接口响应信息
        JSONObject responseObject;
        //请求地址
        String url = configurationClass.ALIYUN_TYWX_GETIMG_URL + "/" + taskId;
        log.info("阿里通义万象查询任务状态get请求地址:{}", url);
        //请求头
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + configurationClass.ALIYUN_API_KEY);
        log.info("阿里通义万象查询任务状态get请求头:{}", headers);
        try {
            String response = HttpUtils.sendGet(url, headers);
            log.info("阿里通义万象查询任务状态get请求响应信息:{}", response);
            responseObject = JSON.parseObject(response);
            result = responseObject.getJSONObject("output").getString("task_status");
        }catch (Exception e){
            log.error("阿里通义万象查询任务状态出错：{}",e.getMessage());
            e.printStackTrace();
        }
        log.debug("ChatGPTService.getImgTaskStatus结束");
        return result;
    }
}
