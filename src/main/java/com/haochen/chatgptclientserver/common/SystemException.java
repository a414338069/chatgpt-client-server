package com.haochen.chatgptclientserver.common;

/**
 * <p>模块说明：</p>
 *
 * <p>修改历史：</p>
 * create_time: 2023/4/8
 *
 * @author chenhao
 * date 2023/4/8
 **/
public class SystemException extends RuntimeException {

    public SystemException(String message) {
        super("系统异常！" + message);
    }
}
