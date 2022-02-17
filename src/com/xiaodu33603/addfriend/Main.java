package com.xiaodu33603.addfriend;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static String steamID, sessionID, steamLoginSecure;
    public static TimerTask tasks;
    public static Timer timer;
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static int i = 0;
    public static void main(String[] args){
        if (Start()){
            doPostBody("https://steamcommunity.com/actions/AddFriendAjax", "sessionID=" + sessionID + "&steamid=" + steamID + "&accept_invite=0");
            System.out.println("启动");
        }else Start();
    }

    public static boolean Start(){
        Scanner input = new Scanner(System.in);
        System.out.println("此工具会每隔1分钟添加对方为好友，直到添加成功等待对方同意后停止(对方好友列表满了用这个工具在合适不过了)\nsessionID和steamLoginSecure在Cookie中查看\n请输入你的sessionID：");
        sessionID = input.next();
        System.out.println("请输入你的steamLoginSecure：");
        steamLoginSecure = input.next();
        System.out.println("请输入对方的Steam64位ID：");
        steamID = input.next();
        System.out.println("输入是否正确\nsessionID：" + sessionID + "\nsteamLoginSecure：" + steamLoginSecure + "\n对方的Steam64位ID：" + steamID + "\nY/N：");
        return Objects.equals(input.next(), "y") || Objects.equals(input.next(), "Y");
    }

    public static void doPostBody(String url, String paramString) {
        tasks = new TimerTask() {
            @Override
            public void run() {
                CloseableHttpClient httpClient;
                CloseableHttpResponse httpResponse = null;
                String result;

                httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(url);
                // 配置请求参数实例
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofDays(35000))// 设置连接主机服务超时时间
                        .setConnectionRequestTimeout(Timeout.ofDays(35000))// 设置连接请求超时时间
                        .build();
                // 为httpPost实例设置配置
                httpPost.setConfig(requestConfig);
                // 设置请求头
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                httpPost.addHeader("Cookie", "sessionid=" + sessionID + "; steamLoginSecure=" + steamLoginSecure);
                // 为httpPost设置封装好的请求参数
                try {
                    httpPost.setEntity(new StringEntity(paramString, ContentType.parse("UTF-8")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    // httpClient对象执行post请求,并返回响应参数对象
                    httpResponse = httpClient.execute(httpPost);
                    // 从响应对象中获取响应内容
                    HttpEntity entity = httpResponse.getEntity();
                    result = EntityUtils.toString(entity);
                    i++;
                    Date date = new Date();
                    System.out.println(i + "次 " + df.format(date) + ": " + result);
                    if (Objects.equals(result, "false")){
                        System.out.println(df.format(date) + ": 请求失败，请重新运行程序");
                    }
                    if (httpResponse.getCode() == 200){
                        System.out.println(df.format(date) + ": 添加成功，即将停止程序");
                        tasks.cancel();
                        timer.cancel();
                        System.out.println(df.format(date) + ": 程序停止成功");
                    }
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                } finally {
                    // 关闭资源
                    if (null != httpResponse) {
                        try {
                            httpResponse.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (null != httpClient) {
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        timer = new Timer();
        timer.schedule(tasks,0, 60000);
    }
}
