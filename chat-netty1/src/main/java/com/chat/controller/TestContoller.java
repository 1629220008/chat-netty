package com.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试是不是gateway是不是正常转发了
 */
@Slf4j
@RequestMapping("/hello")
@RestController
public class TestContoller {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${address.ip}")
    private String ip;

    @GetMapping
    public String hell() {
        log.info("hello");
        return "hello";
    }

    @PostMapping
    public String send(String entity) {
        rocketMQTemplate.convertAndSend(ip, entity);
        return "success";
    }
}
