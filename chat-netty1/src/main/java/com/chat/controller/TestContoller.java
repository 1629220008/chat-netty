package com.chat.controller;

import lombok.extern.slf4j.Slf4j;
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
    @GetMapping
    public String hell() {
        log.info("hello");
        return "hello";
    }

    @PostMapping
    public String send(String msg, String ...userId) {
        return "success";
    }
}
