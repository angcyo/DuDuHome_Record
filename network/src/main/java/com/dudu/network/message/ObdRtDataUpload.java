package com.dudu.network.message;

import com.dudu.commonlib.CommonLib;
import com.dudu.network.message.id.Bicker;
import com.dudu.network.message.id.BusinessMessageEnum;
import com.dudu.obd.common.AskMsg;
import com.dudu.obd.common.AskParams;
import com.dudu.obd.common.BusinessCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dengjun on 2016/3/7.
 * Description :
 */
public class ObdRtDataUpload extends MessagePackage<AskMsg> {
    private AskMsg askMsg;
    private String obeId;//obeid
    private JSONArray obds;

    public ObdRtDataUpload() {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.OBD_DATA.getCode()));
        askMsg.setCode(BusinessCode.OBD_UPLOAD);


    }

    public ObdRtDataUpload(JSONArray obdDataJsonArray) {
        askMsg = new AskMsg();
        askMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.OBD_DATA.getCode()));
        askMsg.setCode(BusinessCode.OBD_UPLOAD);

        obeId = CommonLib.getInstance().getObeId();
        obds = obdDataJsonArray;

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
            askMsg.setCode(BusinessCode.OBD_UPLOAD);
            obeId =  jsonObject.getString("obeId");
            obds =  jsonObject.getJSONArray("obds");

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
            sendJsonObject.put("obds", obds);

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
