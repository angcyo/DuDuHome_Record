package com.dudu.persistence.driving;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/8.
 */
public interface FaultCodeService {

    public Observable<FaultCode> findSwitch(int carCheckType);

    public Observable<FaultCode> saveSwitch(int type, String codes);
}
