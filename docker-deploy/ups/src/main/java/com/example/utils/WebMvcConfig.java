package com.example.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        RequestParamMethodArgumentResolver resolver = new RequestParamMethodArgumentResolver(true);
        resolvers.add(resolver);
    }

}

