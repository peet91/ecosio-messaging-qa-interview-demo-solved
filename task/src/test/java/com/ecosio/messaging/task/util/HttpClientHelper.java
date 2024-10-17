package com.ecosio.messaging.task.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class HttpClientHelper {

  public static CloseableHttpClient getHttpClientAcceptsAllSslCerts() {
    return getHttpClientBuilderAcceptsAllSslCerts().build();
  }

  public static HttpClientBuilder getHttpClientBuilderAcceptsAllSslCerts() {
    return getHttpClientBuilderAcceptsAllSslCerts(60);
  }

  public static HttpClientBuilder getHttpClientBuilderAcceptsAllSslCerts(int requestTimeoutInSeconds) {
    TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
    SSLContext sslContext;
    try {
      sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      throw new IllegalStateException(e);
    }
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
            NoopHostnameVerifier.INSTANCE);

    Registry<ConnectionSocketFactory> socketFactoryRegistry =
            RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslsf)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();

    // time-outs in seconds
    RequestConfig.Builder config = RequestConfig.custom();
    config.setConnectTimeout(requestTimeoutInSeconds * 1000);
    config.setConnectionRequestTimeout(requestTimeoutInSeconds * 1000);
    config.setSocketTimeout(requestTimeoutInSeconds * 1000);

    BasicHttpClientConnectionManager connectionManager =
            new BasicHttpClientConnectionManager(socketFactoryRegistry);

    return HttpClients.custom().setSSLSocketFactory(sslsf)
            .setDefaultRequestConfig(config.build())
            .setConnectionManager(connectionManager)
            .disableCookieManagement();
  }

}
