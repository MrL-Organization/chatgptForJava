package com.mrl.service;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-10-27-14:59
 * @Description: com.mrl.service-chatgptForJava
 * @Version: 1.0
 */
public interface WechatRobotService {
    HashMap<String,String> MessageHandle(Map<String,String> params);
    HashMap<String,Object> EventHandle(Map<String,String> params);
    void sendPrivateMsg(String message, String userId);
}
