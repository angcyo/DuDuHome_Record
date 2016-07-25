package com.dudu.rest.model.portal;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by dengjun on 2016/4/23.
 * Description :
 */
public class UpdatePortalResponse extends RequestResponse {

    public UpdatePortalRes result;

    public class UpdatePortalRes {
        public String url;
        public String group;

        public UpdatePortalRes(String url, String group) {
            this.url = url;
            this.group = group;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }
}
