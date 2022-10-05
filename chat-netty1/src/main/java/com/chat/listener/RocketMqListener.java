package com.chat.listener;

import com.chat.entity.MsgData;
import com.chat.util.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${address.ip}", consumerGroup = "test-consumer", consumeMode = ConsumeMode.ORDERLY)
public class RocketMqListener implements RocketMQListener<MsgData> {

    @Override
    public void onMessage(MsgData msgData) {
        log.info("收到消息：msg={}", msgData);
        Channel channel = ChannelUtils.getChannel(msgData.getToUserId());
        channel.writeAndFlush(new TextWebSocketFrame(msgData.getMsg()));
    }
}
