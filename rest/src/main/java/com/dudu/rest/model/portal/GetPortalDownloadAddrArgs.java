package com.dudu.rest.model.portal;

/**
 * Created by Administrator on 2016/4/22.
 */
public class GetPortalDownloadAddrArgs {
    private String portalVersion;

    public GetPortalDownloadAddrArgs(String portalVersion) {
        this.portalVersion = portalVersion;
    }

    public String getPortalVersion() {
        return portalVersion;
    }

    public void setPortalVersion(String portalVersion) {
        this.portalVersion = portalVersion;
    }
}
