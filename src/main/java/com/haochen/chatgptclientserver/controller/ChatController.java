package com.haochen.chatgptclientserver.controller;

import com.haochen.chatgptclientserver.common.SystemException;
import com.haochen.chatgptclientserver.entity.Chat;
import com.haochen.chatgptclientserver.service.SseChatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final SseChatService sseChatService;

    public ChatController(SseChatService sseChatService) {
        this.sseChatService = sseChatService;
    }

    @PostMapping("/sse")
    public SseEmitter chat(@RequestBody @Validated Chat chat) {
        try {
            return sseChatService.chat(chat.getUid(), chat.getMessage());
        } catch (IOException e) {
            throw new SystemException("打开SSE链接出现错误！请检查！");
        }
    }
}
