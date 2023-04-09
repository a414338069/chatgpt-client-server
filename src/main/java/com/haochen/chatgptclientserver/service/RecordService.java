package com.haochen.chatgptclientserver.service;

import com.unfbx.chatgpt.entity.chat.Message;

import java.util.List;

/**
 * <p>模块说明：</p>
 *
 * <p>修改历史：</p>
 * create_time: 2023/4/8
 *
 * @author chenhao
 * date 2023/4/8
 **/
public interface RecordService {

    /**
     * 根据uid获取记录
     *
     * @param uid 唯一id
     * @return 记录信息
     */
    List<Message> getRecord(String uid);

    /**
     * 保存记录
     *
     * @param uid           唯一id
     * @param messageRecord 记录信息
     */
    void saveRecord(String uid, List<Message> messageRecord);

    /**
     * 压入一个记录，记录在uid的末尾
     *
     * @param uid     唯一值
     * @param message 压入的消息
     */
    void putRecord(String uid, Message message);

    /**
     * 根据uid清理对应的上下文
     *
     * @param uid 唯一id
     */
    void clear(String uid);

}
