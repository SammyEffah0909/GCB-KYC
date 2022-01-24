/*
 * ETZ.Dev.Team 2019
 */
package com.etz.gh.kyc.gcb;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SuperHttpClient {

    private static final Logger logger = LogManager.getLogger(SuperHttpClient.class);
    static final int CONNECT_TIMEOUT = 30;
    static final int SOCKET_TIMEOUT = 30;
    static final boolean LOGGING_ENABLED = true;
    final static boolean ENABLE_DEBUGGING = false; //turn off in production environment.

    public static void main(String[] args) {
        SuperHttpClient client = new SuperHttpClient();
        client.doGet("https://google.com", CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    //default timeout 30secs
    public static String doGet(String url, String token) {
        return doGet(url, token, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doGet(String url, String token, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        httpGet.addHeader("Accept", "application/json");
        httpGet.addHeader("Authorization", "Bearer " + token);

        logger.info("connecting to url >>" + url);
        return send(httpClient, httpGet);
    }

    public static String doGet(String url, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        //httpGet.addHeader("Content-type", "application/json");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpGet);
    }

    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT * 1000).setSocketTimeout(SOCKET_TIMEOUT * 1000).build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        //httpGet.addHeader("Content-type", "application/json");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpGet);
    }

    //default timeout 30secs
    public static String doPost(String url, String payload) {
        return doPost(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doPostXML(String url, String payload) {
        return sendXmlRequest(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doPost(String url, String payload, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        StringEntity st = new StringEntity(payload, "UTF-8");
        st.setChunked(true);
        httpPost.setEntity(st);
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
//        httpPost.addHeader("Content-type", "text/xml");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpPost);
    }

    public static String sendXmlRequest(String url, String payload, int connectTimeout, int socketTimeout) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        StringEntity st = new StringEntity(payload, "UTF-8");
        st.setChunked(true);
        httpPost.setEntity(st);
        httpPost.addHeader("Content-type", "text/xml");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpPost);
    }

    //one way SSL authentication
    public static String doPostSSL(String url, String payload, int connectTimeout, int socketTimeout, String trustStoreLoc, String trustStorePass) {
        return doPostSSL(url, payload, connectTimeout, socketTimeout, trustStoreLoc, trustStorePass, null, null);
    }

    //one way SSL authentication
    //default timeout of 30 seconds
    public static String doPostSSL(String url, String payload, String trustStoreLoc, String trustStorePass) {
        return doPostSSL(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT, trustStoreLoc, trustStorePass, null, null);
    }

    //main ssl method - handles both oneway or two way authentication
    public static String doPostSSL(String url, String payload, int connectTimeout, int socketTimeout, String trustStoreLoc, String trustStorePass, String keystoreloc, String keyStorePass) {
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
        StringEntity st = new StringEntity(payload, "UTF-8");
        st.setChunked(true);
        httpPost.setEntity(st);
        //httpPost.addHeader("Accept", "application/json");
        //httpPost.addHeader("Content-type", "application/json");
        //httpPost.addHeader("Content-type", "text/xml");
        logger.info("connecting to url >>" + url);
        return send(httpClient, httpPost);
    }

    //mutual authentication
    //default timeout of 30 seconds
    public static String doPostSSL(String url, String payload, String trustStoreLoc, String trustStorePass, String keystoreloc, String keyStorePass) {
        return doPostSSL(url, payload, CONNECT_TIMEOUT, SOCKET_TIMEOUT, trustStoreLoc, trustStorePass, keystoreloc, keyStorePass);
    }

    public static String send(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
        if (ENABLE_DEBUGGING) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
        }
        long start = System.currentTimeMillis();
        try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
//            logger.info("RESPONSE HEADERS >> " + response.toString());
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            //long len = entity.getContentLength();
            String rspBody = EntityUtils.toString(entity);
            logger.info("RESPONSE STATUS >> " + status);
            logger.info("RESPONSE BODY >> " + rspBody);
            logger.info("TAT [CONNECT+DATA] >> " + (System.currentTimeMillis() - start));
            return rspBody;
        } catch (ConnectTimeoutException e) {
            logger.info("CONNECT TIMEOUT >> Couldn't establish connection to the host server");
            logger.info("Took more than " + (System.currentTimeMillis() - start) + "ms to connect to the server");
            e.printStackTrace(System.out);
        } catch (SocketTimeoutException e) {
            logger.info("READ TIMEOUT >> Couldn't read data from the host server");
            logger.info("Took more than " + (System.currentTimeMillis() - start) + "ms to read data from the server");
            e.printStackTrace(System.out);
        } catch (Exception e) {
            logger.info("IOException Calling HTTPS server. Possibly server is down or not accepting the request");
            e.printStackTrace(System.out);
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

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sslsf;
        } catch (java.security.KeyStoreException e) {
            logger.info("Error while creating SSL Factory.");
            return null;
        } catch (Exception e) {
            logger.info("Error while creating SSL Factory.");
            e.printStackTrace(System.out);
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

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sslsf;
        } catch (java.security.KeyStoreException e) {
            logger.info("Error while creating SSL Factory.");
            return null;
        } catch (Exception e) {
            logger.info("Error while creating SSL Factory.");
            e.printStackTrace(System.out);
        }
        return null;
    }

    public static String getBascicAuthStr(String username, String password) {
        String authStr = username + ":" + password;
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());
        return authStr;
    }

    public static Map<String, String> doPostMultiPart(String url, List<NameValuePair> params) {
        return doPostMultiPart(url, params, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static Map<String, String> doPostMultiPart(String url, List<NameValuePair> params, int connectTimeout, int socketTimeout) {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        httpPost.setEntity(entity);
        logger.info(" connecting to url >>" + url);
        return sendMap(httpClient, httpPost);
    }

    public static Map<String, String> sendMap(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
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

    public static Map<String, String> doGetMap(String url, String token) {

        return doGetMap(url, token, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static Map<String, String> doGetMap(String url, String token, int connectTimeout, int socketTimeout) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig config = RequestConfig.custom().setConnectTimeout(connectTimeout * 1000).setSocketTimeout(socketTimeout * 1000).build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Authorization", "Bearer " + token);

            logger.info("connecting to url >>" + url);
            return sendMapGet(httpClient, httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Map<String, String> sendMapGet(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
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

}
