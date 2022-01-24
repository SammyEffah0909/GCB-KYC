package com.etz.gh.kyc.gcb;

import com.etz.gh.kyc.model.PayNotifyRequest;
import com.etz.gh.kyc.util.Config;
import com.etz.gh.kyc.util.SuperHttpClient;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GcbClient {

    private static final Logger logger = LogManager.getLogger(GcbClient.class);

    public static String expiry = "";

    public static String access_token = "";

    public static Boolean tokenExpired = true;
    public static String verifyUrl = "";
    public static String authUrl = "";
    public static String username = "";
    public static String password = "";
    public static String grant_type = "";
    public static String cusNumVerifyURL = "";
    public static String getRopayCustDetailsURL = "";
    public static String getRopayCompProdURL = "";
    public static String processRopayPaymentURL = "";
    public static String serviceFormsURL = "";
    public static String servicePayNotifyURL = "";
    public static String policeServiceFormsURL = "";
    public static String policeServicePayNotifyURL = "";
    public static String aisVerifyURL = "";
    public static String aisPayNotifyURL = "";

    static {
        username = Config.getProperty("GCB_USERNAME");
        password = Config.getProperty("GCB_PASSWORD");
        authUrl = Config.getProperty("GCB_AUTHURL");
        verifyUrl = Config.getProperty("GCB_VERIFYURL");
        grant_type = Config.getProperty("GCB_GRANTTYPE");
        cusNumVerifyURL = Config.getProperty("GCB_CUSTVERIFYURL");
        getRopayCustDetailsURL = Config.getProperty("GCB_ROPAY_GETCUSTDETAIL");
        getRopayCompProdURL = Config.getProperty("GCB_ROPAY_GETCOMPRO");
        processRopayPaymentURL = Config.getProperty("GCB_ROPAY_PAYMENT");
        serviceFormsURL = Config.getProperty("GCB_SERVICEFORMSALES_URL");
        servicePayNotifyURL = Config.getProperty("GCB_SERVICEPAYNOTIFY_URL");
        policeServiceFormsURL = Config.getProperty("GCB_POLICE_VERIFY_URL");
        policeServicePayNotifyURL = Config.getProperty("GCB_POLICE_NOTIFY_URL");
        aisVerifyURL = Config.getProperty("AIS_VERIFY");
        aisPayNotifyURL = Config.getProperty("AIS_PAY_NOTIFY");
    }

    public static void main(String[] args) {
        String url = "https://devuatappsvr.gcbltd.com:99/ebus/api/epay/vouchers/ghana_prisons_service_form_sales/formType/General";
        String[] character;
//        System.out.println("107th:: " + url.charAt(10));
        System.out.println(getUniqueId());
    }

    public GcbResponse customerKYC(String account_number) {
        GcbResponse gcbresp = new GcbResponse();
        Map<String, String> rspMap = new HashMap<>();
        gcbresp.setStatus("99");
        try {
            if (!access_token.isEmpty()) {
                String url = verifyUrl.replace("#account_number#", account_number);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}", rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}", rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}", rspMap.get("body"));
                if (((String) rspMap.get("code")).equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    String name = json_.optString("accountName", "");
                    if (!name.isEmpty()) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(name);
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("No Name Found");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }
            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }
        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }
        return gcbresp;
    }

    public String accountVerification(String customerNum, String businessKey) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                String url = cusNumVerifyURL + "api/epay/business/" + businessKey + "/customers/" + customerNum;
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {

                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONObject details = json_.getJSONObject("details");
                    if (!details.isEmpty()) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(details.optString("message"));
                        gcbresp.setProgram(details.optString("program"));
                        gcbresp.setRegistrationNumber(details.optString("registrationNumber"));
                        gcbresp.setStudentName(details.optString("studentName"));
                        gcbresp.setLevel(details.optString("level"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String serviceFormsProducts(String service, String formType) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                System.out.println("ENDPOINT FROM Config:: " + serviceFormsURL);
                logger.info("ENDPOINT FROM Config:: " + serviceFormsURL);

                String url = serviceFormsURL.replace("#service#", service).replace("#formtype#", formType);
                System.out.println("ENDPOINT:: " + url);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {

                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    logger.info("amount::: " + json_.optString("amount"));
                    if (json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
//                        Name~Amount
                        gcbresp.setProductDetails(json_.optString("name") + "~" + json_.optString("amount"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String policeServiceFormsProducts(String applicantPhone) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                System.out.println("ENDPOINT FROM Config:: " + policeServiceFormsURL);
                logger.info("ENDPOINT FROM Config:: " + policeServiceFormsURL);

                String url = policeServiceFormsURL.replace("#applicantPhone#", applicantPhone.trim());
                System.out.println("ENDPOINT:: " + url);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));
                JSONObject json_ = new JSONObject(rspMap.get("body"));
                if (rspMap.get("code").equals("200")) {

                    logger.info("amount::: " + json_.optString("amount"));
                    if (json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
//                        Name~Amount
                        gcbresp.setProductDetails(json_.optString("name") + "~" + json_.optString("amount"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage(json_.optString("message"));
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String getRopayCompanyProducts(String companyId) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                String url = getRopayCompProdURL.replace("#companyId#", companyId);
//                logger.info("GET ROPAY COMPANY PRODUCTS EQUEST URL:: " + url);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONArray details = json_.getJSONArray("details");
                    String prodDetailsList = "";
                    if (!details.isEmpty()) {
                        gcbresp.setStatus("00");
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject object = details.optJSONObject(i);
                            prodDetailsList += object.optString("company_id") + "#" + object.optString("currency") + "#" + object.optString("description") + "#" + object.optString("payment_name") + "#" + object.optString("id") + "#" + object.optString("default_value") + "~";
                        }
                        gcbresp.setProductDetails(prodDetailsList);
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String getRopayCustomerDetails(String companyId, String productId, String custNum) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                String url = getRopayCustDetailsURL.replace("#companyId#", companyId).replace("#productId#", productId).replace("#custNum#", custNum);
//                logger.info("ROPAY GET CUSTOMER DETAILS REQUEST URL:: " + url);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONObject details = json_.getJSONObject("details");
                    if (!details.isEmpty()) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(details.optString("message"));
                        gcbresp.setProgram(details.optString("program"));
                        gcbresp.setRegistrationNumber(details.optString("indexno"));
                        gcbresp.setStudentName(details.optString("name"));
                        gcbresp.setLevel(details.optString("level"));
                        gcbresp.setStudentStatus(details.optString("status"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (JSONException e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String processRopayTransactions(String productId, String productType, String clientId, String amount, String currency, String customerName, String bankReference, String customerPhone, String customerLevel, String program, String paymentName, String description) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                PayNotifyRequest request = new PayNotifyRequest();

                request.setAmount(Double.parseDouble(amount));
                request.setBankReference(bankReference);
                request.setClientId(clientId);
                request.setCustomerLevel(customerLevel);
                request.setCustomerName(customerName);
                request.setCustomerPhone(customerPhone);
                request.setDescription(description);
                request.setPaymentName(paymentName);
                request.setProductId(productId);
                request.setProductType(productType);
                request.setProgram(program);
                request.setCurrency(currency);
                request.setTraceId(getUniqueId());

                logger.info("REQUEST BODY::: " + new JSONObject(request).toString());

                rspMap = SuperHttpClient.doPost(processRopayPaymentURL, new JSONObject(request).toString(), access_token);

                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONObject meta = json_.getJSONObject("meta");
                    JSONObject details = json_.getJSONObject("details");
                    if (!json_.isEmpty()) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setProductDetails("TransactionID: " + meta.optString("transactionId") + ">>> ReceiptNo: " + meta.optString("receiptNumber"));
                        gcbresp.setSmsMessage(details.optString("sendSms") + "~" + details.optString("smsMessage"));

                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage(json_.optString("message"));
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public static String getUniqueId() {
        String message = "";
        try {
            String tt = "";
            for (int s = 0; s < 3; s++) {
                tt = tt + new Random().nextInt(8);
            }
            Date d = new Date();
            DateFormat df = new SimpleDateFormat("ddHHmmss");
            String dt = df.format(d);
            message = dt + tt;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception: " + ex.getMessage());
        }
        return message;
    }

    public String processRopayTransactionsType2(String productId, String productType, String amount, String currency, String customerName, String bankReference, String customerPhone, String customerLevel, String program, String paymentName, String description, String companyId) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                PayNotifyRequest request = new PayNotifyRequest();

                request.setAmount(Double.parseDouble(amount));
                request.setBankReference(bankReference);
                request.setCustomerLevel(customerLevel);
                request.setCustomerName(customerName);
                request.setCustomerPhone(customerPhone);
                request.setDescription(description);
                request.setPaymentName(paymentName);
                request.setProductId(productId);
                request.setProductType(productType);
                request.setProgram(program);
                request.setCurrency(currency);
                request.setCompanyId(companyId);

                logger.info("REQUEST BODY::: " + new JSONObject(request).toString());

                rspMap = SuperHttpClient.doPost(processRopayPaymentURL, new JSONObject(request).toString(), access_token);

                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONObject meta = json_.getJSONObject("meta");
                    JSONObject details = json_.getJSONObject("details");
                    if (!json_.isEmpty()) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setProductDetails("TransactionID: " + meta.optString("transactionId") + "~ PIN: " + details.optString("PIN") + "~ SERIAL: " + details.optString("SERIAL"));
                        gcbresp.setSmsMessage(details.optString("sendSms") + "~" + details.optString("smsMessage"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage(json_.optString("message"));
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String servicePayNotify(String serviceKey, String formType, String bankRef, String fullName, String phoneNumber) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                PayNotifyRequest request = new PayNotifyRequest();
                request.setServiceKey(serviceKey);
                request.setFormType(formType);
                request.setBankRef(bankRef);
                request.setFullName(fullName);
                request.setPhoneNumber(phoneNumber);

                logger.info("REQUEST BODY::: " + new JSONObject(request).toString());

                rspMap = SuperHttpClient.doPost(servicePayNotifyURL, new JSONObject(request).toString(), access_token);

                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));

                    if (json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setSmsMessage(json_.optString("sendSms") + "~" + json_.optString("smsMessage"));

                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage(json_.optString("message"));
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String policeServicePayNotify(String serviceKey, String formType, String bankRef, String fullName, String phoneNumber, String applicantPhone, String mobileMoneyNetwork, String traceId, String amount) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                PayNotifyRequest request = new PayNotifyRequest();
                request.setTraceId(traceId);
                request.setApplicantPhone(applicantPhone.trim());
                request.setMobileMoneyNetwork(mobileMoneyNetwork);
                request.setAmount(Double.parseDouble(amount));
                request.setServiceKey(serviceKey);
                request.setFormType(formType);
                request.setBankRef(bankRef);
                request.setFullName(fullName);
                request.setPhoneNumber(phoneNumber);

                logger.info("REQUEST BODY::: " + new JSONObject(request).toString());

                rspMap = SuperHttpClient.doPost(policeServicePayNotifyURL, new JSONObject(request).toString(), access_token);

                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));

                    if (json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setSmsMessage(json_.optString("sendSms") + "~" + json_.optString("smsMessage"));

                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage(json_.optString("message"));
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage("An Error Occured");
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public static boolean refreshToken() {
        Boolean success = Boolean.valueOf(false);
        Map<String, String> rspMap = new HashMap<>();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("userName", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("grant_type", grant_type));
        rspMap = SuperHttpClient.doPostMultiPart(authUrl, params);
        if (rspMap != null) {
            logger.info("{} HTTP Response code received: {}", rspMap.get("code"));
            logger.info("{} HTTP Response header received: {}", rspMap.get("header"));
            logger.info("{} HTTP Response body received: {}", rspMap.get("body"));
            if (((String) rspMap.get("code")).equals("200")) {
                JSONObject json = new JSONObject(rspMap.get("body"));
                expiry = json.optString(".expires", "");
                access_token = json.optString("access_token", "");
                if (!access_token.isEmpty()) {
                    success = Boolean.valueOf(true);
                }
            }
        } else {
            logger.info("{}  HTTP ERROR Response code received: {}");
        }
        return success.booleanValue();
    }

    public String aisStudentIdVerify(String studentId) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                String url = aisVerifyURL.replace("#studentId#", studentId);
                rspMap = SuperHttpClient.doGet(url, access_token);
                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {

                    JSONObject json_ = new JSONObject(rspMap.get("body"));
                    JSONObject details = json_.getJSONObject("details");
                    if (!details.isEmpty() && json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setSchoolid(details.optString("school_id"));
                        gcbresp.setStudentid(details.optString("student_id"));
                        gcbresp.setStudentName(details.optString("student_name"));
                        gcbresp.setLevel(details.optString("present_class"));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage("verification failed");
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage((new JSONObject(rspMap.get("body")).optString("message")).isEmpty() ? new JSONObject(rspMap.get("body")).optString("errors") : new JSONObject(rspMap.get("body")).optString("message"));
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

    public String aisServicePaymentNotify(String serviceKey, String studentId, String bankRef, String fullName, String phoneNumber, String schoolId, String traceId, String amount, String feeType) {
        GcbResponse gcbresp = new GcbResponse();

        Map<String, String> rspMap = new HashMap<>();

        gcbresp.setStatus("99");
        try {

            if (!access_token.isEmpty()) {

                PayNotifyRequest request = new PayNotifyRequest();
                request.setTraceId(traceId);
                request.setSchoolId(schoolId);
                request.setAmount(Double.parseDouble(amount));
                request.setServiceKey(serviceKey);
                request.setStudentId(studentId);
                request.setBankRef(bankRef);
                request.setFullName(fullName);
                request.setPhoneNumber(phoneNumber);
                request.setFeeType(feeType);

                logger.info("REQUEST BODY::: " + new JSONObject(request).toString());

                rspMap = SuperHttpClient.doPost(aisPayNotifyURL, new JSONObject(request).toString(), access_token);

                logger.info("{} HTTP Response code received: {}" + rspMap.get("code"));
                logger.info("{} HTTP Response header received: {}" + rspMap.get("header"));
                logger.info("{} HTTP Response body received: {}" + rspMap.get("body"));

                if (rspMap.get("code").equals("200")) {
                    JSONObject json_ = new JSONObject(rspMap.get("body"));

                    if (json_.optString("status").equalsIgnoreCase("success")) {
                        gcbresp.setStatus("00");
                        gcbresp.setMessage(json_.optString("message"));
                        gcbresp.setSmsMessage(json_.optString("sendSms") + "~" + (json_.optString("smsMessage").isEmpty() ? "smsMessage not set!" : json_.optString("smsMessage")));

                    } else if (json_.optString("status").equalsIgnoreCase("error")) {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage((json_.optString("message").isEmpty() || json_.optString("message").equals("null")? "Error occurred" : json_.optString("message")));
                    } else {
                        gcbresp.setStatus("06");
                        gcbresp.setMessage(json_.optString("message"));
                    }
                } else {
                    gcbresp.setStatus("06");
                    gcbresp.setMessage((new JSONObject(rspMap.get("body")).optString("message")).isEmpty() ? new JSONObject(rspMap.get("body")).optString("errors") : new JSONObject(rspMap.get("body")).optString("message"));
                }

            } else {
                gcbresp.setStatus("06");
                gcbresp.setMessage("Could not retrieve token");
            }

        } catch (Exception e) {
            gcbresp.setStatus("06");
            gcbresp.setMessage("An exception error occured");
        }

        System.out.println("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        logger.info("FINAL RESPONSE::: " + new JSONObject(gcbresp).toString());
        return new JSONObject(gcbresp).toString();
    }

}
