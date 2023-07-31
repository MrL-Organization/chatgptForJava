package com.mrl.service;

import java.util.ArrayList;

/**
 * @Auther: MrL
 * @Date: 2023-07-31-19:03
 * @Description: com.mrl.service-chatgptForJava
 * @Version: 1.0
 */
public interface TaskImgChatGPTService extends ChatGPTService{

    ArrayList<String> getImg(String taskId);

    String getImgTaskStatus(String taskId);

}
