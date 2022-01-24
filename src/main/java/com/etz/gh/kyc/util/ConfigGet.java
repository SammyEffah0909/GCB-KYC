package com.etz.gh.kyc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class ConfigGet {

    public static void setValue(String tokenExpiration, LocalDateTime expiryDate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ConfigGet() {
    }
    private static final Logger log;

    private final static Properties props = new Properties();

    public static final String BASE_URL;
    public static final String GRANT_TYPE;
    public static final String USERNAME;
    public static final String PASSWORD;
    public static final String FT;
    public static final String TOPUP_BILL;
    public static final String PAYFEE;
    

    static {
        log = Logger.getLogger(ConfigGet.class.getName());
    }

    static {
        try {

            props.load(new FileReader(new File("cfg\\gcb.properties")));
        } catch (IOException ex) {
            System.out.println("Sorry something went bad ooo, " + ex.getMessage());
            log.info("Sorry something went bad ooo, " + ex.getMessage());
        }

        BASE_URL = props.getProperty("BASE_URL");
        GRANT_TYPE = props.getProperty("GRANT_TYPE");
        USERNAME = props.getProperty("USERNAME");
        PASSWORD = props.getProperty("PASSWORD");
        FT = props.getProperty("FT");
        TOPUP_BILL = props.getProperty("TOPUP_BILL");
        PAYFEE = props.getProperty("PAYFEE");
        
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static Object setProperty(String key, String value) {
        return props.setProperty(key, value);
    }

    public static String getValue(final String key) {

        final Properties prop = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream(new File("cfg\\gcb.properties"));

            prop.load(input);
            return prop.getProperty(key);
        } catch (IOException ex) {
            System.out.println("Config->CONFIG EXCEPTION=>" + ex.getMessage());
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println("Config->CONFIG EXCEPTION=>" + e.getMessage());
                }
            }
        }
    }

    public static void setValue(String token, String tokenExpiration) {
        try {
            PropertiesConfiguration conf = new PropertiesConfiguration(new File("cfg\\gcb.properties"));
            conf.setProperty("token", token);
            conf.setProperty("tokenExpiration", tokenExpiration);
            conf.save();
        } catch (ConfigurationException ex) {
            System.out.println("Config->CONFIG EXCEPTION=>" + ex.getMessage());
        }
    }
}
