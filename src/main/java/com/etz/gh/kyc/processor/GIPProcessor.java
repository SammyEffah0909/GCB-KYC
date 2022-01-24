package com.etz.gh.kyc.processor;

import com.etz.gh.gmoneywallet.GMoney;
import com.etz.gh.gmoneywallet.GMoneyClient;
import com.etz.gh.gmoneywallet.GMoneyResponse;
import com.etz.gh.gmoneywallet.TripleDES;
import com.etz.gh.kyc.gcb.GcbClient;
import com.etz.gh.kyc.gcb.GcbResponse;
import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.util.Config;
import com.etz.gh.kyc.util.SuperDomParser;
import com.etz.gh.kyc.util.SuperHttpClient;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class GIPProcessor extends Processor {

    private static final Logger logger = LogManager.getLogger(GIPProcessor.class);
    TripleDES trp = new TripleDES();

    @Override
    public KYCResponse doKYC(String bankCode, String account) {
        final String LOG_PREFIX = bankCode + "***" + account + " ";
        String actCode = "-999";
        String aprvCode = "-999";
        String name = Config.getProperty("DEFAULT_NAME");
        KYCResponse response = new KYCResponse();
        response.setName(name);
        Map<String, String> rspMap = new HashMap<>();

        try {
            String sessionId = getRef(12);
            String trackingNumber = getRef(6);
            String datetime = getDatetime();
            String destbank = Config.getProperty("BANK_" + bankCode);
            String originBank = Config.ETZ_GIP_BANK_CODE;
            String original_account = account;

            if (account.startsWith("233")) {
                account = "0" + account.substring(3);
            } else if (account.startsWith("0233")) {
                account = "0" + account.substring(4);
            } else if (account.startsWith("00233")) {
                account = "0" + account.substring(5);
            }

            if (bankCode.equals("466")) {

                //eg 0233554538775 
                if (original_account.startsWith("0") && (original_account.substring(1).startsWith("233"))) {
                    original_account = original_account.substring(1);
                } //eg 0554538775
                else if (original_account.startsWith("0") && !original_account.startsWith("00") && !(original_account.substring(1).startsWith("233"))) {
                    original_account = "233" + original_account.substring(1);
                } //eg 00233554538775
                else if (original_account.startsWith("00") && (original_account.substring(2).startsWith("233"))) {
                    original_account = original_account.substring(2);
                } //eg 554538775
                else if (!original_account.startsWith("0") && !original_account.startsWith("233")) {
                    original_account = "233" + original_account;
                }
                //if (original_account.matches("\\d{12}")) {
                //  return true;
                //}

                logger.info("{} :: GMoney verification ", LOG_PREFIX);
                GMoney gmoney = new GMoney();
                gmoney.setOriginatorConversationID(getRef(10));
                gmoney.setThirdPartyID(Config.getProperty("GMONEY_THIRD_PARTY_ID"));
                gmoney.setOperatorId(Config.getProperty("GMONEY_KYC_IDENTIFIER"));
                gmoney.setPassword(trp.harden(Config.getProperty("GMONEY_PASSWORD")));
                gmoney.setMsisdn(original_account);
                gmoney.setSecurityCredential(trp.harden(Config.getProperty("GMONEY_KYC_PASSWORD")));
                GMoneyResponse grsp = GMoneyClient.customerKYC(gmoney, Config.getProperty("GMONEY_SYNC_URL"));
                if (grsp != null) {
                    if (grsp.getResultCode().equals("0")) {
                        if (grsp.getMiddleName() != null && grsp.getMiddleName().length() > 0) {
                            response.setName(grsp.getFirstName() + " " + grsp.getMiddleName() + " " + grsp.getLastName());
                        } else {
                            response.setName(grsp.getFirstName() + " " + grsp.getLastName());
                        }
                        response.setError(Config.getProperty("SUCCESS"));
                        response.setDescription("SUCCESSFUL");
                        logger.info("{} :: {} Verification successful", LOG_PREFIX, name);
                    } else {
                        response.setError(Config.getProperty("FAILED"));
                        response.setDescription("FAILED");
                        logger.info("{} Processing failed", LOG_PREFIX);
                    }
                } else {
                    response.setError(Config.getProperty("UNKNOWN_ERROR"));
                    response.setDescription("UNKNOWN ERROR");
                }

            } else if (bankCode.equals("004")) {
                //GCB KYC VERIFICATION
                logger.info("{} :: GCB verification ", LOG_PREFIX);

                GcbResponse grsp = new GcbClient().customerKYC(original_account);
                if (grsp != null) {
                    if (grsp.getStatus().equals("00")) {

                        response.setName(grsp.getMessage());

                        response.setError(Config.getProperty("SUCCESS"));
                        response.setDescription("SUCCESSFUL");
                        logger.info("{} :: {} Verification successful", LOG_PREFIX, name);
                    } else {
                        response.setError(Config.getProperty("FAILED"));
                        response.setDescription("FAILED");
                        logger.info("{} Processing failed", LOG_PREFIX);
                    }
                } else {
                    response.setError(Config.getProperty("UNKNOWN_ERROR"));
                    response.setDescription("UNKNOWN ERROR");
                }

            } else {
                String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"com.ghipss.gip\"><soapenv:Header/>\n"
                        + "<soapenv:Header/>\n"
                        + "<soapenv:Body>\n"
                        + "   <com:GIPTransactionOp>\n"
                        + "      <ReqGIPTransaction>\n"
                        + "         <Amount>000000000000</Amount>\n"
                        + "         <datetime>" + datetime + "</datetime>\n"
                        + "         <TrackingNum>" + trackingNumber + "</TrackingNum>\n"
                        + "         <FunctionCode>230</FunctionCode>\n"
                        + "         <OrigineBank>" + originBank + "</OrigineBank>\n"
                        + "         <DestBank>" + destbank + "</DestBank>\n"
                        + "         <SessionID>" + sessionId + "</SessionID>\n"
                        + "         <ChannelCode>001</ChannelCode>\n"
                        + "         <NameToDebit>N/A</NameToDebit>\n"
                        + "         <AccountToDebit>" + account + "</AccountToDebit>\n"
                        + "         <NameToCredit>Etranzact Gh</NameToCredit>\n"
                        + "         <AccountToCredit>" + account + "</AccountToCredit>\n"
                        + "         <Narration>PPayment</Narration>\n"
                        + "      </ReqGIPTransaction>\n"
                        + "   </com:GIPTransactionOp>\n"
                        + "</soapenv:Body>\n"
                        + "</soapenv:Envelope>";

                logger.info("{} payload {}", LOG_PREFIX, payload);

                rspMap = SuperHttpClient.doPostSSL(Config.GIP_URL, payload, Integer.parseInt(Config.SOCKET_TIMEOUT), Integer.parseInt(Config.SOCKET_TIMEOUT), Config.TRUSTSTORE_LOC, Config.TRUSTSTORE_PWD, Config.KEYSTORE_LOC, Config.KEYSTORE_PWD);
                //for testing calls mock service rspMap = SuperHttpClient.doGet(Config.GIP_URL);
                if (rspMap != null) {
                    logger.info("{} HTTP Response code received: {}", LOG_PREFIX, rspMap.get("code"));
                    logger.info("{} HTTP Response header received: {}", LOG_PREFIX, rspMap.get("header"));
                    logger.info("{} HTTP Response body received: {}", LOG_PREFIX, rspMap.get("body"));
                    if (rspMap.get("code").equals("200")) {
                        SuperDomParser dom = new SuperDomParser(rspMap.get("body"));
                        actCode = dom.getElementValue("ActCode");
                        logger.info("{} ActCode {}", LOG_PREFIX, actCode);
                        if (actCode.equals("000")) {
                            name = dom.getElementValue("NameToCredit");
                            response.setError(Config.getProperty("SUCCESS"));
                            response.setName(name);
                            response.setDescription("SUCCESSFUL");
                            logger.info("{} :: {} Verification successful", LOG_PREFIX, name);
                        } else {
                            response.setError(Config.getProperty("FAILED"));
                            response.setDescription("FAILED");
                            logger.info("{} Processing failed", LOG_PREFIX);
                        }
                        //aprvCode = dom.getElementValue("AprvCode");                   
                        //logger.info("{} AprvCode {}", LOG_PREFIX, aprvCode);
                    } else {
                        response.setError(Config.getProperty("FAILED"));
                        response.setDescription("FAILED");
                        logger.info("{} Transaction processing failed", LOG_PREFIX);
                    }
                } else {
                    response.setError(Config.getProperty("UNKNOWN_ERROR"));
                    response.setDescription("UNKNOWN ERROR");
                }
            }
        } catch (Exception e) {
            response.setError(Config.getProperty("GENERAL_EXCEPTION"));
            response.setDescription("GENERAL EXCEPTION");
            logger.error(LOG_PREFIX + " General Exception", e);
        }
        logger.info("{} Response: {}", LOG_PREFIX, response);

        return response;
    }

    @Override
    public KYCResponse doCustomerVerification(String customerNum, String businessKey) {
        final String LOG_PREFIX = customerNum + "***" + businessKey + " ";
        
        return null;
    }

    public static synchronized String getDatetime() {
        return (new Timestamp(System.currentTimeMillis()) + "").replaceAll("[^\\d]", "").substring(2, 14);
    }

    public static String getRef(int size) {
        String value = "";
        for (int t = 0; t < size; t++) {
            value = value + new Random().nextInt(9);
        }
        return value;
    }

    private static String paddZeros(String amt) {
        if (amt.length() == 12) {
            return amt;
        }
        int diff = 12 - amt.length();
        for (int i = 0; i < diff; i++) {
            amt = "0" + amt;
        }
        return amt;
    }

    @Override
    public KYCResponse doKYC(String msisdn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
