package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mrl.bean.ConstantClass;
import com.mrl.bean.Message;
import com.mrl.conf.ChatGPTServiceFactory;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.TaskImgChatGPTService;
import com.mrl.service.WechatRobotService;
import com.mrl.util.HttpUtils;
import com.mrl.util.MagicPackageUtils;
import com.mrl.util.SheelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-10-27-15:01
 * @Description: com.mrl.service.impl-chatgptForJava
 * @Version: 1.0
 */
@Service
@Slf4j
public class WechatRobotServiceImpl implements WechatRobotService {

    @Resource
    ConfigurationClass configurationClass;

    @Resource
    ChatGPTServiceFactory chatGPTServiceFactory;

    //存放用户的消息记录
    HashMap<String, ArrayList<Message>> user_messages = new HashMap<>();

    //存放用户当前是否处于连续聊天的状态,true-是,false-否
    HashMap<String, Boolean> user_status = new HashMap<>();

    //存放用户开始聊天的时间
    HashMap<String, Date> user_time = new HashMap<>();

    //设置30分钟过期时间
    final long EXPIRED_TIME = 1000*60*30;

    //微信ACCESS_TOKEN
    private String ACCESS_TOKEN;

    //@PostConstruct
    private void setToken(){
        log.info("wechat开始获取access_token");
        HashMap<String,Object> params = new HashMap<>();
        params.put("grant_type","client_credential");
        params.put("appid",configurationClass.WX_APPID);
        params.put("secret",configurationClass.WX_SECRET);
        JSONObject jsonObject = null;
        try{
            String response = HttpUtils.sendGet(configurationClass.WX_GETTOKEN_URL, HttpUtils.asUrlParams(params));
            log.debug("获取ACCESS_TOKEN请求结果：{}",response);
            jsonObject = JSON.parseObject(response);
            this.ACCESS_TOKEN = jsonObject.getString("access_token");
            log.info("设置ACCESS_TOKEN:{}",ACCESS_TOKEN);
        }catch (Exception e){
            if (jsonObject != null){
                String errcode = jsonObject.getString("errcode");
                String errmsg = jsonObject.getString("errmsg");
                log.error("获取ACCESS_TOKEN失败,errcode:{},errmsg:{}",errcode,errmsg);
            }else {
                log.error("获取ACCESS_TOKEN失败，原因：{}",e.getMessage());
            }
        }
    }

