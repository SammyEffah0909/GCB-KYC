package com.etz.gh.kyc.server;

import com.etz.gh.kyc.gcb.GcbClient;
import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.processor.GIPProcessor;
import com.etz.gh.kyc.util.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.util.Headers;
import java.util.Arrays;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author Seth Sebeh-Kusi 20042020
 */
public class GIPKYCHandler implements HttpHandler {

    private static final Logger logger = LogManager.getLogger(GIPKYCHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        long start = System.currentTimeMillis();
        ExchangeUtils.allowAllOrigin(exchange);
        ExchangeUtils.setHeader(exchange, Headers.CONTENT_TYPE, Config.CONTENT_TYPE_TEXT_PLAIN);
        String LOG_PREFIX = "";
        try {
            KYCResponse response = new KYCResponse();
            if (isIPAllowed(exchange)) {
                String bankcode = ExchangeUtils.getQueryParam(exchange, "bankcode");
                String account = ExchangeUtils.getQueryParam(exchange, "account");
                String businessKey = ExchangeUtils.getQueryParam(exchange, "businesskey");
                String custNum = ExchangeUtils.getQueryParam(exchange, "custnum");

                String companyId = ExchangeUtils.getQueryParam(exchange, "companyid");
                String productId = ExchangeUtils.getQueryParam(exchange, "productid");
                String custNumRopay = ExchangeUtils.getQueryParam(exchange, "rocustnum");

                String companyId4Prod = ExchangeUtils.getQueryParam(exchange, "company_id");
                String service = ExchangeUtils.getQueryParam(exchange, "service");
                String formType = ExchangeUtils.getQueryParam(exchange, "formtype");

                String phoneNum = ExchangeUtils.getQueryParam(exchange, "phone_num");

                String studentId = ExchangeUtils.getQueryParam(exchange, "student_id");

                if ((bankcode != null && account != null) && (bankcode.length() > 2 && account.length() > 3)) {
                    LOG_PREFIX = bankcode + "***" + account + " ";
                    response = new GIPProcessor().doKYC(bankcode, account);
                    if (response.getError().equals(Config.getProperty("SUCCESS"))) {
                        ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                        exchange.getResponseSender().send(response.getName());
                    } else {
                        ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                        ExchangeUtils.sendResponse(exchange, response.getName(), 500);
                    }
                } else if (businessKey != null && custNum != null) {
                    LOG_PREFIX = custNum + "***" + businessKey + " ";

                    String resp = new GcbClient().accountVerification(custNum, businessKey);
                    System.out.println("RESPONSE::: " + resp);
                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"name\": \"" + jsonObj.optString("studentName") + "\",\n"
                                    + "    \"program\": \"" + jsonObj.optString("program") + "\",\n"
                                    + "    \"clientid\": \"" + jsonObj.optString("registrationNumber") + "\",\n"
                                    + "    \"level\": \"" + jsonObj.optString("level") + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");
                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
                                    + "    \"resp\":\"06\"    \n"
                                    + "}", 200);
                        }

                    }
                } else if (service != null && formType != null) {
                    LOG_PREFIX = service + "***" + formType + " ";

                    String resp = new GcbClient().serviceFormsProducts(service, formType);
                    System.out.println("RESPONSE::: " + resp);
                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"name\": \"" + jsonObj.optString("productDetails").split("~")[0] + "\",\n"
                                    + "    \"amount\": \"" + jsonObj.optString("productDetails").split("~")[1] + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");
                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
                                    + "    \"resp\":\"06\"    \n"
                                    + "}", 200);
                        }

                    }
                } else if (phoneNum != null) {
                    LOG_PREFIX = phoneNum + "***" + " ";

                    String resp = new GcbClient().policeServiceFormsProducts(phoneNum);
                    System.out.println("RESPONSE::: " + resp);
                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"name\": \"" + jsonObj.optString("productDetails").split("~")[0] + "\",\n"
                                    + "    \"amount\": \"" + jsonObj.optString("productDetails").split("~")[1] + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");
                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
                                    + "    \"resp\":\"06\"    \n"
                                    + "}", 200);
                        }

                    }
                } else if (companyId != null && custNumRopay != null && productId != null) {
                    LOG_PREFIX = custNumRopay + "***" + companyId + " ";

                    String resp = new GcbClient().getRopayCustomerDetails(companyId, productId, custNumRopay);

                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"name\": \"" + jsonObj.optString("studentName") + "\",\n"
                                    + "    \"program\": \"" + jsonObj.optString("program") + "\",\n"
                                    + "    \"clientid\": \"" + jsonObj.optString("registrationNumber") + "\",\n"
                                    + "    \"level\": \"" + jsonObj.optString("level") + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");
                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
                                    + "    \"resp\":\"06\"    \n"
                                    + "}", 200);
                        }

                    } else {
                        ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                        ExchangeUtils.sendResponse(exchange, response.getName(), 500);
                    }
                } else if (companyId4Prod != null) {
                    LOG_PREFIX = companyId4Prod + "***" + " ";

                    String resp = new GcbClient().getRopayCompanyProducts(companyId4Prod);

                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"product_list\": \"" + jsonObj.optString("productDetails") + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");

                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
                                    + "    \"resp\":\"06\"    \n"
                                    + "}", 200);
                        }

                    } else {
                        ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", response.getDescription());
                        ExchangeUtils.sendResponse(exchange, response.getName(), 500);
                    }
                } else if (studentId != null) {
                    LOG_PREFIX = companyId4Prod + "***" + " ";

                    String resp = new GcbClient().aisStudentIdVerify(studentId);

                    if (resp != null) {

                        JSONObject jsonObj = new JSONObject(resp);

                        if (jsonObj.optString("status").equals("00")
                                && jsonObj.optString("message").equalsIgnoreCase("Lookup Successful")) {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "SUCCESSFUL");
                            exchange.getResponseSender().send("{\n"
                                    + "    \"name\": \"" + jsonObj.optString("studentName") + "\",\n"
                                    + "    \"school_id\": \"" + jsonObj.optString("schoolid") + "\",\n"
                                    + "    \"present_class\": \"" + jsonObj.optString("level") + "\",\n"
                                    + "    \"resp\":\"00\"    \n"
                                    + "}");
                        } else {
                            ExchangeUtils.setHeader(exchange, "X-DESCRIPTION", "FAILED");
                            ExchangeUtils.sendResponse(exchange, "{\n"
                                    + "    \"errorMessage\": \"" + jsonObj.optString("message") + "\",\n"
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
