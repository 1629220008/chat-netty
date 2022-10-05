package com.chat.config;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.HashMap;


public class EnvironmentConfig implements EnvironmentPostProcessor {

    private final String PROPERTY_NAME = "CUSTOMIZE_PROPERTY";

    @SneakyThrows
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        HashMap<String, Object> map = new HashMap<>();
        // 每台机子分配一个唯一id，
        String access = "1213";
        map.put("address.ip", access);
        MapPropertySource mapPropertySource = new MapPropertySource(PROPERTY_NAME, map);
        environment.getPropertySources().addLast(mapPropertySource);
    }
}
