package com.haochen.chatgptclientserver.listener;

import com.haochen.chatgptclientserver.common.JacksonUtils;
import com.haochen.chatgptclientserver.service.RecordService;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 描述：OpenAIEventSourceListener
 *
 * @author https:www.unfbx.com
 * @date 2023-02-22
 */
@Slf4j
public class OpenAISSEEventSourceListener extends EventSourceListener {

    private final SseEmitter sseEmitter;

    private final List<Message> responseMessages = new ArrayList<>();

    private RecordService recordService;

    private String uid;

    public OpenAISSEEventSourceListener(SseEmitter sseEmitter, String uid, RecordService recordService) {
        this.sseEmitter = sseEmitter;
        this.recordService = recordService;
        this.uid = uid;
    }

    public OpenAISSEEventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        // 结束标识符
        String endString = "[DONE]";
        if (endString.equals(data)) {
            log.info("OpenAI返回数据结束了");
            if (recordService != null) {
                Message responseMessage = collectResponse();
                this.recordService.putRecord(uid, responseMessage);
            }
            // 传输完成后自动关闭sse
            sseEmitter.complete();
            return;
        }
        // 读取Json
        ChatCompletionResponse completionResponse = JacksonUtils.jsonToObj(data, ChatCompletionResponse.class);
        Message responseMessage = completionResponse.getChoices().get(0).getDelta();
        responseMessages.add(responseMessage);
        sseEmitter.send(SseEmitter.event()
                .id(completionResponse.getId())
                .data(responseMessage)
                .reconnectTime(3000));
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }

    private Message collectResponse() {
        if (CollectionUtils.isEmpty(responseMessages)) {
            return null;
        }
        StringBuilder content = new StringBuilder();
        responseMessages.stream()
                .filter(response -> StringUtils.isNotBlank(response.getContent()))
                .forEach(message -> content.append(message.getContent()));

        String roleString = responseMessages.stream()
                .filter(response -> StringUtils.isNotBlank(response.getRole()))
                .findFirst()
                .map(Message::getRole)
                .orElse("");
        Message.Role role = Message.Role.ASSISTANT;
        for (Message.Role item : Message.Role.values()) {
            if (item.getName().equalsIgnoreCase(roleString)) {
                role = item;
            }
        }
        return Message.builder()
                .role(role)
                .content(content.toString()).build();
    }
}
