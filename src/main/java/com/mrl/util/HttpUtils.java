package com.mrl.util;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Auther: MrL
 * @Date: 2023-04-19-16:18
 * @Description: com.mrl.util-chatgpt
 * @Version: 1.0
 */
public class HttpUtils {
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param headers   发送请求头
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url,HashMap<String,String> headers,String param) throws IOException,ConnectException {
        String result = "";
        BufferedReader in = null;
        try {
            if(param != null && !"".equals(param)){
                url = url + "?" + param;
            }
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //设置请求头
            if (headers != null && headers.size() > 0){
                for (String key : headers.keySet()){
                    conn.setRequestProperty(key,headers.get(key));
                }
            }
            // 建立实际的连接
            conn.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            System.out.println("发送GET请求出现异常！" + e);
            throw e;
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数只能是 name1=value1&name2=value2。
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url,String param) throws IOException,ConnectException {
        return sendGet(url,null,param);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url   发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url) throws IOException,ConnectException {
        return sendGet(url,null,null);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param headers   发送请求头
     * @param param 请求参数，请求参数可以是 name1=value1&name2=value2,也可以是json格式字符串。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url,HashMap<String,String> headers,String param) throws IOException, ConnectException {

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //设置请求头
            if (headers != null && headers.size() > 0){
                for (String key : headers.keySet()){
                    conn.setRequestProperty(key,headers.get(key));
                }
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //参数不为空
            if (param != null && !"".equals(param)) {
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            throw e;
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数可以是 name1=value1&name2=value2,也可以是json格式字符串。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url,String param) throws IOException,ConnectException {
        return sendPost(url,null,param);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url) throws IOException,ConnectException {
        return sendPost(url,null,null);
    }

    /**
     * asUrlParams方法慨述: 将map转化key=123&v=456格式 为只要确保你的编码输入是正确的,就可以忽略掉
     */
    public static String asUrlParams(Map<String, String> source) {
        Iterator<String> it = source.keySet().iterator();
        StringBuilder paramStr = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            String value = source.get(key);
            if (value == null || "".equals(value)) {
                continue;
            }
            try {
                // URL 编码
                value = URLEncoder.encode(value, "utf-8");
            } catch (UnsupportedEncodingException e) {
                // do nothing
            }
            paramStr.append("&").append(key).append("=").append(value);
        }
        // 去掉第一个&
        return paramStr.substring(1);
    }


/*    public static void main(String[] args) {
//        //发送 GET 请求
//        String s=HttpRequest.sendGet("http://localhost:6144/Home/RequestString", "key=123&v=456");
//        System.out.println(s);

        // 发送 POST 请求
        String param = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"Hello!\"}]\n" +
                "  }";
        HashMap headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer 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");
        String url = "https://api.openai-proxy.com/v1/chat/completions";
        String sr = HttpUtils.sendPost(url, headers,param);
        System.out.println(sr);
    }*/

}
