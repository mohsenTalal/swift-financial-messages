package com.mohsen.swift.config;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class HttpClientConfig {

  private static final Logger LOG = LoggerFactory.getLogger(HttpClientConfig.class);

  @Value("${api.http.connection-timeout}")
  int connectionTimeout;

  @Value("${api.http.request-timeout}")
  int requestTimeout;

  @Value("${api.http.socket-timeout}")
  int socketTimeout;

  @Value("${api.http.max-connections}")
  int maxConnections;

  @Value("${api.http.keep-alive-time}")
  int keepAliveTime;

  @Value("${api.http.close-idle-wait-time}")
  int closeIdleWaitTime;

  @Bean
  public PoolingHttpClientConnectionManager poolingConnectionManager() {

    SSLContextBuilder builder = new SSLContextBuilder();
    try {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
      builder = builder.loadTrustMaterial(null, acceptingTrustStrategy);
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      LOG.error("Pooling Connection Manager Initialisation failure because of {}", e.getMessage(), e);
    }

    SSLConnectionSocketFactory socketFactory = null;

    try {
      socketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      LOG.error("Pooling Connection Manager Initialisation failure because of {}", e.getMessage(), e);
    }

    assert socketFactory != null;
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
        .<ConnectionSocketFactory>create()
        .register("https", socketFactory)
        .register("http", new PlainConnectionSocketFactory())
        .build();

    PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    poolingConnectionManager.setMaxTotal(maxConnections);

    return poolingConnectionManager;
  }

  @Bean
  public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
    return (httpResponse, httpContext) -> {
      HeaderElementIterator it = new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
      while (it.hasNext()) {
        HeaderElement element = it.nextElement();
        String param = element.getName();
        String value = element.getValue();

        if (value != null && param.equalsIgnoreCase("timeout")) {
          return Long.parseLong(value) * 1000;
        }
      }
      return keepAliveTime;
    };
  }

  @Bean
  public CloseableHttpClient httpClient(){
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(requestTimeout)
        .setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .build();

    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(poolingConnectionManager())
        .setKeepAliveStrategy(connectionKeepAliveStrategy())
        .build();
  }

  @Bean
  @Scope("prototype")
  public CloseableHttpClient ntlmHttpClient(String username, String password) {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new NTCredentials(username, password, "", ""));

    Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
        .register(AuthSchemes.NTLM, new NTLMSchemeFactory()).build();

    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(connectionTimeout * 1000)
        .setConnectionRequestTimeout(requestTimeout * 1000)
        .setCookieSpec(CookieSpecs.DEFAULT)
        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.KERBEROS, AuthSchemes.SPNEGO))
        .build();

    return HttpClientBuilder.create()
        .setDefaultCredentialsProvider(credentialsProvider)
        .setDefaultAuthSchemeRegistry(authSchemeRegistry)
        .setConnectionManager(poolingConnectionManager())
        .setDefaultRequestConfig(config)
        .build();
  }

  @Bean
  public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
    return new Runnable() {
      @Override
      @Scheduled(fixedDelay = 10000)
      public void run() {
        try {
          if (connectionManager != null) {
            LOG.trace("IdleConnectionMonitor - Closing expired and idle connections...");
            connectionManager.closeExpiredConnections();
            connectionManager.closeIdleConnections(closeIdleWaitTime, TimeUnit.MILLISECONDS);
          } else {
            LOG.trace("IdleConnectionMonitor - Http Client Connection Manager is not initialised");
          }
        } catch (Exception e) {
          LOG.error("IdleConnectionMonitor - Exception while closing connections: {}", e.getMessage(), e);
        }
      }
    };
  }
}
