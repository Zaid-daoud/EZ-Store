package com.ez_store.ez_store.Model;

public class PaymentHelperClass {

    String payer_name, payer_account,payer_amount,payer_purpose,payer_mobile,isvalid;

    public PaymentHelperClass(String payer_name, String payer_account, String payer_amount, String payer_purpose, String payer_mobile,String isvalid) {
        this.payer_name = payer_name;
        this.payer_account = payer_account;
        this.payer_amount = payer_amount;
        this.payer_purpose = payer_purpose;
        this.payer_mobile = payer_mobile;
        this.isvalid=isvalid;
    }

    public String getIsvalid() {
        return isvalid;
    }

    public void setIsvalid(String isvalid) {
        this.isvalid = isvalid;
    }

    public String getPayer_name() {
        return payer_name;
    }

    public void setPayer_name(String payer_name) {
        this.payer_name = payer_name;
    }

    public String getPayer_account() {
        return payer_account;
    }

    public void setPayer_account(String payer_account) {
        this.payer_account = payer_account;
    }

    public String getPayer_amount() {
        return payer_amount;
    }

    public void setPayer_amount(String payer_amount) {
        this.payer_amount = payer_amount;
    }

    public String getPayer_purpose() {
        return payer_purpose;
    }

    public void setPayer_purpose(String payer_purpose) {
        this.payer_purpose = payer_purpose;
    }

    public String getPayer_mobile() {
        return payer_mobile;
    }

    public void setPayer_mobile(String payer_mobile) {
        this.payer_mobile = payer_mobile;
    }
}
