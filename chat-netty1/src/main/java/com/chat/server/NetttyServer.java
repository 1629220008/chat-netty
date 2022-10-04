package com.chat.server;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingService;
import com.chat.handle.CustomerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
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

    @Value("${netty.server.port}")
    private Integer nettyPort;

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    private EventLoopGroup baseGroup;

    private EventLoopGroup workGroup;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        baseGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(baseGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new CustomerChannelInitializer());
        ChannelFuture future = serverBootstrap.bind(nettyPort).sync();

        String ip = InetAddress.getLocalHost().getHostAddress();

        //将服务注册到注册中心
        NamingService namingService = nacosServiceManager
                .getNamingService(nacosDiscoveryProperties.getNacosProperties());

        namingService.registerInstance("netty-service", nacosDiscoveryProperties.getGroup(), ip, nettyPort);

        // 内部wait，大概意思应该是阻塞的监听网络io信号
        future.channel().closeFuture().sync();
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType.isAssignableFrom(ContextClosedEvent.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 优雅下线，关闭netty
        baseGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
        log.info("netty 服务下线");
    }
}
