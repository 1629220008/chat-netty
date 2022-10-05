package com.chat.entity;

import lombok.Data;

/**
 * 实际还会有群聊，具体根据消息体类型判断
 */
@Data
public class MsgData {
    public String msg;
    public String userId;
    public String toUserId;
    public String channelId;
}
