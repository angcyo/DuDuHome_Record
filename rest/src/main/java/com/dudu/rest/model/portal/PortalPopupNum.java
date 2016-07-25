package com.dudu.rest.model.portal;

/**
 * Created by dengjun on 2016/3/17.
 * Description :
 */
public class PortalPopupNum {
    private String obeId;
    private int   portalCount;//Portal记录数

    public PortalPopupNum(String obeId, int portalCount) {
        this.obeId = obeId;
        this.portalCount = portalCount;
    }

    public String getObeId() {
        return obeId;
    }

    public void setObeId(String obeId) {
        this.obeId = obeId;
    }

    public int getPortalCount() {
        return portalCount;
    }

    public void setPortalCount(int portalCount) {
        this.portalCount = portalCount;
    }
}
