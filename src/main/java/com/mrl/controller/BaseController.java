package com.mrl.controller;

import com.mrl.bean.Result;
import com.mrl.conf.ConfigurationClass;
import com.mrl.util.MagicPackageUtils;
import com.mrl.util.SheelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2025-03-18-15:12
 * @Description: com.mrl.controller-base
 * @Version: 1.0
 */
@RestController
@RequestMapping("/base")
@Slf4j
public class BaseController {

    @Resource
    ConfigurationClass configurationClass;

    @RequestMapping("/openPc")
    public Result openPc(){
        Result result = new Result();
        try {
            String broadcastAddress = MagicPackageUtils.getBroadcastAddress(configurationClass.WOL_IP,configurationClass.WOL_MASK);
            String message = MagicPackageUtils.sendMagicPackage(broadcastAddress,configurationClass.WOL_MAC);
            result.successResult(message);
        }catch (Exception e){
            result.failResult(e.getMessage());
        }
        return result;
    }

    @RequestMapping("/closePc")
    public Result closePc(){
        Result result = new Result();
        String message = "";
        try {
            if(SheelUtils.login(configurationClass.WOL_IP, configurationClass.WOL_USER, configurationClass.WOL_PASSWORD)) {
                String execute = SheelUtils.execCommand( "shutdown -s -t 60");
                SheelUtils.close();
                message = "指令发送成功，不出意外电脑将在60s后关闭，返回结果：" + execute;
            }else {
                message = "登录ssh失败！";
            }
            result.successResult(message);
        }catch (Exception e){
            result.failResult(e.getMessage());
        }
        return result;
    }

}
