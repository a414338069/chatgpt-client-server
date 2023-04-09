package com.haochen.chatgptclientserver.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.haochen.chatgptclientserver.common.BizException;
import com.haochen.chatgptclientserver.service.RecordService;
import com.unfbx.chatgpt.entity.chat.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
@Service
public class CacheRecordServiceImpl implements RecordService {

    /**
     * 初始化缓存
     */
    private static final Cache<String, List<Message>> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Override
    public List<Message> getRecord(String uid) {
        if (StringUtils.isBlank(uid)) {
            return new ArrayList<>();
        }
        List<Message> records = CACHE.getIfPresent(uid);
        if (CollectionUtils.isEmpty(records)) {
            records = new ArrayList<>();
        }
        return records;
    }

    @Override
    public void saveRecord(String uid, List<Message> messageRecord) {
        if (StringUtils.isBlank(uid)) {
            throw new BizException("未传输唯一id！");
        }
        if (CollectionUtils.isEmpty(messageRecord)) {
            return;
        }
        List<Message> messages = CACHE.getIfPresent(uid);
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<>();
        }
        messages.addAll(messageRecord);
        CACHE.put(uid, messages);
    }

    @Override
    public void putRecord(String uid, Message message) {
        if (StringUtils.isBlank(uid)) {
            throw new BizException("未传输唯一id！");
        }
        if (message == null) {
            return;
        }
        List<Message> messages = CACHE.getIfPresent(uid);
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<>();
        }
        messages.add(message);
        CACHE.put(uid, messages);
    }

    @Override
    public void clear(String uid) {
        if (StringUtils.isBlank(uid)) {
            throw new BizException("未传输唯一id！");
        }
        CACHE.put(uid, new ArrayList<>());
    }
}
