package com.chat.handle;

import com.alibaba.fastjson.JSON;
import com.chat.constants.RedisEnum;
import com.chat.entity.MsgData;
import com.chat.util.ApplicationContextUtil;
import com.chat.util.ChannelUtils;
import com.chat.util.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务接收到的时候是poolbyte之后类型转变为了TextWebSocketFrame
 */
@Slf4j
public class CustomerChannelHandle extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RedisUtils redisUtils = ApplicationContextUtil.getBean(RedisUtils.class);

    private final RocketMQTemplate rocketMQTemplate = ApplicationContextUtil.getBean(RocketMQTemplate.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 首次连接是FullHttpRequest，处理参数
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            ConcurrentMap<String, String> paramMap = getUrlParams(uri);
            log.info("接收到的参数是：{}", JSON.toJSONString(paramMap));

            ChannelUtils.online(paramMap.get("uid"), ctx.channel());
            // 如果url包含参数，需要处理
            if (uri.contains("?")) {
                String newUri = uri.substring(0, uri.indexOf("?"));
                request.setUri(newUri);
            }
        }
        super.channelRead(ctx, msg);
    }

    private static ConcurrentMap<String, String> getUrlParams(String url) {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        if (!url.contains("?")) {
            return map;
        }
        if (url.split("\\?").length > 0) {
            String[] arr = url.split("\\?")[1].split("&");
            for (String s : arr) {
                String[] split = s.split("=");
                String key = split[0];
                String value = split[1];
                map.put(key, value);
            }
            return map;
        } else {
            return map;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) {
        log.info("服务器收到通信，channel={}, msg={}", channelHandlerContext.channel(), msg.text());
        // 实际消息体转成json存储一些用户id，
        MsgData msgData = JSON.parseObject(msg.text(), MsgData.class);
        List<String> server = (List<String>)redisUtils.getHashValue(RedisEnum.NETTY_USER.getKey(), msgData.getToUserId());
        String toTopic = server.get(0);
        String channelId = server.get(1);
        msgData.setChannelId(channelId);
        rocketMQTemplate.convertAndSend(toTopic, msgData);
        ChannelUtils.writeAndFlush(new TextWebSocketFrame(msg.text()));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("服务器建立通信，channel={}", ctx);
        ChannelUtils.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("服务器关闭连接，channel={}", channel.id());
        channel.close();
        ChannelUtils.remove(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("服务连接终端，错误为=", cause);
        handlerRemoved(ctx);
    }
}