    @Override
    //消息事件处理
    public HashMap<String,String> MessageHandle(Map<String,String> params) {
        log.debug("WechatRobotServiceImpl.WechatRobotMessageHandle开始");

        //快速回复的响应参数
        HashMap<String,String> result = new HashMap<>();
        result.put("ToUserName",params.get("FromUserName"));
        result.put("FromUserName",params.get("ToUserName"));
        result.put("CreateTime",Long.toString(new Date().getTime()));
        result.put("MsgType","text");
        String message = params.get("Content");
        String userId = params.get("FromUserName");
        try {
            StringBuilder aiMessage = new StringBuilder();
            if (message.startsWith("#")){
                if ("#开始聊天".equals(message)){
                    if (user_status.get(userId) != null && user_status.get(userId)){
                        aiMessage = new StringBuilder("已开启连续聊天，请不要重复开启！");
                    }else {
                        user_status.put(userId,true);
                        user_time.put(userId,new Date());
                        aiMessage = new StringBuilder("已开启连续聊天，会增加tokens的使用量，聊天内容请不要过长，30分钟未关闭连续聊天，会自动关闭。");
                    }
                }else if ("#结束聊天".equals(message)){
                    user_status.remove(userId);
                    user_messages.remove(userId);
                    user_time.remove(userId);
                    aiMessage = new StringBuilder("已关闭连续聊天！");
                }else if ("#查询余额".equals(message)) {
                    aiMessage = new StringBuilder(chatGPTServiceFactory.getChatGPTService().queryBalance());
                }else if ("#当前服务".equals(message)) {
                    switch (chatGPTServiceFactory.getServerChoose()){
                        case "1" : aiMessage = new StringBuilder("当前服务为openAI的chatgpt和生成图片"); break;
                        case "2" : aiMessage = new StringBuilder("当前服务为百度的文心一言和AI作画"); break;
                        case "3" : aiMessage = new StringBuilder("当前服务为阿里的通义千问和通义万象"); break;
                        case "4" : aiMessage = new StringBuilder("当前服务为微软的newBing"); break;
                    }
                }else if (message.startsWith("#生成图片")) {
                    String prompt = message.substring(6);
                    ArrayList<String> response = chatGPTServiceFactory.getChatGPTService().generatIMG(prompt);
                    if (response != null && response.size() > 0) {
                        //判断当前服务是否需要taskId去获取图片，是的话返回的是taskId
                        if (chatGPTServiceFactory.isTaskImg()) {
                            aiMessage = new StringBuilder("正在生成图片，taskId:【" + response.get(0) + "】，稍后可以用taskId去获取图片。");
                        }else {
                            for (String s : response) {
                                aiMessage.append(s).append("\n");
                            }
                        }
                    }else {
                        aiMessage = new StringBuilder("生成图片失败,可能对应的服务不支持生成图片。");
                    }
                }else if (message.startsWith("#查询任务状态")) {
                    if (!chatGPTServiceFactory.isTaskImg()) {
                        aiMessage = new StringBuilder("只有百度AI作画或者阿里通义万象生成图片需要用taskId去获取图片，你可以输入【#当前服务】来查询当前使用的的服务。");
                    }else {
                        String taskId = message.substring(8);
                        String response = ((TaskImgChatGPTService) chatGPTServiceFactory.getChatGPTService()).getImgTaskStatus(taskId);
                        switch (response) {
                            case ConstantClass.TASK_STATUS_SUCCESS:
                                aiMessage = new StringBuilder("任务成功，请用【#获取图片 taskId】获取图片。");
                                break;
                            case ConstantClass.TASK_STATUS_FAILED:
                                aiMessage = new StringBuilder("任务失败！");
                                break;
                            case ConstantClass.TASK_STATUS_PENDING:
                                aiMessage = new StringBuilder("任务正在排队，请稍后再重新查询。");
                                break;
                            case ConstantClass.TASK_STATUS_RUNNING:
                                aiMessage = new StringBuilder("任务正在处理，请稍后再重新查询。");
                                break;
                            case ConstantClass.TASK_STATUS_UNKNOWN:
                                aiMessage = new StringBuilder("任务不存在，请重新生成图片。");
                                break;
                            default:
                                aiMessage = new StringBuilder("未知状态，请重试。");
                                break;
                        }
                    }
                }else if (message.startsWith("#获取图片")) {
                    if (!chatGPTServiceFactory.isTaskImg()) {
                        aiMessage = new StringBuilder("只有百度AI作画或者阿里通义万象生成图片需要用taskId去获取图片，你可以输入【#当前服务】来查询当前使用的的服务。");
                    }else {
                        String taskId = message.substring(6);
                        ArrayList<String> response = ((TaskImgChatGPTService) chatGPTServiceFactory.getChatGPTService()).getImg(taskId);
                        if (response != null && response.size() > 0) {
                            for (String s : response) {
                                aiMessage.append(s).append("\n");
                            }
                        } else {
                            aiMessage = new StringBuilder("获取图片失败！");
                        }
                    }
                }else if (message.startsWith("#打开电脑")) {
                    if (configurationClass.WX_ADMIN.equals(userId)) {
                        String broadcastAddress = MagicPackageUtils.getBroadcastAddress(configurationClass.WOL_IP,configurationClass.WOL_MASK);
                        aiMessage = new StringBuilder(MagicPackageUtils.sendMagicPackage(broadcastAddress, configurationClass.WOL_MAC));
                    }else {
                        aiMessage = new StringBuilder("您不是此bot的管理员，无权使用该命令！");
                    }

                }else if (message.startsWith("#关闭电脑")) {
                    if (configurationClass.WX_ADMIN.equals(userId)) {
                        if(SheelUtils.login(configurationClass.WOL_IP, configurationClass.WOL_USER, configurationClass.WOL_PASSWORD)) {
                            String execute = SheelUtils.execCommand( "shutdown -s -t 60");
                            SheelUtils.close();
                            aiMessage = new StringBuilder("指令发送成功，不出意外电脑将在60s后关闭，返回结果：" + execute);
                        }else {
                            aiMessage = new StringBuilder("登录ssh失败！");
                        }
                    }else {
                        aiMessage = new StringBuilder("您不是此bot的管理员，无权使用该命令！");
                    }

                }else {
                    aiMessage = new StringBuilder("未知命令，请输入【帮助】查看所有命令！");
                }
            } else if ("帮助".equals(message)) {
                aiMessage = new StringBuilder("1.单问单答：如果只想让AI回答问题，请直接输入问题，例如【马化腾是谁】；\n" +
                        "2.生成图片：如果要生成图片请说：【#生成图片 描述】，例如【#生成图片 打篮球的鸡】；\n" +
                        "3.查询生成图片任务状态:输入【#查询任务状态 taskId】，查询生成图片任务状态，例如【#查询任务状态 16999422】；\n" +
                        "4.获取生成的图片:输入【#获取图片 taskId】，获取生成的图片，例如【#获取图片 16999422】；\n" +
                        "5.查询余额：输入【#查询余额】可以查询当前服务的余额；\n" +
                        "6.连续聊天：输入【#开始聊天】，即可开始连续聊天，输入【#结束聊天】，即可结束聊天；\n" +
                        "7.查询目前用的是哪个服务：输入【#当前服务】；\n" +
                        "8.帮助:输入【帮助】，查看当前帮助信息；\n" +
                        "9.远程打开家里电脑：输入【#打开电脑】(管理员)。\n" +
                        "10.远程关闭家里电脑：输入【#关闭电脑】(管理员)。\n" +
                        "请注意，如果用的是百度AI作画或者阿里通义万象生成图片会返回taskId，然后用3的命令用taskId去查询任务状态，用4的命令去获取图片。\n" +
                        "请注意，连续聊天功能会耗费大量的tokens，请节制使用。");
            } else {
                if (user_status != null && user_status.size() > 0 && user_status.get(userId)){
                    ArrayList<Message> messages;
                    if (user_time.get(userId).getTime() + EXPIRED_TIME < System.currentTimeMillis()){
                        //30分钟已到，连续状态到期
                        user_status.remove(userId);
                        user_time.remove(userId);
                        user_messages.remove(userId);
                        messages = new ArrayList<>();
                        messages.add(new Message("user",message));
                        aiMessage = new StringBuilder("连续聊天已关闭。\n" + chatGPTServiceFactory.getChatGPTService().chat(messages));
                    }else{
                        //处于连续聊天状态，从user_messages里拿历史记录
                        messages = user_messages.computeIfAbsent(userId, k -> new ArrayList<>());
                        Message send = new Message("user", message);
                        if (messages.size() % 2 == 0) {
                            messages.add(send);
                        }
                        String responseMsg = chatGPTServiceFactory.getChatGPTService().chat(messages);
                        if (responseMsg == null || "".equals(responseMsg)){
                            aiMessage = new StringBuilder("服务异常，请稍后再试！");
                        }else {
                            aiMessage = new StringBuilder(responseMsg);
                            Message response = new Message("assistant", aiMessage.toString());
                            messages.add(response);
                        }
                    }
                }else {
                    //获取ai的返回信息,单问单答
                    ArrayList<Message> messages = new ArrayList<>();
                    messages.add(new Message("user",message));
                    aiMessage = new StringBuilder(chatGPTServiceFactory.getChatGPTService().chat(messages));
                }
            }
            //发送消息
            //sendPrivateMsg(aiMessage, userId);
            result.put("Content", aiMessage.toString());
        } catch (Exception e) {
            log.error("WechatRobotService出错:{}", e.getMessage());
            e.printStackTrace();
            //try {
            //    sendPrivateMsg(e.getMessage(),userId);
            //} catch (Exception ex) {
            //    log.error("WechatRobotService.sendPrivateMsg出错:{}", ex.getMessage());
            //}
        }
        log.debug("WechatRobotServiceImpl.WechatRobotMessageHandle结束");
        return result;
    }

