package com.dudu.voice.semantic.parser;

import com.aispeech.common.JSONResultParser;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.WeatherUtils;
import com.dudu.commonlib.utils.TimeUtils;
import com.dudu.voice.semantic.bean.BrightnessBean;
import com.dudu.voice.semantic.bean.ChooseCmdBean;
import com.dudu.voice.semantic.bean.CmdBean;
import com.dudu.voice.semantic.bean.FaultCmdBean;
import com.dudu.voice.semantic.bean.PhoneBean;
import com.dudu.voice.semantic.bean.PlayVideoBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.VolumeBean;
import com.dudu.voice.semantic.bean.WeatherBean;
import com.dudu.voice.semantic.bean.map.ChoosePageBean;
import com.dudu.voice.semantic.bean.map.CommonNaviBean;
import com.dudu.voice.semantic.bean.map.MapBean;
import com.dudu.voice.semantic.bean.map.MapPlaceNaviBean;
import com.dudu.voice.semantic.constant.SemanticConstant;

import org.json.JSONException;

/**
 * Created by Administrator on 2015/12/28.
 */
public class NativeJsonParser extends SemanticParser {

    private static final float MIN_ATH_THRESHOLD = 0.6f;

    @Override
    public SemanticBean getSemanticBean(String result) {
        mResultParser = new JSONResultParser(result);

        mSemantics = mResultParser.getSem();

        logger.debug("voice mResultParser.getConf() :" + mResultParser.getConf());

        String text = mResultParser.getRec();

        if (mResultParser.getConf() >= MIN_ATH_THRESHOLD) {

            switch (getDomain()) {
                case SemanticConstant.SERVICE_VOLUME:
                    return parseVolumeBean(text);
                case SemanticConstant.SERVICE_CMD:
                    return parseCmdBean(text);
                case SemanticConstant.SERVICE_PHONE:
                    return parsePhoneBean(text);
                case SemanticConstant.SERVICE_CHOOSE_PAGE:
                    return parseChoosePageBean(text);
                case SemanticConstant.SERVICE_CHOOSE_CMD:
                    return parseMapChooseBean(text);
                case SemanticConstant.SERVICE_CHOOSE_STRATEGY:
                    return parseMapChooseBean(text);
                case SemanticConstant.SERVICE_FAULT_CMD:
                    return parseFaultCmdBean(text);
                case SemanticConstant.SERVICE_WEATHER:
                    return parseWeatherBean(text);
                case SemanticConstant.SERVICE_MAP:
                case SemanticConstant.SERVICE_FOOD_SEARCH:
                    return parseMapBean(text);
                case SemanticConstant.SERVICE_BRIGHTNESS:
                    return parseBrightnessBean(text);
                case SemanticConstant.SERVICE_PLAY_VIDEO:
                    return parsePlayVideoBean(text);
                case SemanticConstant.SERVICE_COMMON_NAVI:
                    return parseCommonNaviBean(text);

            }
        }

        if (LauncherApplication.getContext().isNeedSaveVoice()) {

            return SemanticBean.getDefaultBean(text);
        }
        return null;
    }

    private String getDomain() {
        try {
            return mSemantics.getString("domain");
        } catch (JSONException e) {
            logger.error("解析json出错： " + e.getMessage());
        }

        return "";
    }

    private VolumeBean parseVolumeBean(String text) {
        VolumeBean bean = new VolumeBean();
        bean.setHasResult(true);
        bean.setText(text);
        bean.setService(mSemantics.optString(SemanticConstant.DOMAIN));
        bean.setOperation(mSemantics.optString(SemanticConstant.ACTION));
        return bean;
    }

    private CmdBean parseCmdBean(String text) {
        CmdBean bean = new CmdBean();
        bean.setHasResult(true);
        bean.setText(text);
        bean.setService(SemanticConstant.SERVICE_CMD);
        bean.setTarget(mSemantics.optString(SemanticConstant.TARGET));
        bean.setAction(mSemantics.optString(SemanticConstant.ACTION));
        return bean;
    }

