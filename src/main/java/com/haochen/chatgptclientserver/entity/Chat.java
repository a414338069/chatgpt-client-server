package com.haochen.chatgptclientserver.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Chat {

    /**
     * 一次聊天的唯一id
     */
    @NotBlank(message = "聊天的id不能为空")
    private String uid;

    /**
     * 消息内容
     */
    @NotBlank(message = "请输入你需要问的问题")
    private String message;
}
