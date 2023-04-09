package com.haochen.chatgptclientserver.config;

import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>模块说明：</p>
 *
 * <p>修改历史：</p>
 * create_time: 2023/4/8
 *
 * @author chenhao
 * date 2023/4/8
 **/
@Configuration
public class OpenAiClientConfiguration {

    @Value("${openai.chat.apiKey}")
    private List<String> apiKey;

    @Value("${openai.chat.apiHost}")
    private String apiHost;

    @Value("${openai.chat.proxy.enabled:false}")
    private boolean proxyEnabled = false;

    @Value("${openai.chat.proxy.host}")
    private String proxyHost;

    @Value("${openai.chat.proxy.port}")
    private int proxyPort;

    @Bean
    public OpenAiStreamClient openAiStreamClient() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS);
        if (proxyEnabled) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            okHttpClientBuilder.proxy(proxy);
        }
        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        return OpenAiStreamClient
                .builder()
                .apiHost(apiHost)
                .apiKey(apiKey)
                //自定义key使用策略 默认随机策略
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .build();
    }
}
