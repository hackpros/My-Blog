package com.my.blog.website.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

public enum AppContext {

    INSTANCE;

    public static AppContext getInstance() {
        return INSTANCE;
    }

    private ApplicationContext applicationContext;

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static{
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    /**
     * Default constructor
     */
    AppContext() {
    }

    /**
     * 
     */
    public void setContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 
     * @return
     */
    public ApplicationContext getContext() {
        return applicationContext;
    }
}