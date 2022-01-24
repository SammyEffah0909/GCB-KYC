/*
 * name: full name of customer
 * error: 0:successful, others failure
 * description: error description
 */
package com.etz.gh.kyc.model;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class KYCResponse {
    private String error;
    private String name;
    private String description;
    
    
    public KYCResponse() {}

    public KYCResponse(String error, String name) {
        this.error = error;
        this.name = name;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "KYCResponse{" + "error=" + error + ", name=" + name + ", description=" + description + '}';
    }
    
}
