package com.chat.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware, EnvironmentAware {
    private static ApplicationContext applicationContext;
    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @Override
    public void setEnvironment(Environment _environment) {
        environment = _environment;
    }

    public static  <T> T getBean(Class<T> t) {
        return applicationContext.getBean(t);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(String s, Class<T> t) {
        return (T)environment.getProperty(s);
    }
}
