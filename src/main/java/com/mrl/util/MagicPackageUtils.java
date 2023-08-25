package com.mrl.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;


/**
 * @Title: 魔术包工具类
 * @Description: 用于WAL
 * @author: MrL
 * @date: 2023/8/25 14:02
 */
@Slf4j
public class MagicPackageUtils {

    public static void main(String[] args) {
        // 10.0.50.138  D8-9E-F3-88-B6-3C
        // 10.0.50.129  66-00-6A-8F-1D-64
        // 10.0.50.186  D8-9E-F3-95-AC-74
        // 单播
//        sendMagicPackage("10.0.50.186", "D8-9E-F3-95-AC-74");

        // 广播，需要先根据子网掩码和ip得到主机的广播地址 08-BF-B8-A4-3F-49
        String broadcastAddress = getBroadcastAddress("10.0.50.186", "255.255.255.0");
        System.out.println(broadcastAddress);

    }

    /**
     * @param mac
     * @Title: 组装魔术包数据
     * @MethodName: assembleMagicData
     * @Return java.lang.String
     * @Exception
     * @Description: 魔术包是用16进制表示的数据包，它由固定的前缀数据以及固定重复次数的目标主机MAC地址所组成。
     * 所谓固定前缀数据即6对“FF”，所谓固定重复次数即16次，也就是说魔术包是由12个“F”加重复16次的主机MAC地址组成，例如本文所用试验机的MAC地址为“66-00-6A-8F-1D-64”，
     * 所以使该机远程开机的魔术包为：
     * String magicPacageData = "FFFFFFFFFFFF" +
     * "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" +
     * "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" +
     * "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" +
     * "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64" + "64006A8F1D64";
     * <p>
     * 在Windows系统中，主机的MAC地址可以通过在命令窗口中输入“ipconfig -all”命令查看。
     * @author: FLY
     * @date: 2021/1/11 18:02
     */
    private static String assembleMagicData(String mac) {

        String macR = null;
        if (mac.contains("-")) {
            macR = mac.replaceAll("-", "");
        } else if (mac.contains(":")) {
            macR = mac.replaceAll(":", "");
        }
        String repeatedStr = createRepeatedStr(macR, 16);
        return "FFFFFFFFFFFF" + repeatedStr;
    }


    /**
     * @param ip
     * @param mac
     * @Title: 远程开机
     * @MethodName: sendMagicPackage
     * @Return void
     * @Exception
     * @Description: 发送UDP广播，实现远程开机
     * @author: FLY
     * @date: 2021/1/11 17:19
     */
    public static String sendMagicPackage(String ip, String mac) {
        if (StringUtils.isBlank(ip)
                || StringUtils.isBlank(mac)) {
            return "ip和mac不能为空！";
        }
        //端口号
        int port = 9;
        String magicPacageData = assembleMagicData(mac);
        log.info("【魔包远程开机】，IP地址：{}，MAC地址：{}，魔术包数据：{}", ip, mac, magicPacageData);
        // mac地址转换为2进制的魔术包数据
        byte[] command = hexStr2Byte(magicPacageData);

        //广播魔术包
        try {
            //1.获取ip地址
            InetAddress address = InetAddress.getByName(ip);
            //2.获取广播socket
            MulticastSocket socket = new MulticastSocket(port);
            //3.封装数据包
            /*public DatagramPacket(byte[] buf,int length
             *      ,InetAddress address
             *      ,int port)
             * buf：缓存的命令
             * length：每次发送的数据字节数，该值必须小于等于buf的大小
             * address：广播地址
             * port：广播端口
             */
            DatagramPacket packet = new DatagramPacket(command, command.length, address, port);
            //4.发送数据
            socket.send(packet);
            //5.关闭socket
            socket.close();
        } catch (UnknownHostException e) {
            //Ip地址错误时候抛出的异常
            log.error("【魔包远程开机】异常-Ip地址错误，IP地址：{}，MAC地址：{}，异常信息：{}", ip, mac, e);
            return "远程开机异常，ip地址错误，ip:" + ip;

        } catch (IOException e) {
            //获取socket失败时候抛出的异常
            log.error("【魔包远程开机】异常-获取socket失败，IP地址：{}，MAC地址：{}，异常信息：{}", ip, mac, e);
            return "远程开机异常，ip:" + ip + ",mac:" + mac + ",异常信息：" + e.getMessage();
        }
        return "远程开机成功，请稍后查看电脑启动状态。";
    }

