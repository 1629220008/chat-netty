package com.chat.handle;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务接收到的时候是poolbyte之后类型转变为了TextWebSocketFrame
 */
@Slf4j
public class CustomerChannelHandle extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) throws Exception {
        log.info("服务器收到通信，channel={}, msg={}", channelHandlerContext.channel(), msg.text());
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
}
