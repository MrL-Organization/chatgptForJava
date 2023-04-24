package com.mrl.service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.ConnectException;

/**
 * @Auther: MrL
 * @Date: 2023-04-20-10:46
 * @Description: com.mrl.service-chatgpt
 * @Version: 1.0
 */
public interface QqRobotService {
    void QqRobotEvenHandle(HttpServletRequest request) throws IOException;
    void sendPrivateMsg(String message, String user_id) throws IOException, ConnectException;
}