package com.etz.gh.kyc.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author seth.sebeh
 */
public class Config {

//    private static final Logger logger = LogManager.getLogger(Config.class);
    final static Logger logger = Logger.getLogger(Config.class);
    private static final Properties props = new Properties();
    public static final String HOST;
    public static final int PORT;

    public static final String GIP_URL;
    public static final String ETZ_GIP_BANK_CODE;
    public static final String SOCKET_TIMEOUT;
    public static final String TRUSTSTORE_LOC;
    public static final String TRUSTSTORE_PWD;
    public static final String KEYSTORE_LOC;
    public static final String KEYSTORE_PWD;
    public static final String ALLOWED_IP_ADDRESSES;

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plian";
    public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";

    public static final HashMap<String, String> SERVICE_MAP;

    static {
        try {
            props.load(new FileReader(new File("cfg" + File.separator + "ini.properties")));
        } catch (IOException ex) {
            logger.error("Error loading configuration parameters. Application start up failed. Check if app configuration file ini.properties exist in cfg folder before restarting the service again.", ex);
        }
        HOST = props.getProperty("HOST");
        PORT = Integer.parseInt(props.getProperty("PORT"));
        String[] service_props = props.getProperty("SERVICE").split("\\|");
        SERVICE_MAP = new HashMap<>();
        for (String service : service_props) {
            String[] a = service.split("=");
            SERVICE_MAP.put(a[0].trim().toUpperCase(), a[1].trim());
        }

        GIP_URL = props.getProperty("GIP_URL");
        ETZ_GIP_BANK_CODE = props.getProperty("ETZ_GIP_BANK_CODE");
        SOCKET_TIMEOUT = props.getProperty("SOCKET_TIMEOUT");
        TRUSTSTORE_LOC = props.getProperty("TRUSTSTORE_LOC");
        TRUSTSTORE_PWD = props.getProperty("TRUSTSTORE_PWD");
        KEYSTORE_LOC = props.getProperty("KEYSTORE_LOC");
        KEYSTORE_PWD = props.getProperty("KEYSTORE_PWD");
        ALLOWED_IP_ADDRESSES = props.getProperty("ALLOWED_IP_ADDRESSES");
    }

    private Config() {
    }

    public static void main(String[] args) {
        System.out.println(HOST);
        //System.out.println(SERVICE_MAP.get("B"));
        //System.out.println(SERVICE_MAP.get("GMONEY"));
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getPropertyEager(String key) {
        try {
            props.load(new FileReader(new File("cfg" + File.separator + "ini.properties")));
        } catch (Exception ex) {
            logger.error("Sorry something went bad ooo. Unable to load config data from file|database. ", ex);
        }
        return props.getProperty(key);
    }

}
