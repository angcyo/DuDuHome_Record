package com.dudu.network.message;

import com.dudu.commonlib.CommonLib;
import com.dudu.network.message.id.Bicker;
import com.dudu.network.message.id.BusinessMessageEnum;
import com.dudu.obd.common.AskMsg;
import com.dudu.obd.common.AskParams;
import com.dudu.obd.common.BusinessCode;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dengjun on 2016/3/7.
 * Description :
 */
public class TirePressureDataUpload extends MessagePackage<AskMsg> {
    private AskMsg askMsg;
    private String obeId;//obeid
    private String tirePressureData;

    public TirePressureDataUpload() {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.TIRE_PRESSURE_DATA.getCode()));
        askMsg.setCode(BusinessCode.TIRE_PRESSURE_UPLOAD);


    }

    public TirePressureDataUpload(String tirePressureData) {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.TIRE_PRESSURE_DATA.getCode()));
        askMsg.setCode(BusinessCode.TIRE_PRESSURE_UPLOAD);

        obeId = CommonLib.getInstance().getObeId();
        this.tirePressureData = tirePressureData;

        AskParams askParams = new AskParams();
        askParams.setContext(toJsonString());

        askMsg.setParams(askParams);
    }

    @Override
    public void setMessageId(String messageId) {
        askMsg.setMessageId(messageId);
    }

    @Override
    public String getMessageId() {
        return askMsg.getMessageId();
    }

    @Override
    public boolean isNeedWaitResponse() {
        return true;
    }

    @Override
    public boolean isNeedEncrypt() {
        return false;
    }

    @Override
    public void createFromJsonString(String messageJsonString) {
        try {
            JSONObject jsonObject = new JSONObject(messageJsonString);
            askMsg.setMessageId(jsonObject.getString("messageId"));
            askMsg.setCode(BusinessCode.TIRE_PRESSURE_UPLOAD);
            obeId =  jsonObject.getString("obeId");

            JSONObject tireDataJsonObject = new JSONObject();
            tireDataJsonObject.put("a", jsonObject.get("a"));
            tireDataJsonObject.put("b", jsonObject.get("b"));
            tireDataJsonObject.put("c", jsonObject.get("c"));
            tireDataJsonObject.put("d", jsonObject.get("d"));
            tireDataJsonObject.put("e", jsonObject.get("e"));
            tireDataJsonObject.put("f", jsonObject.get("f"));
            tireDataJsonObject.put("g", jsonObject.get("g"));
            tireDataJsonObject.put("h", jsonObject.get("h"));
            tireDataJsonObject.put("i", jsonObject.get("i"));
            tireDataJsonObject.put("j", jsonObject.get("j"));
            tirePressureData = tireDataJsonObject.toString();

            AskParams askParams = new AskParams();
            askParams.setContext(toJsonString());
            askMsg.setParams(askParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toJsonString() {
        JSONObject sendJsonObject =  new JSONObject();
        try {
            sendJsonObject.put("messageId", askMsg.getMessageId());
            sendJsonObject.put("obeId", obeId);

            JSONObject tireData = new JSONObject(tirePressureData);
            sendJsonObject.put("a",tireData.get("a"));
            sendJsonObject.put("b",tireData.get("b"));
            sendJsonObject.put("c",tireData.get("c"));
            sendJsonObject.put("d",tireData.get("d"));
            sendJsonObject.put("e",tireData.get("e"));
            sendJsonObject.put("f",tireData.get("f"));
            sendJsonObject.put("g",tireData.get("g"));
            sendJsonObject.put("h",tireData.get("h"));
            sendJsonObject.put("i",tireData.get("i"));
            sendJsonObject.put("j",tireData.get("j"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sendJsonObject.toString();
    }

    @Override
    public boolean isNeedCache() {
        return true;
    }

    @Override
    public AskMsg getMessageEntity() {
        return askMsg;
    }

    @Override
    public void setMessageEntity(AskMsg messageEntity) {
        askMsg = messageEntity;
    }

    @Override
    public String getBusinessCode() {
        return askMsg.getCode().getCode();
    }

    @Override
    public void setBusinessCode(String businessCode) {

    }
}
