package com.haochen.chatgptclientserver.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * <p>模块说明：</p>
 *
 * <p>修改历史：</p>
 * create_time: 2023/4/8
 *
 * @author chenhao
 * date 2023/4/8
 **/
public interface SseChatService {

    /**
     * 使用SSE进行聊天
     *
     * @param uid     传输uid则表示本次聊天，用以区分上下文
     * @param message 本次消息
     * @return sse信息
     */
    SseEmitter chat(String uid, String message) throws IOException;

}
