package com.chat.util;

import com.chat.constants.RedisEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtils {
    private static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final String USER = "user";

    private static final AttributeKey<String> key = AttributeKey.valueOf(USER);

    private static final ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    private static final RedisUtils redisUtils = ApplicationContextUtil.getBean(RedisUtils.class);

    private static final String topic = ApplicationContextUtil.getValue("address.ip", String.class);

    public static void writeAndFlush(Object o) {
        group.writeAndFlush(o);
    }

    public static void add(Channel channel) {
        group.add(channel);
    }

    public static void remove(Channel channel) {
        group.remove(channel);
        String userId = channel.attr(key).get();
        channelMap.remove(userId);
    }

    @SneakyThrows
    public static void online(String userId, Channel channel) {
        // 保存channel通道的附带信息，以用户的uid为标识
        channel.attr(key).set(userId);
        channelMap.put(userId, channel);
        // 將映射存放到redis 便于集群的服务发现 为了省事，先假设用户只会登录一台机子
        redisUtils.setHashValue(RedisEnum.NETTY_USER.getKey(), userId, Arrays.asList(topic, channel.id().asLongText()));
    }

    public static Channel getChannel(String userId) {
        return channelMap.get(userId);
    }
}
