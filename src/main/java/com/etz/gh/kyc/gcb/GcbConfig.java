/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etz.gh.kyc.gcb;

/**
 *
 * @author sunkwa-arthur
 */
public class GcbConfig {
    private String url;
    private String verifyUrl;
    private String username;
    private String password;
    private String grant_type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public void setGrant_type(String grant_type) {
        this.grant_type = grant_type;
    }

    public String getVerifyUrl() {
        return verifyUrl;
    }

    public void setVerifyUrl(String verifyUrl) {
        this.verifyUrl = verifyUrl;
    }

    @Override
    public String toString() {
        return "GcbConfig{" + "url=" + url + ", verifyUrl=" + verifyUrl + ", username=" + username + ", password=" + password + ", grant_type=" + grant_type + '}';
    }

    
    
    
}
