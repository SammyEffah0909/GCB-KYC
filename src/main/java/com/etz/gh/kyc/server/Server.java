/*
 * 17042020 05:00
 */
package com.etz.gh.kyc.server;

import com.etz.gh.kyc.gcb.GcbClient;
import com.etz.gh.kyc.gcb.GcbTokenJob;
import com.etz.gh.kyc.util.Config;
import io.undertow.Undertow;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class Server {

    private static final Logger logger = LogManager.getLogger(Server.class);
    
//    final static Logger logger = Logger.getLogger(Server.class);
    private static Undertow server;

    public static void main(String[] args) {
        start();
    }

    public static void start() {

        long start = System.currentTimeMillis();
        logger.info("Starting server...");

        //SCHEDULERS
        try {
            logger.info("FETCHING NEW TOKEN");

            Boolean configComplete = GcbClient.refreshToken();
            if (configComplete) {
                logger.info("SETTING UP REFRESH SCHEDULE");
                JobDetail jobDetail1 = null;
                Trigger trigger1 = null;

                // Download Job
                String tokenJobSchedule = Config.getProperty("GCB_TOKEN_REFRESH_SCHEDULE");
                logger.info("GCB TOKEN REFRESH SCHEDULE ::: " + tokenJobSchedule);

                SchedulerFactory schedulerFactory = new StdSchedulerFactory();
                Scheduler scheduler = schedulerFactory.getScheduler();
                scheduler.start();

                if (!tokenJobSchedule.isEmpty()) {
                    logger.info("Creating REFRESH schedule");
                    jobDetail1 = JobBuilder.newJob(GcbTokenJob.class).withIdentity("tokenJob").build();
                    trigger1 = TriggerBuilder.newTrigger()
                            .withSchedule(CronScheduleBuilder.cronSchedule(tokenJobSchedule))
                            .build();
                    scheduler.scheduleJob(jobDetail1, trigger1);

                    try {
                        String host = Config.HOST;
                        int port = (Config.PORT);
                        Undertow.Builder builder = Undertow.builder()
                                .addHttpListener(port, host)
                                .setHandler(RouteHandler.ROUTES());
                        server = builder.build();
                        server.start();
                        long tat = System.currentTimeMillis() - start;
                        logger.info("Server started in {}ms; listening on {}:{}"+ "<>" + tat + "<>" + host + port);
                        logger.info("Server started in " + tat + " ms");
                    } catch (Exception er) {
                        logger.error("Error occured starting server", er);
                        logger.error("Shutting down...");
                    }

                } else {
                    logger.info("REFRESH TOKEN SCHEDULE NOT SET");
                }
            } else {

                logger.info("ERROR FETCHING TOKEN");
            }

        } catch (Exception e) {
            logger.error("Error occured starting server", e);
            logger.error("Shutting down...");
        }

    }

    public static void stop() {
    }

}
