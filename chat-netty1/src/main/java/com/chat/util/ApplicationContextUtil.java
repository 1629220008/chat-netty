package com.chat.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static  <T> T getBean(Class<T> t) {
        return applicationContext.getBean(t);
    }
}
