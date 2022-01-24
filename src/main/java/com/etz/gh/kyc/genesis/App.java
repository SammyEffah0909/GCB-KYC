/* 
 * 17042020 05:00
 * GIPKYC Service init class. class that starts and manages the lifecycle of app
 * 
 */
package com.etz.gh.kyc.genesis;

import com.etz.gh.kyc.server.Server;
import org.apache.log4j.Logger;


/**
 *
 * @author Seth Sebeh-Kusi
 */
public class App {

   final static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        new App().start();
    }

    public void start() {
        long start = System.currentTimeMillis();
        logger.info("*** GIPKYCService 1.0 Starting...");
        startServer();
        logger.info("*** GIPKYCService 1.0 started in {} ms. Ready to receive requests " + (System.currentTimeMillis() - start));
    }

    private void startServer() {
        Server.start();
    }

    private void stopServer() {
        Server.stop();
    }

}
