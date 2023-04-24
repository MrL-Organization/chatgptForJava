package com.mrl.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.ChatGPTService;
import com.mrl.service.QqRobotService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
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
    public void QqRobotEvenHandle(HttpServletRequest request) throws IOException {
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle开始");
        JSONObject jsonParam = this.getJSONParam(request);
        if ("message".equals(jsonParam.getString("post_type"))) {
            String user_id = "";
            String message = "";
            try {
                message = jsonParam.getString("message");
                user_id = jsonParam.getString("user_id");
                String aiMessage = "";
                if ("查询余额".equals(message)) {
                    aiMessage = chatGPTService.queryBalance();
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(aiMessage,user_id);
                }else if (message.startsWith("#生成图片")) {
                    String prompt = message.substring(6);
                    ArrayList<String> response = chatGPTService.generatIMG(prompt);
                    for (String s : response) {
                        StringBuilder sb = new StringBuilder(s);
                        sb.insert(0, "[CQ:image,file=")
                                .append(",type=show,id=40004]");
                        sendPrivateMsg(sb.toString(),user_id);
                    }
                } else if ("帮助".equals(message)) {
                    String sb = "1.如果只想让AI回答问题，请直接输入问题\n" +
                            "2.如果要生成图片请说：【#生成图片 描述】\n" +
                            "3.输入【查询余额】可以查询当前openAIkey的剩余tokens";
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(sb,user_id);
                } else {
                    //获取ai的返回信息
                    aiMessage = chatGPTService.answerQuestion(message);
                    //发送给机器人，然后机器人发送给我们
                    sendPrivateMsg(aiMessage,user_id);
                }
            } catch (Exception e) {
                log.error("QqRobotService出错:{}", e.getMessage());
                sendPrivateMsg(e.getMessage(),user_id);
            }
        }
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle结束");
    }

    /**
     * 获取Request对象中的参数转为JSONObject
     *
     * @param request
     * @return
     */
    private JSONObject getJSONParam(HttpServletRequest request) {
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
            log.debug("request参数转为json：{}", jsonParam);
        } catch (Exception e) {
            log.error("request参数转为json出错：{}", e.getMessage());
        }
        log.debug("QqRobotServiceImpl.getJSONParam结束");
        return jsonParam;
    }

    /**
     * 调取cq发送私聊消息的接口
     *
     * @param message 消息
     * @throws IOException
     * @throws ConnectException
     */
    @Override
    public void sendPrivateMsg(String message, String user_id) throws IOException, ConnectException {
        log.debug("QqRobotServiceImpl.sendToCqhttp开始");
        if ("".equals(user_id)){
            user_id = configurationClass.CQHTTP_USERID;
        }
        String url = configurationClass.CQHTTP_URL + "/send_private_msg?access_token=" + configurationClass.ACCESS_TOKEN;
        HashMap params = new HashMap<String, String>();
        params.put("user_id", user_id);
        params.put("message", message);
        log.info("私聊消息接口url:{}", url);
        log.info("私聊消息接口参数:{}", params);
        String response = HttpUtils.sendPost(url, null, HttpUtils.asUrlParams(params));
        log.info("私聊消息接口返回:{}", response);
        log.debug("QqRobotServiceImpl.sendToCqhttp结束");
    }
}