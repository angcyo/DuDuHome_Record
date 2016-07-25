package com.dudu.commonlib.utils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by Administrator on 2016/2/17.
 */
public class DataJsonTranslation {

    private static Logger logger = LoggerFactory.getLogger("DataJsonTranslation");

    public static String objectToJson(Object object){
        Gson gson = new Gson();
        String jsonString = gson.toJson(object);
        logger.debug(jsonString);
        return jsonString;
    }

    public static Object jsonToObject(String json, Class classOfJson){
        Gson gson = new Gson();
        return gson.fromJson(json,classOfJson);
    }

    /**
     * 将Json对象转换成Map
     *
     * @param jsonString
     *            json对象
     * @return Map对象
     * @throws JSONException
     */
    public static Map<String,String> toMap(String jsonString) throws JSONException {
        logger.debug("jsonString:"+jsonString);
        JSONObject jsonObject = new JSONObject(jsonString);
        Map<String,String> result = new HashMap<>();
        Iterator iterator = jsonObject.keys();
        String key = null;
        String value = null;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = jsonObject.getString(key);
            result.put(key, value);
        }
        return result;

    }
}
