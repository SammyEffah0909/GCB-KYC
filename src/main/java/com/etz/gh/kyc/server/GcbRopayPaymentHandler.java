/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.gh.kyc.server;

import com.etz.gh.kyc.gcb.GcbClient;
import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.util.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author samuel.onwona
 */
public class GcbRopayPaymentHandler implements HttpHandler {

    private static final Logger logger = LogManager.getLogger(GcbRopayPaymentHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        long start = System.currentTimeMillis();
        ExchangeUtils.allowAllOrigin(exchange);
//        ExchangeUtils.setHeader(exchange, Headers.CONTENT_TYPE, Config.CONTENT_TYPE_TEXT_PLAIN);
        ExchangeUtils.setHeader(exchange, Headers.CONTENT_TYPE, Config.CONTENT_TYPE_APPLICATION_JSON);
        String LOG_PREFIX = "";
        try {
            KYCResponse response = new KYCResponse();

            if (isIPAllowed(exchange)) {
                int bufferSize = 1024;
                char[] buffer = new char[bufferSize];
                StringBuilder jsonData = new StringBuilder();
                Reader in = new InputStreamReader(exchange.getInputStream(), StandardCharsets.UTF_8);
                for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0;) {
                    jsonData.append(buffer, 0, numRead);
                }
                logger.info("REQUEST BODY::: " + jsonData.toString());
                JSONObject jsonObj = new JSONObject(jsonData.toString());
                String productType = jsonObj.optString("productType");
                String clientId = jsonObj.optString("clientId");
                String amount = jsonObj.optString("amount");
                String currency = jsonObj.optString("currency");
                String companyId = jsonObj.optString("companyId");
                String productId = jsonObj.optString("productId");
                String bankReference = jsonObj.optString("bankReference");
                String customerName = jsonObj.optString("customerName");
                String customerPhone = jsonObj.optString("customerPhone");
                String customerLevel = jsonObj.optString("customerLevel");
                String program = jsonObj.optString("program");
                String paymentName = jsonObj.optString("paymentName");
                String description = jsonObj.optString("description");
                String traceId = jsonObj.optString("traceid");
                String resp;
                if (amount != null && bankReference != null && productType != null && productId != null && companyId != null && customerName != null) {
                    LOG_PREFIX = bankReference + "***" + customerPhone + " ";
                    if (!clientId.isEmpty()) {
                        resp = new GcbClient().processRopayTransactions(productId, productType, clientId, amount, currency, customerName, bankReference, customerPhone, customerLevel, program, paymentName, description);
                        if (resp != null) {

                            JSONObject respJsonObj = new JSONObject(resp);

                            if (respJsonObj.optString("status").equals("00")) {
                                ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                                exchange.getResponseSender().send("{\n"
                                        + "    \"message\": \"" + respJsonObj.optString("message") + "\",\n"
                                        + "    \"transaction_details\": \"" + respJsonObj.optString("productDetails") + "\",\n"
                                        + "    \"sendSms\": \"" + respJsonObj.optString("smsMessage").split("~")[0] + "\",\n"
                                        + "    \"smsMessageToSend\": \"" + respJsonObj.optString("smsMessage").split("~")[1] + "\",\n"
                                        + "    \"resp\":\"00\"    \n"
                                        + "}");
                            } else {
                                ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                                ExchangeUtils.sendResponse(exchange, "{\n"
                                        + "    \"errorMessage\": \"" + respJsonObj.optString("message") + "\",\n"
                                        + "    \"resp\":\"06\"    \n"
                                        + "}", 200);
                            }

                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                            ExchangeUtils.sendResponse(exchange, response.getName(), 500);
                        }
                    } else {
                        resp = new GcbClient().processRopayTransactionsType2(productId, productType, amount, currency, customerName, bankReference, customerPhone, customerLevel, program, paymentName, description, companyId);
                        if (resp != null) {

                            JSONObject respJsonObj = new JSONObject(resp);

                            if (respJsonObj.optString("status").equals("00")) {
                                ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                                exchange.getResponseSender().send("{\n"
                                        + "    \"message\": \"" + respJsonObj.optString("message") + "\",\n"
                                        + "    \"transaction_details\": \"" + respJsonObj.optString("productDetails") + "\",\n"
                                        + "    \"sendSms\": \"" + respJsonObj.optString("smsMessage").split("~")[0] + "\",\n"
                                        + "    \"smsMessageToSend\": \"" + respJsonObj.optString("smsMessage").split("~")[1] + "\",\n"
                                        + "    \"resp\":\"00\"    \n"
                                        + "}");
                            } else {
                                ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                                ExchangeUtils.sendResponse(exchange, "{\n"
                                        + "    \"errorMessage\": \"" + respJsonObj.optString("message") + "\",\n"
                                        + "    \"resp\":\"06\"    \n"
                                        + "}", 200);
                            }

                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                            ExchangeUtils.sendResponse(exchange, response.getName(), 500);
                        }
                    }

                } else {
                    logger.warn("Invalid request. Uknown parameters");
                    ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "INVALID PARAMET ERS");
                    ExchangeUtils.sendResponse(exchange, "INVALID PARAMETERS", 400);
                }
            } else {
                ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "IP NOT ALLOWED");
                ExchangeUtils.sendResponse(exchange, "IP_NOT_ALLOWED", 401);
            }
        } catch (Exception e) {
            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "GENERAL EXCEPTION");
            logger.error("Error processing request", e);
            exchange.dispatch(ResponseCodeHandler.HANDLE_500);
        }

        logger.info(
                "<<< {} end of request , TAT {} ms" + LOG_PREFIX + (System.currentTimeMillis() - start));
    }

    private static boolean isIPAllowed(HttpServerExchange exchange) {
        String sourceIP = ExchangeUtils.getRemoteIP(exchange);
        logger.info(">>> new request received: {} from source IP: {}" + exchange.getQueryString() + sourceIP);
        String restrictIP = Config.getProperty("RESTRICT_IP");
        if (restrictIP != null & restrictIP.equals("1")) {
            String[] allowedIPs = Config.ALLOWED_IP_ADDRESSES.split("#");
            if (Arrays.asList(allowedIPs).contains(sourceIP)) {
                logger.info("REQUESTOR IP VALID. ALOW TO PROCEED");
                return true;
            } else {
                logger.info("REQUESTOR IP NOT ALLOWED");
                return false;
            }
        }
        logger.info("IP WHITELISTING DISABLED.");
        return true;
    }

}
