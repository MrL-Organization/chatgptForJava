package com.mrl.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mrl.conf.ConfigurationClass;
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

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:46
 * @Description: com.mrl.service.impl-chatgpt
 * @Version: 1.0
 */
@Service
@Slf4j
public class QqRobotServiceImpl implements QqRobotService {

    @Resource
    ConfigurationClass configurationClass;

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
                StringBuilder aiMessage = new StringBuilder();
                if ("查询余额".equals(message)){
                    JSONObject aiResponse = chatGPTService.queryBalance();
                    JSONObject data = aiResponse.getJSONObject("data");
                    aiMessage.append("总共：")
                            .append(data.getString("total"))
                            .append("，已用：")
                            .append(data.getString("used"))
                            .append("，剩余：")
                            .append(data.getString("balance"))
                            .append("。");
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(aiMessage.toString());
                }else if (message.startsWith("#生成图片")) {
                    String prompt = message.substring(6);
                    JSONObject aiResponse = chatGPTService.generatIMG(prompt);
                    JSONArray arr = aiResponse.getJSONArray("data");
                    if (arr == null || arr.size() == 0) {
                        result.put("message","openai接口返回空！");
                        return result;
                    }
                    for (Object o : arr) {
                        if ("url".equals(configurationClass.OPENAI_IMG_FORMAT)) {
                            String url = ((JSONObject) o).getString("url");
                            aiMessage.append("[CQ:image,file=")
                                        .append(url)
                                        .append(",type=show,id=40004]");
                        }else if ("b64_json".equals(configurationClass.OPENAI_IMG_FORMAT)) {
                            String b64_json = ((JSONObject) o).getString("b64_json");
                            aiMessage.append("[CQ:image,file=base64://")
                                    .append(b64_json)
                                    .append(",type=show,id=40004]");
                        }
                        //发送给机器人，然后机器人发送给我们
                        sendPrivateMsg(aiMessage.toString());
                        aiMessage.delete(0,aiMessage.length());
                    }
                }else if ("帮助".equals(message)){
                    aiMessage.append("1.如果只想让AI回答问题，请直接输入问题\n")
                                .append("2.如果要生成图片请说：【#生成图片 描述】\n")
                                .append("3.输入【查询余额】可以查询当前openAIkey的剩余tokens");
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(aiMessage.toString());
                }else {
                    //获取ai的返回信息
                    JSONObject aiResponse = chatGPTService.answerQuestion(message);
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(((JSONObject) aiResponse.getJSONArray("choices").get(0)).getJSONObject("message").getString("content"));
                }
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

    /**
     * 获取Request对象中的参数转为JSONObject
     * @param request
     * @return
     */
    private JSONObject getJSONParam(HttpServletRequest request){
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

    /**
     * 调取cq发送私聊消息的接口
     * @param message 消息
     * @throws IOException
     * @throws ConnectException
     */
    @Override
    public void sendPrivateMsg(String message) throws IOException, ConnectException {
        log.debug("QqRobotServiceImpl.sendToCqhttp开始");
        String url = configurationClass.CQHTTP_URL + "/send_private_msg?access_token=" + configurationClass.ACCESS_TOKEN;
        HashMap params = new HashMap<String,String>();
        params.put("user_id", configurationClass.CQHTTP_USERID);
        params.put("message",message);
        log.info("私聊消息接口url:{}",url);
        log.info("私聊消息接口参数:{}",params);
        String response = HttpUtils.sendPost(url,null, HttpUtils.asUrlParams(params));
        log.info("私聊消息接口返回:{}", response);
        log.debug("QqRobotServiceImpl.sendToCqhttp结束");
    }

    /**
     * cq码转义特殊字符
     * @param s
     * @return
     */
    private String cqCodeEscape(String s){
        return s.replaceAll("&", "&amp;")
                .replaceAll("\\[", "&#91;")
                .replaceAll("]", "&#93;")
                .replaceAll(",", "&#44;");
    }
}