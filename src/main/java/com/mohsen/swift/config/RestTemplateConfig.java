package com.mohsen.swift.config;

import com.mohsen.swift.util.ApiErrorResponseHandler;
import com.mohsen.swift.util.ApiLogInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class RestTemplateConfig {

  @Autowired
  CloseableHttpClient httpClient;

  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = restTemplateBuilder.errorHandler(new ApiErrorResponseHandler()).build();
    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory()));
    restTemplate.setInterceptors(Collections.singletonList(new ApiLogInterceptor()));
    return restTemplate;
  }

  @Bean
  @Scope("prototype")
  public RestTemplate ntlmRestTemplate(CloseableHttpClient httpClient) {
    RestTemplate restTemplate = restTemplateBuilder.errorHandler(new ApiErrorResponseHandler()).build();
    restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(ntlmClientHttpRequestFactory(httpClient)));
    restTemplate.setInterceptors(Collections.singletonList(new ApiLogInterceptor()));
    return restTemplate;
  }

  @Bean
  public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setHttpClient(httpClient);
    return clientHttpRequestFactory;
  }

  @Bean
  @Scope("prototype")
  public HttpComponentsClientHttpRequestFactory ntlmClientHttpRequestFactory(CloseableHttpClient httpClient) {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setHttpClient(httpClient);
    return clientHttpRequestFactory;
  }

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setThreadNamePrefix("dashboardPoolScheduler");
    scheduler.setPoolSize(50);
    return scheduler;
  }
}
