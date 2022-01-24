package com.etz.gh.kyc.server;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 *
 * @author seth.sebeh
 */
public class FallbackHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        String allowedMethods = "POST,GET";
        String method = exchange.getRequestMethod().toString();
        if (allowedMethods.contains(method)) {
            exchange.dispatch(ResponseCodeHandler.HANDLE_404);
        } else {
            exchange.dispatch(ResponseCodeHandler.HANDLE_405);
        }
    }

}
