package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mrl.service.ChatGPTService;
import com.mrl.service.QqRobotService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:46
 * @Description: com.mrl.service.impl-chatgpt
 * @Version: 1.0
 */
@Service
@Slf4j
public class QqRobotServiceImpl implements QqRobotService {

    @Value("${cqhttp.url}")
    String cqHttpUrl;

    @Value("${cqhttp.userId}")
    String userId;

    @Value("${cqhttp.access_token}")
    String access_token;

    @Resource
    ChatGPTService chatGPTService;

    @Override
    public JSONObject QqRobotEvenHandle(HttpServletRequest request) {
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle开始");
        JSONObject result = new JSONObject();
        result.put("message", "fail");
        JSONObject jsonParam = this.getJSONParam(request);
        if("message".equals(jsonParam.getString("post_type"))){
            try {
                String message = jsonParam.getString("message");
                String aiMessage = "";
                if ("查询余额".equals(message)){
                    JSONObject aiResponse = chatGPTService.queryBalance();
                    JSONObject data = aiResponse.getJSONObject("data");
                    StringBuilder sb = new StringBuilder();
                    sb.append("总共：")
                            .append(data.getString("total"))
                            .append(",已用：")
                            .append(data.getString("used"))
                            .append(",剩余：")
                            .append(data.getString("balance"))
                            .append("。");
                    aiMessage = sb.toString();
                }else {
                    //获取ai的返回信息
                    JSONObject aiResponse = chatGPTService.sendToAI(message);
                    aiMessage = ((JSONObject) aiResponse.getJSONArray("choices").get(0)).getJSONObject("message").getString("content");
                }
                //发送给机器人，然后机器人发送给我们
                sendToCqhttp(aiMessage);
                result.put("message", "success");
            } catch (Exception e) {
                log.error("出错:{}",e.getMessage());
                result.put("message",e.getMessage());
                e.printStackTrace();
            }
        }
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle结束");
        return result;
    }

    public JSONObject getJSONParam(HttpServletRequest request){
        log.debug("QqRobotServiceImpl.getJSONParam开始");
        JSONObject jsonParam = null;
        try {
            // 获取输入流
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

            // 数据写入Stringbuilder
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = streamReader.readLine()) != null) {
                sb.append(line);
            }
            jsonParam = JSONObject.parseObject(sb.toString());
            log.debug("request参数转为json：{}",jsonParam);
        } catch (Exception e) {
            log.error("request参数转为json出错：{}",e.getMessage());
        }
        log.debug("QqRobotServiceImpl.getJSONParam结束");
        return jsonParam;
    }

    public void sendToCqhttp(String message) throws IOException, ConnectException {
        log.debug("QqRobotServiceImpl.sendToCqhttp开始");
        String url = cqHttpUrl + "/send_private_msg?access_token=" + access_token;
        HashMap params = new HashMap<String,String>();
        params.put("user_id",userId);
        params.put("message",message);
        log.info("发送机器人请求接口，参数:{}",params);
        String response = HttpUtils.sendPost(url,null, HttpUtils.asUrlParams(params));
        log.info("返回信息:{}", response);
        log.debug("QqRobotServiceImpl.sendToCqhttp结束");
    }
}