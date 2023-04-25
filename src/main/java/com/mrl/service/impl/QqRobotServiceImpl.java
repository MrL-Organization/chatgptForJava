package com.mrl.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.Message;
import com.mrl.conf.ChatGPTServiceFactory;
import com.mrl.conf.ConfigurationClass;
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
import java.util.Date;
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

    @Resource
    ConfigurationClass configurationClass;

    @Resource
    ChatGPTServiceFactory chatGPTServiceFactory;

    //存放用户的消息记录
    HashMap<String,ArrayList<Message>> user_messages = new HashMap<>();

    //存放用户当前是否处于连续聊天的状态,true-是,false-否
    HashMap<String, Boolean> user_status = new HashMap<>();

    //存放用户开始聊天的时间
    HashMap<String, Date> user_time = new HashMap<>();

    //设置30分钟过期时间
    final long EXPIRED_TIME = 1000*60*30;

    //ChatGPTService chatGPTService;

    @Override
    public Map QqRobotEvenHandle(HttpServletRequest request) throws IOException {
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle开始");
        JSONObject jsonParam = this.getJSONParam(request);
        //快速回复的响应参数
        HashMap<String,Object> result = new HashMap<>();
        String post_type = jsonParam.getString("post_type");
        String message = jsonParam.getString("message");
        String user_id = jsonParam.getString("user_id");
        String request_type = jsonParam.getString("request_type");
        if ("message".equals(post_type)) {
            try {
                String aiMessage = "";
                if (message.startsWith("#")){
                    if ("#开始聊天".equals(message)){
                        user_status.put(user_id,true);
                        user_time.put(user_id,new Date());
                        aiMessage = "已开启连续聊天，会增加tokens的使用量，聊天内容请不要过长，30分钟未关闭连续聊天，会自动关闭。";
                    }else if ("#结束聊天".equals(message)){
                        user_status.remove(user_id);
                        user_messages.remove(user_id);
                        user_time.remove(user_id);
                        aiMessage = "已关闭连续聊天！";
                    }else if ("#查询余额".equals(message)) {
                        aiMessage = chatGPTServiceFactory.getChatGPTService().queryBalance();
                    }else if (message.startsWith("#生成图片")) {
                        String prompt = message.substring(6);
                        ArrayList<String> response = chatGPTServiceFactory.getChatGPTService().generatIMG(prompt);
                        for (String s : response) {
                            StringBuilder sb = new StringBuilder(s);
                            sb.insert(0, "[CQ:image,file=")
                                    .append(",type=show,id=40004]");
                            aiMessage = sb.toString();
                        }
                    }else {
                        aiMessage = "未知命令，请输入【帮助】查看所有命令！";
                    }
                } else if ("帮助".equals(message)) {
                    aiMessage = "1.单问单答：如果只想让AI回答问题，请直接输入问题，例如【马化腾是谁】；\n" +
                            "2.生成图片：如果要生成图片请说：【#生成图片 描述】，例如【#生成图片 打篮球的鸡】；\n" +
                            "3.查询余额：输入【#查询余额】可以查询当前key的剩余美元；\n" +
                            "4.连续聊天：输入【#开始聊天】，即可开始连续聊天，输入【#结束聊天】，即可结束聊天；\n" +
                            "5.帮助:输入【帮助】，查看当前帮助信息；\n" +
                            "请注意，连续聊天功能会耗费大量的tokens，请节制使用。";
                } else {
                    if (user_status != null && user_status.size() > 0 && user_status.get(user_id)){
                        ArrayList<Message> messages;
                        if (user_time.get(user_id).getTime() + EXPIRED_TIME < System.currentTimeMillis()){
                            //30分钟已到，连续状态到期
                            user_status.remove(user_id);
                            user_time.remove(user_id);
                            user_messages.remove(user_id);
                            messages = new ArrayList<>();
                            messages.add(new Message("user",message));
                            aiMessage = "连续聊天已关闭。\n"+chatGPTServiceFactory.getChatGPTService().chat(messages);
                        }else{
                            //处于连续聊天状态，从user_messages里拿历史记录
                            messages = user_messages.computeIfAbsent(user_id, k -> new ArrayList<>());
                            Message send = new Message("user", message);
                            messages.add(send);
                            aiMessage = chatGPTServiceFactory.getChatGPTService().chat(messages);
                            Message response = new Message("assistant",aiMessage);
                            messages.add(response);
                        }
                    }else {
                        //获取ai的返回信息,单问单答
                        ArrayList<Message> messages = new ArrayList<>();
                        messages.add(new Message("user",message));
                        aiMessage = chatGPTServiceFactory.getChatGPTService().chat(messages);
                    }
                }
                //发送消息
                //sendPrivateMsg(aiMessage, user_id);
                result.put("reply",aiMessage);
                result.put("auto_escape",false);
            } catch (Exception e) {
                log.error("QqRobotService出错:{}", e.getMessage());
                e.printStackTrace();
                sendPrivateMsg(e.getMessage(),user_id);
            }
        }else if ("request".equals(post_type) && "friend".equals(request_type)){
            //自动通过好友请求
            String flag = jsonParam.getString("flag");
            try {
                //result.put("approve",true);
                agreeFriendRequest(flag,true,null);
                //休眠1s，好友通过后再发送消息
                Thread.sleep(1000);
                sendPrivateMsg("请发送【帮助】查看提示信息。",user_id);
            } catch (Exception e) {
                log.error("QqRobotService出错:{}", e.getMessage());
                e.printStackTrace();
                sendPrivateMsg(user_id+"好友添加失败:"+e.getMessage(),"");
            }
        }
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle结束");
        return result;
    }

    /**
     * 调取cq发送私聊消息的接口
     *
     * @param message 消息
     * @throws IOException
     * @throws ConnectException
     */
    @Override
    public void sendPrivateMsg(String message, String user_id) throws IOException {
        log.debug("QqRobotServiceImpl.sendPrivateMsg开始");
        if ("".equals(user_id) || user_id == null){
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
        log.debug("QqRobotServiceImpl.sendPrivateMsg结束");
    }

    /**
     * 同意好友申请
     */
    @Override
    public void agreeFriendRequest(String flag,boolean approve,String remark) throws IOException {
        log.debug("QqRobotServiceImpl.agreeFriendRequest开始");
        String url = configurationClass.CQHTTP_URL + "/set_friend_add_request?access_token=" + configurationClass.ACCESS_TOKEN;
        HashMap<String,Object> params = new HashMap<>();
        params.put("flag", flag);
        params.put("approve", approve);
        params.put("remark", remark);
        log.info("同意好友申请接口url:{}", url);
        log.info("同意好友申请接口参数:{}", params);
        String response = HttpUtils.sendPost(url, null, HttpUtils.asUrlParams(params));
        log.info("同意好友申请接口返回:{}", response);
        log.debug("QqRobotServiceImpl.agreeFriendRequest结束");
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
}