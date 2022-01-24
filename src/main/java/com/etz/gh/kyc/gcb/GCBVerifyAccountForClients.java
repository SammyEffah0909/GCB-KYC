/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.gh.kyc.gcb;

import com.etz.gh.kyc.model.TokenGenerationResponse;
import com.etz.gh.kyc.util.ConfigGet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author samuel.onwona
 */
//com.etz.gh.kyc.gcb.GCBVerifyAccountForClients
public class GCBVerifyAccountForClients {

    private static final Logger logger = LogManager.getLogger(GcbClient.class);
    
    public static void main(String[] args) {
        System.out.println(new GCBVerifyAccountForClients().verifyAccount(args[0], args[1]));
    }

    public String verifyAccount(String businessKey, String custNum) {

        Map<String, String> rspMap = new HashMap<>();
        JSONObject respObj = new JSONObject();
        try {

            TokenGenerationResponse loginResponse = getToken(ConfigGet.BASE_URL, ConfigGet.USERNAME, ConfigGet.PASSWORD, ConfigGet.GRANT_TYPE);
            System.out.println("****ACCOUNT VERIFICATION  REQUEST****");
            System.out.println(ConfigGet.BASE_URL + "api/epay/business/" + businessKey + "/customers/" + custNum);
            long start = System.currentTimeMillis();

            rspMap = SuperHttpClient.doGetMap(ConfigGet.BASE_URL + "api/epay/business/" + businessKey + "/customers/" + custNum, loginResponse.getAccess_token());
            System.out.println("->TAT" + (System.currentTimeMillis() - start));

            System.out.println("******ACCOUNT VERIFICATION RESPONSE*******");
            System.out.println(rspMap.get("body"));
            if (rspMap.get("code").equals("200")) {
                JSONObject json = new JSONObject(rspMap.get("body"));
                String status = json.optString("status");
                String message = json.optString("message");
                if (status.equalsIgnoreCase("success") && message.equalsIgnoreCase("Successful  Lookup")) {

//{"registrationNumber":"502019092","studentName":"TETTEY GIFTY","program":"PROGRAMME","level":"RGN 5"}
                    respObj.put("registrationNumber", json.getJSONObject("details").optString("registrationNumber"));
                    respObj.put("studentName", json.getJSONObject("details").optString("studentName"));
                    respObj.put("program", json.getJSONObject("details").optString("program"));
                    respObj.put("level", json.getJSONObject("details").optString("level"));
                    respObj.put("responseCode", "00");
                    respObj.put("responseMsg", message);

                } else {
                    respObj.put("responseCode", "06");
                    respObj.put("responseMsg", message);
                }
            } else {
                respObj.put("responseCode", "09");
                respObj.put("responseMsg", "verification failed... Did not get 200 response code!!");
            }

        } catch (Exception e) {
            respObj.put("responseCode", "09");
            respObj.put("responseMsg", "Verification failed... Exception occurred!");

        }
        return respObj.toString();
    }

    public TokenGenerationResponse getToken(String AuthEndPointURL, String username, String password, String grant_type) throws Exception {
        TokenGenerationResponse tokenGenResp = new TokenGenerationResponse();

        String access_token = ConfigGet.getValue("token");
        String expiry = ConfigGet.getValue("tokenExpiration");
//        String expiry = getPropertyValue("tokenExpiration");
//        String access_token = getPropertyValue("token");
        Boolean tokenExpired = true;
        try {
            DateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss S");
            Date checkTime = df.parse(df.format(new Date()));
            tokenExpired = checkTime.after(df.parse(expiry));

        } catch (Exception es) {
            System.out.println(es.getMessage());
            tokenExpired = true;
        }
        if (expiry == null || access_token == null || tokenExpired) {

            try {
                Map<String, String> rspMap = new HashMap<>();
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("userName", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("grant_type", grant_type));

                rspMap = SuperHttpClient.doPostMultiPart(AuthEndPointURL + "token", params);
//                System.out.println("RESP MAP>>> " + rspMap);
                if (rspMap != null) {

                    if (rspMap.get("code").equals("200")) {
                        JSONObject json = new JSONObject(rspMap.get("body"));

//                        System.out.println("RESPONSE>>> " + json);
                        expiry = json.optString("expires_in", "");
                        access_token = json.optString("access_token", "");
                        tokenGenResp.setAccess_token(json.getString("access_token"));
                        tokenGenResp.setToken_type(json.getString("token_type"));
                    }

                }

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss S");
                calendar.add(Calendar.MILLISECOND, Integer.parseInt(expiry));
                Date addMilliSeconds = calendar.getTime();

                System.out.println("Using New Token");

                ConfigGet.setValue(tokenGenResp.getAccess_token(), sdf.format(addMilliSeconds));
//                setPropertyValue("tokenExpiration", sdf.format(addMilliSeconds));
//                setPropertyValue("token", tokenGenResp.getAccess_token());

            } catch (JSONException ex) {
                System.out.println("TOKEN EXCEPTION=>" + ex.getMessage());
                logger.info((Object) ("TOKEN EXCEPTION=>" + ex.getMessage()));
            }
        } else {
            System.out.println("Old Token");
            logger.info((Object) ("Old Token"));
            tokenGenResp.setAccess_token(access_token);

        }
        return tokenGenResp;
    }

}
