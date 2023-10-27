package com.mrl.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

/**
 * @Auther: MrL
 * @Date: 2023-04-14-15:50
 * @Description: 配置类
 * @Version: 1.0
 */
@Configuration
@PropertySource(value="classpath:config.properties", encoding="UTF-8")
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



    //百度获取ACCESS_TOKEN接口地址
    @Value("${baidu.getToken.url}")
    public String BAIDU_TOKEN_URL;
    //百度服务应用ID
    @Value("${baidu.appId}")
    public String BAIDU_APP_ID;
    //百度服务应用API_KEY
    @Value("${baidu.apiKey}")
    public String BAIDU_API_KEY;
    //百度服务应用密钥
    @Value("${baidu.secretKey}")
    public String BAIDU_SECRET_KEY;
    //百度账号accessKey
    @Value("${baidu.account.accessKey}")
    public String BAIDU_ACCESS_KEY_ID;
    //百度账号secretKey
    @Value("${baidu.account.secretKey}")
    public String BAIDU_SECRET_ACCESS_KEY;
    //百度文心一言API地址
    @Value("${baidu.wenxinyiyan.url}")
    public String BAIDU_WXYY_URL;


    //百度AI作画API地址
    @Value("${baidu.aizuohua.url}")
    public String BAIDU_AIZUOHUA_URL;
    //百度AI作画获取图片API地址
    @Value("${baidu.aizuohua.getImg.URL}")
    public String BAIDU_AIZUOHUA_GETIMG_URL;
    //百度AI作画图片大小
    @Value("${baidu.aizuohua.size}")
    public String BAIDU_AIZUOHUA_SIZE;
    //百度AI作画图片数量
    @Value("${baidu.aizuohua.num}")
    public String BAIDU_AIZUOHUA_NUM;
    //百度AI作画图片风格
    @Value("${baidu.aizuohua.style}")
    public String BAIDU_AIZUOHUA_STYLE;



    //阿里云apiKey
    @Value("${aliyun.appKey}")
    public String ALIYUN_API_KEY;
    //阿里云通义千问api地址
    @Value("${aliyun.tongyiqianwen.url}")
    public String ALIYUN_TYQW_URL;


    //阿里云通义万象api地址
    @Value("${aliyun.tongyiwanxiang.url}")
    public String ALIYUN_TYWX_URL;
    //阿里云通义万象获取图片地址
    @Value("${aliyun.tongyiwanxiang.getImg.url}")
    public String ALIYUN_TYWX_GETIMG_URL;
    //通义万象生成图片的风格
    @Value("${aliyun.tongyiwanxiang.style}")
    public String ALIYUN_TYWX_STYLE;
    //通义万象生成的图片数量
    @Value("${aliyun.tongyiwanxiang.num}")
    public String ALIYUN_TYWX_NUM;
    //通义万象生成的图片大小
    @Value("${aliyun.tongyiwanxiang.size}")
    public String ALIYUN_TYWX_SIZE;


    //wol的唤醒ip和mac
    @Value("${wol.ip}")
    public String WOL_IP;
    @Value("${wol.mac}")
    public String WOL_MAC;
    @Value("${wol.mask}")
    public String WOL_MASK;
    @Value("${wol.user}")
    public String WOL_USER;
    @Value("${wol.password}")
    public String WOL_PASSWORD;

    //微信验证签名token
    @Value("${wechat.sign.token}")
    public String WX_SIGN_TOKEN;




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
