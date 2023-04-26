package com.mrl.service;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:46
 * @Description: com.mrl.service-chatgpt
 * @Version: 1.0
 */
public interface QqRobotService {
    HashMap<String,Object> QqRobotMessageHandle(JSONObject jsonParam) throws IOException;
    HashMap<String,Object> QqRobotRequestHandle(JSONObject jsonParam) throws IOException;
    void sendPrivateMsg(String message, String user_id) throws IOException;
    void agreeFriendRequest(String flag,boolean approve,String remark) throws IOException;
}