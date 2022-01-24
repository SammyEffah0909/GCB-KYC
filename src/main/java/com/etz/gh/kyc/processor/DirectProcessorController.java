package com.etz.gh.kyc.processor;

import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class DirectProcessorController {

    private static final Logger logger = LogManager.getLogger(Processor.class);

    public KYCResponse process(String network, String msisdn) {
        final String LOG_PREFIX = network + "***" + msisdn + " ";
        KYCResponse response = new KYCResponse();
        try {
            String className = Config.SERVICE_MAP.get(network.toUpperCase());
            Processor processor = (Processor) Class.forName(className).newInstance();
            response = processor.doKYC(msisdn);
        } catch (ClassNotFoundException ex) {
            response.setError("1");
            logger.error(LOG_PREFIX + " KCY Processor Class Not Found Exception", ex);
        } catch (InstantiationException ex) {
            logger.error(LOG_PREFIX + " KCY Processor Class Instantiaton Exception", ex);
            response.setError("2");
        } catch (IllegalAccessException ex) {
            logger.error(LOG_PREFIX + " Illegal Access Exception", ex);
            response.setError("3");
        } catch (Exception e) {
            logger.error(LOG_PREFIX + " General Exception", e);
            response.setError("6");
        }
        logger.info("{} Response: {}", LOG_PREFIX, response);
        
        return response;
    }
}
