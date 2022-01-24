package com.etz.gh.kyc.test;

import com.etz.gh.kyc.model.KYCResponse;
import com.etz.gh.kyc.processor.Processor;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class Test extends Processor{

    @Override
    public KYCResponse doKYC(String msisdn) {
       return new KYCResponse("0","His Glory");
    }

    @Override
    public KYCResponse doKYC(String network, String msisdn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public KYCResponse doCustomerVerification(String customerNUm, String busKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
