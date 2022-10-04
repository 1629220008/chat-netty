package com.chat.handle;

import com.alibaba.fastjson.JSON;
import com.chat.util.ApplicationContextUtil;
import com.chat.util.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务接收到的时候是poolbyte之后类型转变为了TextWebSocketFrame
 */
@Slf4j
public class CustomerChannelHandle extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final String USER = "user";
    private final AttributeKey<String> key = AttributeKey.valueOf(USER);
    private static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private RedisUtils redisUtils = ApplicationContextUtil.getBean(RedisUtils.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 首次连接是FullHttpRequest，处理参数
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            ConcurrentMap<String, String> paramMap = getUrlParams(uri);
            System.out.println("接收到的参数是：" + JSON.toJSONString(paramMap));

            online(paramMap.get("uid"), ctx.channel());
            // 如果url包含参数，需要处理
            if (uri.contains("?")) {
                String newUri = uri.substring(0, uri.indexOf("?"));
                request.setUri(newUri);
            }
        } else if (msg instanceof TextWebSocketFrame) {
            // 正常的TEXT消息类型
            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            log.info("read0: {}", frame.text());
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        log.info("服务器收到通信，channel={}, msg={}", channelHandlerContext.channel(), msg.text());
        AttributeKey.valueOf("name");
        group.writeAndFlush(new TextWebSocketFrame(msg.text()));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("服务器建立通信，channel={}", ctx);
        group.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("服务器关闭连接，channel={}", channel.id());
        channel.close();
        group.remove(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务连接终端，错误为=", cause);
        handlerRemoved(ctx);
    }

    @SneakyThrows
    private void online(String userId, Channel channel) {
        // 保存channel通道的附带信息，以用户的uid为标识
        channel.attr(key).set(userId);
        // 將映射存放到redis 便于集群的服务发现 为了省事，先假设用户只会登录一台机子
        String ip = InetAddress.getLocalHost().getHostAddress();
        redisUtils.setValue(userId, Arrays.asList(ip, channel.id().asLongText()));
    }
}
