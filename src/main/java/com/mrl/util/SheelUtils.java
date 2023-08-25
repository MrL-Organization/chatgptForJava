package com.mrl.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Auther: MrL
 * @Date: 2023-08-25-15:20
 * @Description: com.mrl.util-chatgptForJava
 * @Version: 1.0
 */
@Slf4j
public class SheelUtils {
    private static Session session = null;
    private static int port = 22;
    private static int timeout = 5 * 1000;

    public static void main(String[] args) {
        String ip = "10.147.20.33";
        String user = "root";
        String password = "password";
        if(login(ip, user, password)) {
            //String cmd = "shutdown -s -t 0";
            String cmd = "dir";
            String s = execCommand(cmd);
            System.out.println(s);
            close();
        }
    }

    public static boolean login(String host, String username, String password) {
        log.info("ssh登录，host:{},username:{},password:{}",host,username,password);
        JSch jSch = new JSch();
        // 1. 获取 ssh session
        try {
            session = jSch.getSession(username, host, port);
            session.setPassword(password);
            session.setTimeout(timeout);
            session.setConfig("StrictHostKeyChecking", "no");
            //解决Algorithm negotiation fail
            //session.setConfig("kex", session.getConfig("kex") + ",diffie-hellman-group14-sha1");
            session.setConfig("kex", session.getConfig("kex") + ",diffie-hellman-group1-sha1");
            session.connect();  // 获取到 ssh session
            return true;
        } catch (JSchException e) {
            log.error("ssh登陆失败！错误信息：{}",e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String execCommand(String cmd) {
        if (session == null) return "请先登录！";
        ChannelExec channelExec = null;
        BufferedReader inputStreamReader = null;
        BufferedReader errInputStreamReader = null;
        StringBuilder runLog = new StringBuilder("");
        StringBuilder errLog = new StringBuilder("");
        try {
            // 2. 通过 exec 方式执行 shell 命令
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.connect();  // 执行命令
            // 3. 获取标准输入流
            inputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
            // 4. 获取标准错误输入流
            errInputStreamReader = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));
            // 5. 记录命令执行 log
            String line = null;
            while ((line = inputStreamReader.readLine()) != null) {
                runLog.append(line).append("\n");
            }
            // 6. 记录命令执行错误 log
            String errLine = null;
            while ((errLine = errInputStreamReader.readLine()) != null) {
                errLog.append(errLine).append("\n");
            }
            // 7. 输出 shell 命令执行日志
            log.info("exitStatus=" + channelExec.getExitStatus() + ", openChannel.isClosed="
                    + channelExec.isClosed());
            log.info("命令执行完成，执行日志如下: {}",runLog.toString());
            //log.error("命令执行完成，执行错误日志如下:{}",errLog.toString());
        } catch (Exception e) {
            log.error("ssh命令执行错误:{}",e.getMessage());
            e.printStackTrace();
            return "ssh命令执行错误,错误信息：" + e.getMessage();
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (errInputStreamReader != null) {
                    errInputStreamReader.close();
                }
                if (channelExec != null) {
                    channelExec.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return runLog.toString();
    }

    public static void close() {
        if (session != null) {
            session.disconnect();
        }
    }
}
