package com.mohsen.swift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class ApiErrorResponseHandler implements ResponseErrorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ApiErrorResponseHandler.class);

  @Override
  public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
    return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
        || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
  }

  @Override
  public void handleError(ClientHttpResponse httpResponse) throws IOException {
    if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
      LOG.error("Internal server error while calling the Web Service");
    } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
      LOG.error("Client error while calling the Web Service");

      if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
        LOG.error("Requested Web Service is not found");
      }
    }
  }
}
