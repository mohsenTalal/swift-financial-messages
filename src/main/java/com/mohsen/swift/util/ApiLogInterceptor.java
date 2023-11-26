package com.mohsen.swift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ApiLogInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(ApiLogInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);
    return response;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("------------------------------ API REQUEST ------------------------------");
      LOG.debug("URI          : {}", request.getURI());
      LOG.debug("Method       : {}", request.getMethod());
      LOG.debug("Headers      : {}", request.getHeaders());
      if (HttpMethod.GET != request.getMethod()) {
        LOG.debug("Request body : {}", new String(body, StandardCharsets.UTF_8));
      }
    }
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("------------------------------ API RESPONSE ------------------------------");
      LOG.debug("Status code  : {}", response.getStatusCode());
      LOG.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
    }
  }
}
