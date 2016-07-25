package com.dudu.voice.semantic.bean;

/**
 * Created by Administrator on 2016/1/6.
 */
public class PhoneBean extends SemanticBean {

    private String contactName;

    private String phoneNumber;

    private String action;

    private String operator;

    private double conf = 0.65;

    public double getConf() {
        return conf;
    }

    public void setConf(double conf) {
        this.conf = conf;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {

        this.operator = operator;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
