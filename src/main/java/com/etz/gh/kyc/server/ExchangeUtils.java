package com.etz.gh.kyc.server;

import io.undertow.io.Receiver;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author seth.sebeh
 */
public class ExchangeUtils {

    static final Logger logger = LogManager.getLogger(ExchangeUtils.class);

    public static String getMethod(HttpServerExchange exchange) {
        return exchange.getRequestMethod().toString();
    }

    public static String getRemoteIP(HttpServerExchange exchange) {
        return exchange.getSourceAddress().getAddress().getHostAddress();
    }

    public static void printRequestHeaders(HttpServerExchange exchange) {
        logger.info(Thread.currentThread().getName() + " -- headers --");
        HeaderMap headers = exchange.getRequestHeaders();
        logger.info(headers);
        headers.forEach(s -> logger.info(s.getHeaderName() + "=" + s));
    }

    public static String getHeader(HttpServerExchange exchange, String header) {
        HeaderMap headers = exchange.getRequestHeaders();
        return headers.getFirst(header);
    }
    
    public static void allowAllOrigin(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
    }

    public static void setHeaders(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
    }
    
    public static void setHeader(HttpServerExchange exchange, String header, String value) {
        exchange.getResponseHeaders().put(new HttpString(header), value);
    }
    
    public static void setHeader(HttpServerExchange exchange, HttpString header, String value) {
        exchange.getResponseHeaders().put(header, value);
    }

    public static void sendResponse(HttpServerExchange exchange, String message, int statusCode) {
        exchange.setStatusCode(statusCode);
        exchange.getResponseSender().send(message);
        exchange.getResponseSender().close();
    }

    public static void sendResponse(HttpServerExchange exchange, int statusCode) {
        exchange.setStatusCode(statusCode);
        exchange.getResponseSender().close();
    }

    public static String getRequestBody(HttpServerExchange exchange) throws IOException {
        InputStream inputStream = exchange.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException ex) {
            logger.error("Something wrong here", ex);
            exchange.dispatch(ResponseCodeHandler.HANDLE_500);
        }
        String body = stringBuilder.toString();
        logger.info(Thread.currentThread().getName() + "Request body " + body);
        return body;
    }

    public static Map<String, Deque<String>> getQueryMap(HttpServerExchange exchange) {
        return exchange.getQueryParameters();
    }

    public static String getQueryParam(HttpServerExchange exchange, String param) {
        try{
            return exchange.getQueryParameters().get(param).getFirst();
        }catch(NullPointerException e){
            return null;
        }
    }

    public static OutputStream getOutputStream(HttpServerExchange exchange) {
        try (OutputStream os = exchange.getOutputStream()) {
            return os;
        } catch (IOException ex) {
            logger.error("Something wrong here", ex);
            exchange.dispatch(ResponseCodeHandler.HANDLE_500);
        }
        return null;
    }

    public static void sendResponseOutputStream(HttpServerExchange exchange, String message) {
        try (OutputStream os = exchange.getOutputStream()) {
            exchange.getResponseSender().send(message);
            os.write(message.getBytes());
            os.flush();
        } catch (IOException ex1) {
            logger.error("Something wrong here", ex1);
            exchange.dispatch(ResponseCodeHandler.HANDLE_500); 
        }
    }
//    
//    public static String getBodyRequestParams(HttpServerExchange exchange){
////    StringBuilder requestBody = new StringBuilder((int) bytesInBody);
//    exchange.getRequestReceiver().receiveFullString(new Receiver.FullStringCallback() {
//        @Override
//        public void handle(HttpServerExchange exchange, String message) {
//            requestBody.append(message);
//        }
//    });
//
//    return requestBody.toString();
//}
}
