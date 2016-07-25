package com.dudu.rest.model.portal;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by dengjun on 2016/4/23.
 * Description :
 */
public class PortalVersionResponse extends RequestResponse {

    public PortalVersion result;

    public class PortalVersion {
        public String portalVersion;

        public String getPortalVersion() {
            return portalVersion;
        }

        public void setPortalVersion(String portalVersion) {
            this.portalVersion = portalVersion;
        }
    }
}
