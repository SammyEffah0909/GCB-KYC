/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.gh.kyc.gcb;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author sunkwa-arthur
 */
public class GcbTokenJob implements Job {

    Logger log = Logger.getLogger(GcbTokenJob.class);

    @Override
    public void execute(JobExecutionContext pArg0) throws JobExecutionException {

        log.info("REFRESH TOKEN DAILY JOB >>> STARTED");
        try {

            GcbClient.refreshToken();

        } catch (Exception e) {
//            e.printStackTrace();
            log.info("REFRESH TOKEN DAILY JOB ::: ERROR::: " + e.getMessage());
        }
    }
}
