package com.dudu.workflow.tpms;

import java.util.List;

/**
 * Created by Administrator on 2016/5/2.
 */
public interface TpmsDataCallBack {
    public void onDatas(List<TirePressureData> result);
    public void onError(Exception error);
}
