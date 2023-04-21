package com.mrl.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther: MrL
 * @Date: 2023-04-14-15:50
 * @Description: 配置类
 * @Version: 1.0
 */
@Configuration
public class ConfigurationClass {
    //cqhttp的访问url
    @Value("${cqhttp.url}")
    public String CQHTTP_URL;

    //cqhttp私聊消息默认发送用户
    @Value("${cqhttp.userId}")
    public String CQHTTP_USERID;

    //cqhttp的token
    @Value("${cqhttp.access_token}")
    public String ACCESS_TOKEN;

    //openai代理域名
    @Value("${openAI.domain}")
    public String OPENAI_DOMAIN;

    //openaikey
    @Value("${openAI.key}")
    public String OPENAI_KEY;

    //openai域名协议
    @Value("${openAI.protocol}")
    public String OPENAI_PROTOCOL;

    //openai使用的语言模型
    @Value("${openAI.model}")
    public String OPENAI_MODEL;

    //openai生成图片的数量
    @Value("${openAI.img.num}")
    public int OPENAI_IMG_NUM;

    //openai生成图片的大小
    @Value("${openAI.img.size}")
    public String OPENAI_IMG_SIZE;

    //openai生成图片返回的格式 url,b64_json
    @Value("${openAI.img.responseFormat}")
    public String OPENAI_IMG_FORMAT;

}
