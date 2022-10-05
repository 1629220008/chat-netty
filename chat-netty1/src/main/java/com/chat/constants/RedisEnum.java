package com.chat.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RedisEnum {
    NETTY_USER("netty-user", "netty的channel与用户映射关系");
    private String key;
    private String desc;
}
