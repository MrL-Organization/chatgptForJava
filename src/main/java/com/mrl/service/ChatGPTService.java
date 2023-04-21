package com.mrl.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-19-16:03
 * @Description: com.mrl.service-chatgpt
 * @Version: 1.0
 */
public interface ChatGPTService {
    public JSONObject sendToAI(String message);
    public JSONObject queryBalance();
}
