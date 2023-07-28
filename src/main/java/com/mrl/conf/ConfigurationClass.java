package com.mrl.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Auther: MrL
 * @Date: 2023-04-14-15:50
 * @Description: 配置类
 * @Version: 1.0
 */
@Configuration
@Slf4j
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


    //openai聊天使用的语言模型
    @Value("${openAI.chat.model}")
    public String OPENAI_CHAT_MODEL;
    //openai聊天时生成信息的最大tokens
    @Value("${openAI.chat.max_tokens}")
    public String OPENAI_CHAT_MAX_TOKENS;


    //openai生成文本时生成信息的最大tokens
    @Value("${openAI.text.max_tokens}")
    public String OPENAI_TEXT_MAX_TOKENS;
    //openai生成文本使用的语言模型
    @Value("${openAI.text.model}")
    public String OPENAI_TEXT_MODEL;


    //openai生成图片的数量
    @Value("${openAI.img.num}")
    public String OPENAI_IMG_NUM;
    //openai生成图片的大小
    @Value("${openAI.img.size}")
    public String OPENAI_IMG_SIZE;
    //openai生成图片返回的格式 url,b64_json
    @Value("${openAI.img.responseFormat}")
    public String OPENAI_IMG_FORMAT;

    //百度服务应用ID
    @Value("${baidu.appId}")
    public String APP_ID;

    //百度服务应用API_KEY
    @Value("${baidu.apiKey}")
    public String API_KEY;

    //百度服务应用密钥
    @Value("${baidu.secretKey}")
    public String SECRET_KEY;

    //百度账号accessKey
    @Value("${baidu.account.accessKey}")
    public String ACCESS_KEY_ID;

    //百度账号secretKey
    @Value("${baidu.account.secretKey}")
    public String SECRET_ACCESS_KEY;

    /**
     * 测试配置文件的必输配置项是否为空
     */
    @PostConstruct
    private void testParam(){
        if("".equals(CQHTTP_URL)){
            log.error("cqhttp的访问地址为空");
            System.exit(0);
        }
        /*if("".equals(CQHTTP_USERID)){
            throw new RuntimeException("cqhttp的userid为空！");
        }*/
        if("".equals(ACCESS_TOKEN)){
            log.error("cqhttp的ACCESS_TOKEN为空");
            System.exit(0);
        }
        if("".equals(OPENAI_DOMAIN)){
            log.error("openai的访问地址为空");
            System.exit(0);
        }
        if("".equals(OPENAI_KEY)){
            log.error("openai的key为空");
            System.exit(0);
        }
        if("".equals(OPENAI_PROTOCOL)){
            log.error("openai的访问协议为空");
            System.exit(0);
        }
        if("".equals(OPENAI_CHAT_MODEL)){
            log.error("openai的聊天模型为空");
            System.exit(0);
        }
        /*if("".equals(OPENAI_CHAT_MAX_TOKENS)){
            throw new RuntimeException("openai的聊天最大token为空！");
        }*/
        /*if("".equals(OPENAI_TEXT_MAX_TOKENS)){
            throw new RuntimeException("openai的文本最大token为空！");
        }*/
        if("".equals(OPENAI_TEXT_MODEL)){
            log.error("openai的文本模型为空");
            System.exit(0);
        }
        /*if("".equals(OPENAI_IMG_NUM)){
            throw new RuntimeException("openai的图片生成数量为空！");
        }*/
        /*if("".equals(OPENAI_IMG_SIZE)){
            throw new RuntimeException("openai的图片生成大小为空！");
        }*/
        /*if("".equals(OPENAI_IMG_FORMAT)){
            throw new RuntimeException("openai的图片生成格式为空！");
        }*/
    }
}
