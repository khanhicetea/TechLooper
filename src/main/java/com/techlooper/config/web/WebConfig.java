package com.techlooper.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.techlooper.controller"})
public class WebConfig extends WebMvcConfigurerAdapter {

  @Resource
  private Environment environment;

  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("index.html");
  }

  @Bean
  public ViewResolver viewResolver() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setSuffix("");
    return viewResolver;
  }

  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations(environment.getProperty("webapp.resource.location"))
      .resourceChain(true)
            /*
             * .addResolver(new CachingResourceResolver(cacheManager,
             * "default"))
             */.addResolver(new GzipResourceResolver())
      // .addTransformer(new CachingResourceTransformer(cacheManager,
      // "default"))
      .addTransformer(new CssLinkResourceTransformer());
  }
}