    /**
     * @param hex
     * @Title: 将16进制字符串转换为用byte数组表示的二进制形式
     * @MethodName: hexStr2Byte
     * @Return byte[]
     * @Exception
     * @Description:
     * @author: FLY
     * @date: 2021/1/11 17:22
     */
    private static byte[] hexStr2Byte(String hex) {

        ByteBuffer bf = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            String hexStr = String.valueOf(hex.charAt(i));
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            bf.put(b);
        }
        return bf.array();
    }

    /**
     * @param seed
     * @param n
     * @Title: 将一个字符串重复n次
     * @MethodName: createRepeatedStr
     * @Return java.lang.String
     * @Exception
     * @Description:
     * @author: FLY
     * @date: 2021/1/11 17:22
     */
    private static String createRepeatedStr(String seed, int n) {
        return String.join("", Collections.nCopies(n, seed));
    }

    /**
     * @param ip
     * @param subnetMask 子网掩码
     * @Title: 根据子网掩码和ip得到主机的广播地址
     * @MethodName: getBroadcastAddress
     * @Return java.lang.String
     * @Exception
     * @Description:
     * @author: FLY
     * @date: 2021/1/12 19:02
     */
    public static String getBroadcastAddress(String ip, String subnetMask) {

        String ipBinary = toBinary(ip);
        String subnetBinary = toBinary(subnetMask);
        String broadcastBinary = getBroadcastBinary(ipBinary, subnetBinary);
        String wholeBroadcastBinary = spiltBinary(broadcastBinary);
        return binaryToDecimal(wholeBroadcastBinary);
    }


    /**
     * @param wholeBroadcastBinary
     * @Title: 二进制的ip字符串转十进制
     * @MethodName: binaryToDecimal
     * @Return java.lang.String
     * @Exception
     * @Description:
     * @author: FLY
     * @date: 2021/1/12 19:03
     */
    private static String binaryToDecimal(String wholeBroadcastBinary) {

        String[] strings = wholeBroadcastBinary.split("\\.");
        StringBuilder sb = new StringBuilder(40);
        for (int j = 0; j < strings.length; j++) {
            String s = Integer.valueOf(strings[j], 2).toString();
            sb.append(s).append(".");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    /**
     * @param broadcastBinary
     * @Title: 按8位分割二进制字符串
     * @MethodName: spiltBinary
     * @Return java.lang.String
     * @Exception
     * @Description:
     * @author: FLY
     * @date: 2021/1/12 19:03
     */
    private static String spiltBinary(String broadcastBinary) {

        StringBuilder stringBuilder = new StringBuilder(40);
        char[] chars = broadcastBinary.toCharArray();
        int count = 0;
        for (int j = 0; j < chars.length; j++) {
            if (count == 8) {
                stringBuilder.append(".");
                count = 0;
            }
            stringBuilder.append(chars[j]);
            count++;
        }
        return stringBuilder.toString();
    }

    //得到广播地址的二进制码
    private static String getBroadcastBinary(String ipBinary, String subnetBinary) {
        int i = subnetBinary.lastIndexOf('1');
        StringBuilder broadcastIPBinary = new StringBuilder(ipBinary.substring(0, i + 1));
        for (int j = broadcastIPBinary.length(); j < 32; j++) {
            broadcastIPBinary.append("1");
        }
        return broadcastIPBinary.toString();
    }

    //转二进制
    private static String toBinary(String content) {
        String binaryString = "";
        String[] ipSplit = content.split("\\.");
        for (String split : ipSplit) {
            String s = Integer.toBinaryString(Integer.parseInt(split));
            int length = s.length();
            for (int i = length; i < 8; i++) {
                s = "0" + s;
            }
            binaryString = binaryString + s;
        }
        return binaryString;
    }


}
