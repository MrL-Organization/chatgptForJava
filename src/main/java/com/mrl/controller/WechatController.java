package com.mrl.controller;

import com.mrl.conf.ConfigurationClass;
import com.mrl.service.QqRobotService;
import com.mrl.service.WechatRobotService;
import com.mrl.util.ShaUtils;
import com.mrl.util.XMLUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private WechatRobotService wechatRobotService;

    @GetMapping()
    public String check(@RequestParam("signature")String signature,
                        @RequestParam("timestamp")String timestamp,
                        @RequestParam("nonce")String nonce,
                        @RequestParam("echostr")String echostr){

        log.info("微信sign校验入参，signature:{}, timestamp:{},nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        try {
            String tmpStr = ShaUtils.getSHA1(configurationClass.WX_SIGN_TOKEN,  timestamp,  nonce);
            log.info("检测是否匹配:tmpStr:{},signature:{}",tmpStr,signature);
            if (tmpStr.equals(signature.toUpperCase())){
                log.info("数据源为微信后台，将echostr[{}]返回！", echostr);
                return echostr;
            }
        }catch (Exception e){
            log.error("验证出错，{}",e.getMessage());
        }
        log.info("不是微信后台");
        return "不是微信后台";
    }

    @PostMapping()
    public String handle(HttpServletRequest request, HttpServletResponse response) throws IOException, DocumentException {
        log.debug("接收到微信推送：{}", request.getRequestURI());
        HashMap<String,String> result;
        Map<String,String> params = XMLUtils.xmlToMap(request);
        String MsgType = params.get("MsgType");

        if ("text".equals(MsgType)){
            //消息事件
            log.info("消息事件-接收参数：{}",params);
            result = wechatRobotService.MessageHandle(params);
            String r = XMLUtils.mapToXml(result);
            log.info("消息事件-返回结果：{}",r);
            return r;
        }else if ("event".equals(MsgType)){
            //请求事件
            //log.info("请求事件-接收参数：{}",params);
            //result = robotService.QqRobotRequestHandle(params);
            //log.info("请求事件-返回结果：{}",result);
        }


        return "";
    }

}
