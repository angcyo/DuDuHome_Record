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
public class DriveHabitsDataUpload extends MessagePackage<AskMsg> {
    private AskMsg askMsg;
    private String obeId;//obeid
    private String flamountData;

    public DriveHabitsDataUpload() {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.OBD_DRIVING_HABITS_DATA.getCode()));
        askMsg.setCode(BusinessCode.DRIVE_HABIT_UPLOAD);


    }

    public DriveHabitsDataUpload(String flamountData) {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.OBD_DRIVING_HABITS_DATA.getCode()));
        askMsg.setCode(BusinessCode.DRIVE_HABIT_UPLOAD);

        obeId = CommonLib.getInstance().getObeId();
        this.flamountData = flamountData;

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
            askMsg.setCode(BusinessCode.DRIVE_HABIT_UPLOAD);
            obeId =  jsonObject.getString("obeId");

            JSONObject flamountDataJsonObject = new JSONObject();
            flamountDataJsonObject.put("a", jsonObject.get("a"));
            flamountDataJsonObject.put("b", jsonObject.get("b"));
            flamountDataJsonObject.put("c", jsonObject.get("c"));
            flamountDataJsonObject.put("d", jsonObject.get("d"));
            flamountDataJsonObject.put("e", jsonObject.get("e"));
            flamountDataJsonObject.put("f", jsonObject.get("f"));
            flamountDataJsonObject.put("g", jsonObject.get("g"));
            flamountDataJsonObject.put("h", jsonObject.get("h"));
            flamountDataJsonObject.put("i", jsonObject.get("i"));
            flamountDataJsonObject.put("j", jsonObject.get("j"));
            flamountDataJsonObject.put("k", jsonObject.get("k"));
            flamountData = flamountDataJsonObject.toString();

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

            JSONObject flamData = new JSONObject(flamountData);
            sendJsonObject.put("a",flamData.get("a"));
            sendJsonObject.put("b",flamData.get("b"));
            sendJsonObject.put("c",flamData.get("c"));
            sendJsonObject.put("d",flamData.get("d"));
            sendJsonObject.put("e",flamData.get("e"));
            sendJsonObject.put("f",flamData.get("f"));
            sendJsonObject.put("g",flamData.get("g"));
            sendJsonObject.put("h",flamData.get("h"));
            sendJsonObject.put("i",flamData.get("i"));
            sendJsonObject.put("j",flamData.get("j"));
            sendJsonObject.put("k",flamData.get("k"));
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