    @Override
    //请求事件处理
    public HashMap<String, Object> EventHandle(Map<String,String> params) {
        //log.debug("WechatRobotServiceImpl.WechatRobotRequestHandle开始");
        ////快速回复的响应参数
        //HashMap<String,Object> result = new HashMap<>();
        //String user_id = jsonParam.getString("user_id");
        //String request_type = jsonParam.getString("request_type");
        //if ("friend".equals(request_type)){
        //    //自动通过好友请求
        //    String flag = jsonParam.getString("flag");
        //    try {
        //        //result.put("approve",true);
        //        agreeFriendRequest(flag,true,null);
        //        //休眠1s，好友通过后再发送消息
        //        Thread.sleep(1000);
        //        sendPrivateMsg("请发送【帮助】查看提示信息。",user_id);
        //    } catch (Exception e) {
        //        log.error("WechatRobotService出错:{}", e.getMessage());
        //        e.printStackTrace();
        //        sendPrivateMsg(user_id+"好友添加失败:"+e.getMessage(),"");
        //    }
        //}
        //log.debug("WechatRobotServiceImpl.WechatRobotRequestHandle结束");
        //return result;
        return null;
    }

    /**
     * 发送私聊消息-无接口权限，公众号必须完成微信认证，但是个人公众号不能进行微信认证
     */
    @Override
    public void sendPrivateMsg(String message, String userId) throws IOException {
        log.debug("WechatRobotServiceImpl.sendPrivateMsg开始");
        if (ACCESS_TOKEN == null || "".equals(ACCESS_TOKEN)){
            setToken();
        }
        if ("".equals(userId) || userId == null){
            userId = configurationClass.WX_ADMIN;
        }
        String url = configurationClass.WX_SENDMSG_URL + "?access_token=" + ACCESS_TOKEN;
        HashMap<String,Object> params = new HashMap<>();
        params.put("touser", userId);
        params.put("msgtype", "text");
        HashMap<String,Object> text = new HashMap<>();
        text.put("content",message);
        params.put("text", text);
        log.info("私聊消息接口post请求地址:{}", url);
        log.info("私聊消息接口post请求参数:{}", params);
        String response = HttpUtils.sendPost(url, null, HttpUtils.asUrlParams(params));
        log.info("私聊消息接口post请求返回信息:{}", response);
        log.debug("WechatRobotServiceImpl.sendPrivateMsg结束");
    }

}
