package com.mrl.service;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:46
 * @Description: com.mrl.service-chatgpt
 * @Version: 1.0
 */
public interface QqRobotService {
    JSONObject QqRobotEvenHandle(HttpServletRequest request);
    void sendPrivateMsg(String message) throws IOException;
}