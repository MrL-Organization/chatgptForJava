package com.mrl.bean;

import lombok.Data;

/**
 * @Auther: MrL
 * @Date: 2023-04-23-9:06
 * @Description: 和chatgpt聊天的消息类
 * @Version: 1.0
 */
@Data
public class Message {
    String role;
    String content;
}