    private PhoneBean parsePhoneBean(String text) {
        PhoneBean bean = new PhoneBean();
        bean.setHasResult(true);
        bean.setText(text);
        bean.setService(SemanticConstant.SERVICE_PHONE);
        bean.setConf(mResultParser.getConf());
        if (!mSemantics.isNull(SemanticConstant.PERSON)) {
            bean.setContactName(mSemantics.optString(SemanticConstant.PERSON));
        }

        if (!mSemantics.isNull(SemanticConstant.NUMBER)) {
            bean.setPhoneNumber(mSemantics.optString(SemanticConstant.NUMBER));
        }

        if (!mSemantics.isNull(SemanticConstant.ACTION)) {
            bean.setAction(mSemantics.optString(SemanticConstant.ACTION));
        }

        if (!mSemantics.isNull("operator")) {
            bean.setOperator(mSemantics.optString("operator"));
        }


        return bean;
    }

    private ChoosePageBean parseChoosePageBean(String text) {
        ChoosePageBean choosePageBean = new ChoosePageBean();
        choosePageBean.setText(text);
        choosePageBean.setService(SemanticConstant.SERVICE_CHOOSE_PAGE);
        choosePageBean.setHasResult(true);
        choosePageBean.setAction(mSemantics.optString(SemanticConstant.ACTION));
        return choosePageBean;
    }

    private ChooseCmdBean parseMapChooseBean(String text) {

        ChooseCmdBean mapChooseBean = new ChooseCmdBean();
        mapChooseBean.setHasResult(true);
        mapChooseBean.setChoose_number(mSemantics.optString("choose_number"));

        if (getDomain().equals(SemanticConstant.SERVICE_CHOOSE_STRATEGY))
            mapChooseBean.setChoose_type(SemanticConstant.SERVICE_CHOOSE_STRATEGY);
        else
            mapChooseBean.setChoose_type(mSemantics.optString("type"));

        mapChooseBean.setText(text);
        mapChooseBean.setService(SemanticConstant.SERVICE_CHOOSE_CMD);

        return mapChooseBean;
    }

    private FaultCmdBean parseFaultCmdBean(String text) {

        FaultCmdBean faultCmdBean = new FaultCmdBean();
        faultCmdBean.setAction(mSemantics.optString("action"));
        faultCmdBean.setText(text);
        faultCmdBean.setHasResult(true);
        faultCmdBean.setService(SemanticConstant.SERVICE_FAULT_CMD);

        return faultCmdBean;
    }

    private WeatherBean parseWeatherBean(String text) {

        WeatherBean weatherBean = new WeatherBean();
        weatherBean.setHasResult(true);
        weatherBean.setText(text);
        weatherBean.setService(SemanticConstant.SERVICE_WEATHER);
        switch (mSemantics.optString("date")) {
            case "今天":
                weatherBean.setDate(TimeUtils.format(TimeUtils.format2));
                break;
            case "明天":
                weatherBean.setDate(WeatherUtils.getDate(+1));
                break;
            case "后天":
                weatherBean.setDate(WeatherUtils.getDate(+2));
                break;
        }
        weatherBean.setCity(WeatherUtils.getCurrentCity());

        return weatherBean;
    }

    private MapBean parseMapBean(String text) {

        MapPlaceNaviBean mapBean = new MapPlaceNaviBean();
        mapBean.setHasResult(true);
        mapBean.setService(SemanticConstant.SERVICE_MAP);
        mapBean.setPoiName(mSemantics.optString("poiName"));
        mapBean.setText(text);
        return mapBean;
    }

    private BrightnessBean parseBrightnessBean(String text) {
        BrightnessBean brightnessBean = new BrightnessBean();
        brightnessBean.setHasResult(true);
        brightnessBean.setText(text);
        brightnessBean.setAction(mSemantics.optString(SemanticConstant.ACTION));
        brightnessBean.setService(SemanticConstant.SERVICE_BRIGHTNESS);
        return brightnessBean;
    }

    private PlayVideoBean parsePlayVideoBean(String text) {
        PlayVideoBean playVideoBean = new PlayVideoBean();
        playVideoBean.setChoose_number(mSemantics.optString("choose_number"));
        playVideoBean.setText(text);
        playVideoBean.setHasResult(true);
        playVideoBean.setService(SemanticConstant.SERVICE_PLAY_VIDEO);
        return playVideoBean;
    }

    private CommonNaviBean parseCommonNaviBean(String text){
        CommonNaviBean comm = new CommonNaviBean();
        comm.setHasResult(true);
        comm.setText(text);
        comm.setService(SemanticConstant.SERVICE_COMMON_NAVI);
        comm.setAction(mSemantics.optString("action"));
        return comm;
    }
}
