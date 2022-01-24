/*
 * ETZ.Dev.Team 2019
 */
package com.etz.gh.kyc.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SuperHttpClientOriginal {

   private static final Logger logger = LogManager.getLogger(SuperHttpClientOriginal.class);

    static final int CONNECT_TIMEOUT = 30;
    static final int SOCKET_TIMEOUT = 30;
    static final boolean LOGGING_ENABLED = true;
    final static boolean ENABLE_DEBUGGING = false; //turn off in production environment.

    public static void main(String[] args) {
        SuperHttpClientOriginal client = new SuperHttpClientOriginal();
        client.doGet("https://google.com", CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    //default timeout 30secs
    public static Map<String, String> doGet(String url, String payload) {
        return doGet(url, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static Map<String, String> doGet(String url, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        //httpGet.addHeader("Content-type", "application/json");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpGet);
    }

    //default timeout 30secs
    public static Map<String, String> doPost(String url, String payload) {
        return doPost(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static Map<String, String> doPost(String url, String payload, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        StringEntity st = new StringEntity(payload, "UTF-8");
        st.setChunked(true);
        httpPost.setEntity(st);
        //httpPost.addHeader("Accept", "application/json");
        //httpPost.addHeader("Content-type", "application/json");
        //httpPost.addHeader("Content-type", "text/xml");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpPost);
    }

    //one way SSL authentication
    public static Map<String, String> doPostSSL(String url, String payload, int connectTimeout, int socketTimeout, String trustStoreLoc, String trustStorePass) {
        return doPostSSL(url, payload, connectTimeout, socketTimeout, trustStoreLoc, trustStorePass, null, null);
    }

    //one way SSL authentication
    //default timeout of 30 seconds
    public static Map<String, String> doPostSSL(String url, String payload, String trustStoreLoc, String trustStorePass) {
        return doPostSSL(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT, trustStoreLoc, trustStorePass, null, null);
    }

    //main ssl method - handles both oneway or two way authentication
    public static Map<String, String> doPostSSL(String url, String payload, int connectTimeout, int socketTimeout, String trustStoreLoc, String trustStorePass, String keystoreloc, String keyStorePass) {
        SSLConnectionSocketFactory sslsf = null;
        if (keystoreloc == null) {
            //1 way
            sslsf = getSocketFactory(trustStoreLoc, trustStorePass);
        } else {
            //2 way
            sslsf = getSocketFactory(trustStoreLoc, trustStorePass, keystoreloc, keyStorePass);
        }
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        httpPost.addHeader("Content-type", "text/xml");
        StringEntity st = new StringEntity(payload, "UTF-8");
        st.setChunked(true);
        httpPost.setEntity(st);
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpPost);
    }

    //mutual authentication
    //default timeout of 30 seconds
    public static Map<String, String> doPostSSL(String url, String payload, String trustStoreLoc, String trustStorePass, String keystoreloc, String keyStorePass) {
        return doPostSSL(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT, trustStoreLoc, trustStorePass, keystoreloc, keyStorePass);
    }

    public static Map<String, String> send(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
        if (ENABLE_DEBUGGING) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
        }
        long start = System.currentTimeMillis();
        try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
            logger.info("RESPONSE HEADERS >> " + response.toString());
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            //long len = entity.getContentLength();
            String rspBody = EntityUtils.toString(entity);
            //logger.info("RESPONSE BODY >> " + rspBody);
            logger.info("TAT [CONNECT+DATA] >> " + (System.currentTimeMillis() - start));
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("code", String.valueOf(status));
            returnMap.put("body", rspBody);
            returnMap.put("header", response.toString());
            return returnMap;
        } catch (ConnectTimeoutException e) {
            logger.error("CONNECT TIMEOUT >> Couldn't establish connection to the host server");
            logger.error("Took more than " + (System.currentTimeMillis() - start) + "ms to connect to the server");
            logger.error("Error ", e);
        } catch (SocketTimeoutException e) {
            logger.error("READ TIMEOUT >> Couldn't read data from the host server");
            logger.error("Took more than " + (System.currentTimeMillis() - start) + "ms to read data from the server");
            logger.error("Error ", e);
        } catch (Exception e) {
            logger.error("IOException Calling HTTPS server. Possibly server is down or not accepting the request");
            logger.error("Error ", e);
        }
        return null;
    }

    //two way ssl set up
    public static SSLConnectionSocketFactory getSocketFactory(String trustStoreLoc, String trustStorePass, String keystoreloc, String keyStorePass) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            try (final InputStream is = new FileInputStream(keystoreloc)) {
                keyStore.load(is, keyStorePass.toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePass.toCharArray());

            final KeyStore trustStore = KeyStore.getInstance("JKS");
            try (final InputStream is = new FileInputStream(trustStoreLoc)) {
                trustStore.load(is, trustStorePass.toCharArray());
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null, new NoopHostnameVerifier());
            return sslsf;
        } catch (java.security.KeyStoreException e) {
            logger.error("Error while creating SSL Factory.");
            logger.error("Error ", e);
            return null;
        } catch (Exception e) {
            logger.info("Error while creating SSL Factory.");
            logger.error("Error ", e);
        }
        return null;
    }

    //one way ssl set up
    public static SSLConnectionSocketFactory getSocketFactory(String trustStoreLoc, String trustStorePass) {
        try {
            final KeyStore trustStore = KeyStore.getInstance("JKS");
            try (final InputStream is = new FileInputStream(trustStoreLoc)) {
                trustStore.load(is, trustStorePass.toCharArray());
            }
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null, new NoopHostnameVerifier());
            return sslsf;
        } catch (java.security.KeyStoreException e) {
            logger.error("Error while creating SSL Factory.");
            logger.error("Error ", e);
            return null;
        } catch (Exception e) {
            logger.info("Error while creating SSL Factory.");
            logger.error("Error ", e);
        }
        return null;
    }

    public static String getBascicAuthStr(String username, String password) {
        String authStr = username + ":" + password;
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());
        return authStr;
    }
}
