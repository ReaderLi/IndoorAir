package liruide.scu.com.indoorair.WebService;

/**
 * Created by LRD on 2017/11/27.
 */

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LRD on 2017/4/19.
 */

public class LoginWebService {


    //请求 URL: http://47.95.213.176:3000/login
    private static String IP = "47.95.213.176:3000";
    private static String TAG="LoginWebService";
    //把TOMCATURL改为你的服务地址

    /**
     * 通过Get方式获取HTTP服务器数据
     *
     * @return
     */
    public static String executeHttpGet(String url,String username, String password ) {
        HttpURLConnection conn = null;
        InputStream is = null;

        Log.i(TAG,"executeHttpGet");
        try {
            // 用户名 密码
            // URL 地址
            //String path = "http://kamin.mynatapp.cc/";
            String path = "http://"+IP+"/login";
            path = path + "?username=" + username + "&password=" + password;

            Log.i(TAG,"my path is "+path);

            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(3000); // 设置超时时间
            conn.setReadTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET"); // 设置获取信息方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

            Log.i(TAG,"getResponseCode");


            int num1 = conn.getResponseCode();
            Log.i(TAG,"返回结果是 "+num1);

            if (num1 == 200) {
                //得到网络返回的输入流
                is = conn.getInputStream();
                return parseInfo(is);
            }

            String iss = "返回状态不正常"+num1;

            return iss;

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 意外退出时进行连接关闭保护
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "服务器连接超时...";
    }

    // 将输入流转化为 String 型
    private static String parseInfo(InputStream inStream) throws Exception {
        byte[] data = read(inStream);
        // 转化为字符串
        return new String(data, "UTF-8");
    }

    // 将输入流转化为byte型
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }
}
