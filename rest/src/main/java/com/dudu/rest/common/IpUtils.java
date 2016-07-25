package com.dudu.rest.common;

import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.Encrypt;
import com.dudu.rest.model.common.RequestArgs;
import com.dudu.rest.model.common.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/4/18.
 */
public class IpUtils {

    private static Logger logger = LoggerFactory.getLogger("IpUtils");

    public static RequestBody requestArgsToRequestBody(String method, Object requestArgsObject, String obeId) {
        String json = null;
        if (requestArgsObject != null) {
            json = DataJsonTranslation.objectToJson(requestArgsObject);
        }
        logger.debug("json" + json);
        RequestArgs requestArgs = new RequestArgs(method, json);

        return IpUtils.requestArgsEncrypt(requestArgs, obeId);
    }

    public static RequestBody requestArgsEncrypt(RequestArgs requestArgs, String obeId) {
        RequestBody requestBody = new RequestBody();
        try {
            String obeId_Md5 = Encrypt.MD5Encode(obeId);
            String obeId_Md5_16 = obeId_Md5.substring(8, 24);
            String json = DataJsonTranslation.objectToJson(requestArgs);
            requestBody.params = Encrypt.AESEncrypt(json, obeId_Md5_16);
            logger.debug("params:" + requestBody.params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestBody;
    }
}
