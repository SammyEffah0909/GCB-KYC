package com.etz.gh.kyc.util;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class GeneralUtils {

    private static AtomicLong reference = new AtomicLong(0);
    private static final String ALPHABET = "01C5ARSTUV6789ABC1DEB8FG45678HIJKL4M01239NOPQ234WRXY6Z";
    private static int refLength = 13;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println(getReferenceAtomic());
        System.out.println("tat " + (System.currentTimeMillis() - start) + " ms");
    }

    public static String generateReference() {
        Random RANDOM = new SecureRandom();
        StringBuilder returnValue = new StringBuilder(refLength);
        for (int i = 0; i < refLength; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return "02" + new String(returnValue);
    }

    public static String getReferenceAtomic() {
        return formatReference(reference.getAndIncrement());
    }

    private static String formatReference(long param) {
        String prefix = "";
        int paramLength = (String.valueOf(param)).length();
        if (refLength > paramLength) {
            for (int i = 0; i < (refLength - paramLength); i++) {
                prefix = prefix + "0";
            }
        }else{
            return "02MG" + param;
        }
        return "02MG" + prefix + "" + param;
    }

    public static String generateUniqueId() {
        String message = "";
        try {
            String tt = "";
            for (int s = 0; s < 5; s++) {
                tt = tt + (new Random()).nextInt(9);
            }
            System.out.println(tt);
            Date d = new Date();
            DateFormat df = new SimpleDateFormat("MMddHHmmss");
            String dt = df.format(d);
            message = "02MG" + dt + tt;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return message;
    }

}
