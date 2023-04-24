package com.mrl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * @Auther: MrL
 * @Date: 2023-04-19-15:55
 * @Description: com.mrl-chatgpt
 * @Version: 1.0
 */
@PropertySource(value = {"config.properties"})
@SpringBootApplication
public class ChatGPTApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ChatGPTApplication.class, args);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
