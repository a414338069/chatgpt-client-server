package com.haochen.chatgptclientserver.service.impl;

import com.haochen.chatgptclientserver.listener.OpenAISSEEventSourceListener;
import com.haochen.chatgptclientserver.service.RecordService;
import com.haochen.chatgptclientserver.service.SseChatService;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>模块说明：</p>
 *
 * <p>修改历史：</p>
 * create_time: 2023/4/8
 *
 * @author chenhao
 * date 2023/4/8
 **/
@Service
@Slf4j
public class SseChatServiceImpl implements SseChatService {

    /**
     * 超时时间
     */
    @Value("${sse.config.timeout:30}")
    private long sseConnectionTimeout = 30;

    /**
     * 最大的上下文数量
     */
    @Value("${openai.chat.max-context:10}")
    private int maxContextSize = 10;

    private final RecordService recordService;

    private final OpenAiStreamClient openAiStreamClient;

    public SseChatServiceImpl(RecordService recordService, OpenAiStreamClient openAiStreamClient) {
        this.recordService = recordService;
        this.openAiStreamClient = openAiStreamClient;
    }

    @Override
    public SseEmitter chat(String uid, String message) throws IOException {
        Assert.hasText(uid, "uid不能为空！");
        Assert.hasText(message, "请输入您的问题");
        SseEmitter sseEmitter = initSse(uid);
        List<Message> messages = recordService.getRecord(uid);
        Message currentMessage = Message.builder().role(Message.Role.USER).content(message).build();
        messages.add(currentMessage);
        messages = limitMaxContextHandle(messages);
        sseEmitter.send(SseEmitter.event()
                .id(uid)
                .name("正在进行询问，请稍后...")
                .data(LocalDateTime.now())
                .reconnectTime(3000));
        recordService.saveRecord(uid, messages);
        OpenAISSEEventSourceListener openAIEventSourceListener = new OpenAISSEEventSourceListener(sseEmitter, uid, recordService);
        openAiStreamClient.streamChatCompletion(messages, openAIEventSourceListener);
        return sseEmitter;
    }

    private SseEmitter initSse(String uuid) {
        long sseTimeOut = sseConnectionTimeout * 1000;
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(sseTimeOut);
        sseEmitter.onCompletion(() -> log.info(LocalDateTime.now() + ", uid#" + uuid + ", on completion"));
        sseEmitter.onTimeout(() -> log.info(LocalDateTime.now() + ", uid#" + uuid + ", on timeout#" + sseEmitter.getTimeout()));
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info(LocalDateTime.now() + ", uid#" + uuid + ", on error#" + throwable.toString());
                        sseEmitter.send(SseEmitter.event().id(uuid).name("发生异常！")
                                .data(throwable.getMessage()).reconnectTime(3000));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        return sseEmitter;
    }

    private List<Message> limitMaxContextHandle(Collection<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return new ArrayList<>();
        }
        Queue<Message> messageQueue = new ArrayDeque<>(messages);
        if (messageQueue.size() > maxContextSize) {
            int differ = messageQueue.size() - maxContextSize;
            for (int i = 0; i < differ; i++) {
                messageQueue.poll();
            }
        }
        return new ArrayList<>(messageQueue);
    }
}
