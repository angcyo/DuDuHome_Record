package com.record.service;

/**
 * Created by robi on 2016-05-07 10:50.
 */
public class InfoBean {
    public String record_state;

    public InfoBean() {
    }

    public InfoBean(String record_state) {
        this.record_state = record_state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"record_state\":");
        builder.append("\"");
        builder.append(record_state);
        builder.append("\"}");
        return builder.toString();
    }
}
