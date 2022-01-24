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
public class GcbPoliceServicePayNotifyHandler implements HttpHandler {

    private static final Logger logger = LogManager.getLogger(GcbServicePayNotifyHandler.class);

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
                String serviceKey = jsonObj.optString("serviceKey");
                String formType = jsonObj.optString("formType");
                String bankRef = jsonObj.optString("bankRef");
                String fullName = jsonObj.optString("fullName");
                String phoneNumber = jsonObj.optString("phoneNumber");
                String amount = jsonObj.optString("amount");
                String traceId = jsonObj.optString("traceId");
                String applicantPhone = jsonObj.optString("applicantPhone").trim();
                String mobileMoneyNetwork = jsonObj.optString("mobileMoneyNetwork");

                if (bankRef != null && fullName != null && phoneNumber != null && formType != null && serviceKey != null && applicantPhone != null && amount != null) {
                    LOG_PREFIX = bankRef + "***" + phoneNumber + " ";
                    String resp = new GcbClient().policeServicePayNotify(serviceKey, formType, bankRef, fullName, phoneNumber, applicantPhone.trim(), mobileMoneyNetwork, traceId, amount);
                    if (resp != null) {

                        JSONObject respJsonObj = new JSONObject(resp);

                        if (respJsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"message\": \"" + respJsonObj.optString("message") + "\",\n"
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
