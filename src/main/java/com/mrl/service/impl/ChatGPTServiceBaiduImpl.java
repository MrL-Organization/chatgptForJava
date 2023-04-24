package com.mrl.service.impl;

import com.mrl.bean.Message;
import com.mrl.service.ChatGPTService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: MrL
 * @Date: 2023-04-24-12:14
 * @Description: com.mrl.service.impl-chatgptForJava
 * @Version: 1.0
 */
@Service
public class ChatGPTServiceBaiduImpl implements ChatGPTService {
    @Override
    public String answerQuestion(String prompt) {
        return null;
    }

    @Override
    public ArrayList<String> generatIMG(String prompt) {
        return null;
    }

    @Override
    public String queryBalance() {
        return null;
    }

    @Override
    public String chat(List<Message> message) {
        return null;
    }
}
