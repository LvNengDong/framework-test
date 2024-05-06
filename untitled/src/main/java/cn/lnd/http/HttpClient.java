package cn.lnd.http;

import okhttp3.*;

import java.io.IOException;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/19 16:49
 */
public class HttpClient {

    public static void main(String[] args) throws IOException {
        //第一步需要创建一个OkHttpClient的实例
        OkHttpClient client = new OkHttpClient();

        //第二步创建request对象
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://spa.corp.qunar.com/spa/days/preSale/calendar").newBuilder();
        urlBuilder.addQueryParameter("d1", "2023-05-19");
        urlBuilder.addQueryParameter("d2", "2023-05-21");
        urlBuilder.addQueryParameter("hotelId", "4179271");
        urlBuilder.addQueryParameter("roomId", "1394142752");
        urlBuilder.addQueryParameter("st", "HOTELCALENDAR");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        //第三步调用OkHttpClient的newCall() 方法来创建一个Call 对象，并调用它的execute() 方法来发送请求并获取服务器返回的数据
        //其中Response 对象就是服务器返回的数据
        Response response = client.newCall(request).execute();
        String responseData = response.body().string();
        System.out.println(responseData);
    }

}
