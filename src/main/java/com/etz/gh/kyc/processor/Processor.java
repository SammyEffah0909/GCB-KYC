package com.etz.gh.kyc.processor;

import com.etz.gh.kyc.model.KYCResponse;

public abstract class Processor {
    //for direct
    public abstract KYCResponse doCustomerVerification(String customerNUm, String busKey);
    
    public abstract KYCResponse doKYC(String msisdn);
    
    //for gipdoKYC(msisdn)
    public abstract KYCResponse doKYC(String bankCode, String account);
}
