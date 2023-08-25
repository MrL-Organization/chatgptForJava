package com.mrl.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.Session;
import com.mrl.bean.ConstantClass;
import com.mrl.bean.Message;
import com.mrl.conf.ChatGPTServiceFactory;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.QqRobotService;
import com.mrl.service.TaskImgChatGPTService;
import com.mrl.util.HttpUtils;
import com.mrl.util.MagicPackageUtils;
import com.mrl.util.SheelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
    ChatGPTServiceFactory chatGPTServiceFactory;

    //存放用户的消息记录
    HashMap<String,ArrayList<Message>> user_messages = new HashMap<>();

    //存放用户当前是否处于连续聊天的状态,true-是,false-否
    HashMap<String, Boolean> user_status = new HashMap<>();

    //存放用户开始聊天的时间
    HashMap<String, Date> user_time = new HashMap<>();

    //设置30分钟过期时间
    final long EXPIRED_TIME = 1000*60*30;

    @Override
    //消息事件处理
    public HashMap<String,Object> QqRobotMessageHandle(JSONObject jsonParam) throws IOException {
        log.debug("QqRobotServiceImpl.QqRobotMessageHandle开始");

        //快速回复的响应参数
        HashMap<String,Object> result = new HashMap<>();
        String message = jsonParam.getString("message");
        String user_id = jsonParam.getString("user_id");
        try {
            String aiMessage = "";
            if (message.startsWith("#")){
                if ("#开始聊天".equals(message)){
                    if (user_status.get(user_id) != null && user_status.get(user_id)){
                        aiMessage = "已开启连续聊天，请不要重复开启！";
                    }else {
                        user_status.put(user_id,true);
                        user_time.put(user_id,new Date());
                        aiMessage = "已开启连续聊天，会增加tokens的使用量，聊天内容请不要过长，30分钟未关闭连续聊天，会自动关闭。";
                    }
                }else if ("#结束聊天".equals(message)){
                    user_status.remove(user_id);
                    user_messages.remove(user_id);
                    user_time.remove(user_id);
                    aiMessage = "已关闭连续聊天！";
                }else if ("#查询余额".equals(message)) {
                    aiMessage = chatGPTServiceFactory.getChatGPTService().queryBalance();
                }else if ("#当前服务".equals(message)) {
                    switch (chatGPTServiceFactory.getServerChoose()){
                        case "1" : aiMessage = "当前服务为openAI的chatgpt和生成图片"; break;
                        case "2" : aiMessage = "当前服务为百度的文心一言和AI作画"; break;
                        case "3" : aiMessage = "当前服务为阿里的通义千问和通义万象"; break;
                        case "4" : aiMessage = "当前服务为微软的newBing"; break;
                    }
                }else if (message.startsWith("#生成图片")) {
                    String prompt = message.substring(6);
                    ArrayList<String> response = chatGPTServiceFactory.getChatGPTService().generatIMG(prompt);
                    if (response != null && response.size() > 0) {
                        //判断当前服务是否需要taskId去获取图片，是的话返回的是taskId
                        if (chatGPTServiceFactory.isTaskImg()) {
                            aiMessage = "正在生成图片，taskId:【" + response.get(0) + "】，稍后可以用taskId去获取图片。";
                        }else {
                            for (String s : response) {
                                StringBuilder sb = new StringBuilder(s);
                                sb.insert(0, "[CQ:image,file=")
                                        .append(",type=show,id=40004]");
                                aiMessage = sb.toString();
                            }
                        }
                    }else {
                        aiMessage = "生成图片失败,可能对应的服务不支持生成图片。";
                    }
                }else if (message.startsWith("#查询任务状态")) {
                    if (!chatGPTServiceFactory.isTaskImg()) {
                        aiMessage = "只有百度AI作画或者阿里通义万象生成图片需要用taskId去获取图片，你可以输入【#当前服务】来查询当前使用的的服务。";
                    }else {
                        String taskId = message.substring(8);
                        String response = ((TaskImgChatGPTService) chatGPTServiceFactory.getChatGPTService()).getImgTaskStatus(taskId);
                        switch (response) {
                            case ConstantClass.TASK_STATUS_SUCCESS:
                                aiMessage = "任务成功，请用【#获取图片 taskId】获取图片。";
                                break;
                            case ConstantClass.TASK_STATUS_FAILED:
                                aiMessage = "任务失败！";
                                break;
                            case ConstantClass.TASK_STATUS_PENDING:
                                aiMessage = "任务正在排队，请稍后再重新查询。";
                                break;
                            case ConstantClass.TASK_STATUS_RUNNING:
                                aiMessage = "任务正在处理，请稍后再重新查询。";
                                break;
                            case ConstantClass.TASK_STATUS_UNKNOWN:
                                aiMessage = "任务不存在，请重新生成图片。";
                                break;
                            default:
                                aiMessage = "未知状态，请重试。";
                                break;
                        }
                    }
                }else if (message.startsWith("#获取图片")) {
                    if (!chatGPTServiceFactory.isTaskImg()) {
                        aiMessage = "只有百度AI作画或者阿里通义万象生成图片需要用taskId去获取图片，你可以输入【#当前服务】来查询当前使用的的服务。";
                    }else {
                        String taskId = message.substring(6);
                        ArrayList<String> response = ((TaskImgChatGPTService) chatGPTServiceFactory.getChatGPTService()).getImg(taskId);
                        if (response != null && response.size() > 0) {
                            for (String s : response) {
                                StringBuilder sb = new StringBuilder(s);
                                sb.insert(0, "[CQ:image,file=")
                                        .append(",type=show,id=40004]");
                                aiMessage = sb.toString();
                            }
                        } else {
                            aiMessage = "获取图片失败！";
                        }
                    }
                }else if (message.startsWith("#打开电脑")) {
                    if (configurationClass.CQHTTP_USERID.equals(user_id)) {
                        String broadcastAddress = MagicPackageUtils.getBroadcastAddress(configurationClass.WOL_IP,configurationClass.WOL_MASK);
                        aiMessage = MagicPackageUtils.sendMagicPackage(broadcastAddress,configurationClass.WOL_MAC);
                    }else {
                        aiMessage = "您不是此bot的管理员，无权使用该命令！";
                    }

                }else if (message.startsWith("#关闭电脑")) {
                    if (configurationClass.CQHTTP_USERID.equals(user_id)) {
                        if(SheelUtils.login(configurationClass.WOL_IP, configurationClass.WOL_USER, configurationClass.WOL_PASSWORD)) {
                            String execute = SheelUtils.execCommand( "shutdown -s -t 60");
                            SheelUtils.close();
                            aiMessage = "指令发送成功，不出意外电脑将在60s后关闭，返回结果：" + execute;
                        }else {
                            aiMessage = "登录ssh失败！";
                        }
                    }else {
                        aiMessage = "您不是此bot的管理员，无权使用该命令！";
                    }

                }else {
                    aiMessage = "未知命令，请输入【帮助】查看所有命令！";
                }
            } else if ("帮助".equals(message)) {
                aiMessage = "1.[CQ:face,id=32]单问单答[CQ:face,id=32]：如果只想让AI回答问题，请直接输入问题，例如【马化腾是谁】；\n" +
                        "2.[CQ:face,id=74]生成图片[CQ:face,id=74]：如果要生成图片请说：【#生成图片 描述】，例如【#生成图片 打篮球的鸡】；\n" +
                        "3.[CQ:face,id=179]查询生成图片任务状态[CQ:face,id=179]:输入【#查询任务状态 taskId】，查询生成图片任务状态，例如【#查询任务状态 16999422】；\n" +
                        "4.[CQ:face,id=175]获取生成的图片[CQ:face,id=175]:输入【#获取图片 taskId】，获取生成的图片，例如【#获取图片 16999422】；\n" +
                        "5.[CQ:face,id=176]查询余额[CQ:face,id=176]：输入【#查询余额】可以查询当前服务的余额；\n" +
                        "6.[CQ:face,id=101]连续聊天[CQ:face,id=101]：输入【#开始聊天】，即可开始连续聊天，输入【#结束聊天】，即可结束聊天；\n" +
                        "7.[CQ:face,id=98]查询目前用的是哪个服务[CQ:face,id=98]：输入【#当前服务】；\n" +
                        "8.[CQ:face,id=49]帮助[CQ:face,id=49]:输入【帮助】，查看当前帮助信息；\n" +
                        "9.[CQ:face,id=178]远程打开家里电脑[CQ:face,id=178]：输入【#打开电脑】(管理员)。\n" +
                        "9.[CQ:face,id=39]远程关闭家里电脑[CQ:face,id=39]：输入【#关闭电脑】(管理员)。\n" +
                        "请注意，如果用的是百度AI作画或者阿里通义万象生成图片会返回taskId，然后用3的命令用taskId去查询任务状态，用4的命令去获取图片。\n" +
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
        log.debug("QqRobotServiceImpl.QqRobotEvenHandle结束");
        return result;
    }

    @Override
    //请求事件处理
    public HashMap<String, Object> QqRobotRequestHandle(JSONObject jsonParam) throws IOException {
        log.debug("QqRobotServiceImpl.QqRobotRequestHandle开始");
        //快速回复的响应参数
        HashMap<String,Object> result = new HashMap<>();
        String user_id = jsonParam.getString("user_id");
        String request_type = jsonParam.getString("request_type");
        if ("friend".equals(request_type)){
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
        log.debug("QqRobotServiceImpl.QqRobotRequestHandle结束");
        return result;
    }

    /**
     * 发送私聊消息
     */
    @Override
    public void sendPrivateMsg(String message, String user_id) throws IOException {
        log.debug("QqRobotServiceImpl.sendPrivateMsg开始");
        if ("".equals(user_id) || user_id == null){
            user_id = configurationClass.CQHTTP_USERID;
        }
        String url = configurationClass.CQHTTP_URL + "/send_private_msg?access_token=" + configurationClass.ACCESS_TOKEN;
        HashMap<String,Object> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("message", message);
        log.info("私聊消息接口post请求地址:{}", url);
        log.info("私聊消息接口post请求参数:{}", params);
        String response = HttpUtils.sendPost(url, null, HttpUtils.asUrlParams(params));
        log.info("私聊消息接口post请求返回信息:{}", response);
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
        log.info("同意好友申请post请求地址:{}", url);
        log.info("同意好友申请post请求参数:{}", params);
        String response = HttpUtils.sendPost(url, null, HttpUtils.asUrlParams(params));
        log.info("同意好友申请post请求返回信息:{}", response);
        log.debug("QqRobotServiceImpl.agreeFriendRequest结束");
    }


}