package com.mrl.service;

import com.mrl.bean.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: MrL
 * @Date: 2023-04-19-16:03
 * @Description: com.mrl.service-chatgpt
 * @Version: 1.0
 */
public interface ChatGPTService {
    String answerQuestion(String prompt);
    ArrayList<String> generatIMG(String prompt);
    String queryBalance();
    String chat(List<Message> message);
}
