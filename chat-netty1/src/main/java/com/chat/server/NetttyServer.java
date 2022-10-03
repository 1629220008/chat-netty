package com.chat.server;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.alibaba.nacos.api.config.annotation.NacosProperty;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.chat.handle.CustomerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;

import java.net.InetAddress;

@Configuration
@Slf4j
public class NetttyServer implements ApplicationRunner, SmartApplicationListener {
    private ServerBootstrap serverBootstrap;

    private ChannelFuture sync;

    private Channel channel;

    @Value("${netty.server.port}")
    private Integer nettyPort;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventLoopGroup baseGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(baseGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new CustomerChannelInitializer());
        ChannelFuture future = serverBootstrap.bind(nettyPort);
        channel = future.channel();

        String ip = InetAddress.getLocalHost().getHostAddress();
        //将服务注册到注册中心

        NamingService namingService = NamingFactory.createNamingService(nacosDiscoveryProperties.getServerAddr());
        namingService.registerInstance("netty-service", ip, nettyPort);
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType.isAssignableFrom(ContextClosedEvent.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 优雅下线，关闭netty
        if (channel != null) {
            channel.close();
        }
        log.info("netty 服务下线");
    }
}
