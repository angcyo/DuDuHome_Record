package com.record.event;

/**
 * Created by robi on 2016-06-12 15:42.
 */
public class RecordEvent {
    private boolean isRecord = false;

    public RecordEvent(boolean isRecord) {
        this.isRecord = isRecord;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }
}
