package com.dudu.rest.model.flow;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dengjun on 2016/3/17.
 * Description :
 */
public class FlowUpload {
    @SerializedName("obeId")
    private String obeId;

    @SerializedName("usedFlow")
    private float usedFlow;

    @SerializedName("createTime")
    private String createTime;

    public FlowUpload(float usedFlow, String obeId) {
        this.usedFlow = usedFlow;
        this.obeId = obeId;
        createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public String getObeId() {
        return obeId;
    }

    public void setObeId(String obeId) {
        this.obeId = obeId;
    }

    public float getUsedFlow() {
        return usedFlow;
    }

    public void setUsedFlow(float usedFlow) {
        this.usedFlow = usedFlow;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public JSONObject toJsonObject() throws JSONException {
        return new JSONObject(new Gson().toJson(this));
    }
}
