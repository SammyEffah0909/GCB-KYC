package com.etz.gh.kyc.server;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class EchoService implements HttpHandler {

    private static final Logger logger = LogManager.getLogger(EchoService.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        long start = System.currentTimeMillis();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("I AM ALIVE");
        logger.info("Echo service TAT " + (System.currentTimeMillis() - start) + " ms");
    }
}
