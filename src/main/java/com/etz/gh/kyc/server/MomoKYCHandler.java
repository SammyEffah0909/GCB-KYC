package com.etz.gh.kyc.server;

import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.processor.DirectProcessorController;
import com.etz.gh.kyc.util.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class MomoKYCHandler implements HttpHandler {

    private static final Logger logger = LogManager.getLogger(MomoKYCHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        try {
            long start = System.currentTimeMillis();
            String sourceIP = ExchangeUtils.getRemoteIP(exchange);
            logger.info(">>> new request received: {} from source IP: {}", exchange.getQueryString(), sourceIP);

            boolean ipAllowed = true;
            if (!ipAllowed) {
                ExchangeUtils.sendResponse(exchange, "IP_NOT_ALLOWED", 401);
            }

            String msisdn = ExchangeUtils.getQueryParam(exchange, "msisdn");
            String network = ExchangeUtils.getQueryParam(exchange, "network");
            final String LOG_PREFIX = network + "***" + msisdn + " ";

            KYCResponse response = new DirectProcessorController().process(network, msisdn);

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, Config.CONTENT_TYPE_TEXT_PLAIN);
            if (response != null && response.getError().equals("0")) {
                exchange.getResponseSender().send(response.getName());
            } else {
                ExchangeUtils.sendResponse(exchange, "VERIFICTION FAILED", 500);
            }

            logger.info("<<< {} end of request , TAT {} ms", LOG_PREFIX, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error("Error processing request", e);
            exchange.dispatch(ResponseCodeHandler.HANDLE_500);
        }

    }
}
