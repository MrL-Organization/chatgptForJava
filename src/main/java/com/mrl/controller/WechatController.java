package com.mrl.controller;

import com.mrl.conf.ConfigurationClass;
import com.mrl.util.ShaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: MrL
 * @Date: 2023-10-27-11:29
 * @Description: com.mrl.controller-chatgptForJava
 * @Version: 1.0
 */
@RestController()
@RequestMapping("/wechat")
@Slf4j
public class WechatController {

    @Autowired
    ConfigurationClass configurationClass;

    @RequestMapping("/check")
    public String check(@RequestParam("signature")String signature,
                        @RequestParam("timestamp")String timestamp,
                        @RequestParam("nonce")String nonce,
                        @RequestParam("echostr")String echostr){

        log.info("微信sign校验入参，signature:{}, timestamp:{},nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        try {
            String tmpStr = ShaUtils.getSHA1(configurationClass.WX_SIGN_TOKEN,  timestamp,  nonce);
            log.info("检测是否匹配:tmpStr:{},signature:{}",tmpStr,signature);
            if (signature != null && signature.equals(tmpStr)){
                log.info("数据源为微信后台，将echostr[{}]返回！", echostr);
                return echostr;
            }
        }catch (Exception e){
            log.error("验证出错，{}",e.getMessage());
        }
        log.info("不是微信后台");
        return "不是微信后台";
    }

}
