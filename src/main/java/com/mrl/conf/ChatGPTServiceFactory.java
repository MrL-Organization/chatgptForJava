package com.mrl.conf;

import com.mrl.service.ChatGPTService;
import com.mrl.service.impl.ChatGPTServiceAliImpl;
import com.mrl.service.impl.ChatGPTServiceBaiduImpl;
import com.mrl.service.impl.ChatGPTServiceOpenAIImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @Auther: MrL
 * @Date: 2023-04-24-11:31
 * @Description: ai服务工厂类
 * @Version: 1.0
 */

@Component
public class ChatGPTServiceFactory {

    @Value("${server.choose}")
    private String SERVER_CHOOSE;

    @Resource
    private ChatGPTServiceOpenAIImpl chatGPTServiceOpenAI;

    @Resource
    private ChatGPTServiceBaiduImpl chatGPTServiceBaidu;

    @Resource
    private ChatGPTServiceAliImpl chatGPTServiceAli;

    private ChatGPTServiceFactory(){}

    public ChatGPTService getChatGPTService() {
        if ("1".equals(SERVER_CHOOSE) || "".equals(SERVER_CHOOSE)){
            return chatGPTServiceOpenAI;
        }else if ("2".equals(SERVER_CHOOSE)){
            return chatGPTServiceBaidu;
        }else if ("3".equals(SERVER_CHOOSE)){
            return chatGPTServiceAli;
        }
        return null;
    }

    public String getServerChoose() {
        return SERVER_CHOOSE;
    }

    public boolean isTaskImg() {
        return "2".equals(SERVER_CHOOSE) || "3".equals(SERVER_CHOOSE);
    }


}
