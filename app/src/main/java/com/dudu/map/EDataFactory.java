package com.dudu.map;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 外部程序接受到业务广播后，解析业务数据，并转化为BaseEData类型数据的，工程方法类。
 * 
 * @author huafeng.hf
 *
 */
public class EDataFactory {
	
	public static BaseEData create(String data) {
		
		try {
			BaseEData result = null;
			JSONObject jData = new JSONObject(data);
			String EDataType = jData.optString(BaseEData.KEY_EDATA_TYPE);
			if (!TextUtils.isEmpty(EDataType)) {
				if (EDataType.contains("ENaviInfo")) {

					Log.d("lxh","--------- navi ENaviInfo");
					result = new ENaviInfo();
				}
//				else if(EPOI.class.toString().equals(EDataType)){
//					result = new EPOI();
//				}
				else if(EDataType.contains("EPic")){
					result = new EPic();
				}
				
				if(result != null){					
					return result.unwrapFromJson(data);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
