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
public class BizException extends RuntimeException {
    public BizException(String message) {
        super("业务异常！" + message);
    }
}
