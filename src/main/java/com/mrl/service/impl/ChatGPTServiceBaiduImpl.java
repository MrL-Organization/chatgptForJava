package com.mrl.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.billing.BillingClient;
import com.baidubce.services.billing.BillingClientConfiguration;
import com.baidubce.services.billing.model.finance.UserBalanceQueryResponse;
import com.mrl.bean.Message;
import com.mrl.conf.ConfigurationClass;
import com.mrl.service.ChatGPTService;
import com.mrl.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: MrL
 * @Date: 2023-04-24-12:14
 * @Description: com.mrl.service.impl-chatgptForJava
 * @Version: 1.0
 */
@Service
@Slf4j
public class ChatGPTServiceBaiduImpl implements ChatGPTService {

    //配置文件类
    @Resource
    ConfigurationClass configurationClass;

    private String ACCESS_TOKEN;


    @Override
    public String answerQuestion(String prompt) {
        log.debug("ChatGPTService.answerQuestion开始,参数prompt:{}",prompt);
        //返回结果
        String result;
        //接口响应信息
        JSONObject responseObject;
        if (ACCESS_TOKEN == null || "".equals(ACCESS_TOKEN)) {
            setAccessToken();
        }
        String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + ACCESS_TOKEN;
        log.info("文心一言回答问题请求地址:{}",url);
        HashMap<String,Object> params = new HashMap<>();
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user",prompt));
        params.put("messages",messages);
        log.info("文心一言回答问题请求参数:{}",params);
        try{
            String response = HttpUtils.sendPost(url, JSON.toJSONString(params));
            responseObject = JSONObject.parseObject(response);
            result = responseObject.getString("result");
        }catch (Exception e){
            log.error("文心一言回答问题出错：{}",e.getMessage());
            result = "文心一言回答问题出错：" + e.getMessage();
            setAccessToken();
        }
        log.debug("ChatGPTService.answerQuestion结束");
        return result;
    }

    @Override
    public ArrayList<String> generatIMG(String prompt) {
        return null;
    }

    @Override
    public String queryBalance() {
        BillingClientConfiguration config = new BillingClientConfiguration();
        config.setCredentials(new DefaultBceCredentials(configurationClass.ACCESS_KEY_ID, configurationClass.SECRET_ACCESS_KEY));
        config.setEndpoint("https://billing.baidubce.com");
        BillingClient client = new BillingClient(config);
        UserBalanceQueryResponse response = client.userBalanceQuery();
        BigDecimal balance = response.getCashBalance();
        return "百度文心一言余额还有"+balance+"元。";
    }

    @Override
    public String chat(List<Message> messages) {
        log.debug("ChatGPTService.chat开始,messages:{}",messages);
        //返回结果
        String result;
        //接口响应信息
        JSONObject responseObject;
        if (ACCESS_TOKEN == null || "".equals(ACCESS_TOKEN)) {
            setAccessToken();
        }
        String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + ACCESS_TOKEN;
        log.info("文心一言回答问题请求地址:{}",url);
        HashMap<String,Object> params = new HashMap<>();
        params.put("messages",messages);
        log.info("文心一言回答问题请求参数:{}",params);
        try{
            String response = HttpUtils.sendPost(url, JSON.toJSONString(params));
            log.info("文心一言回答问题返回结果:{}",response);
            responseObject = JSONObject.parseObject(response);
            result = responseObject.getString("result");
        }catch (Exception e){
            log.error("文心一言回答问题出错：{}",e.getMessage());
            result = "文心一言回答问题出错：" + e.getMessage();
            setAccessToken();
        }
        log.debug("ChatGPTService.chat结束");
        return result;
    }

    private void setAccessToken()  {
        log.info("开始获取access_token");
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        HashMap<String,Object> params = new HashMap<>();
        params.put("grant_type","client_credentials");
        params.put("client_id",configurationClass.API_KEY);
        params.put("client_secret",configurationClass.SECRET_KEY);
        try{
            String response = HttpUtils.sendGet(url, HttpUtils.asUrlParams(params));
            log.debug("获取ACCESS_TOKEN请求结果：{}",response);
            ACCESS_TOKEN = JSON.parseObject(response).getString("access_token");
            log.info("设置ACCESS_TOKEN:{}",ACCESS_TOKEN);
        }catch (Exception e){
            log.error("获取ACCESS_TOKEN失败，原因：{}",e.getMessage());
        }

    }
}
