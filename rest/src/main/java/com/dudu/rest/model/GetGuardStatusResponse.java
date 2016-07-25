package com.dudu.rest.model;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by Robi on 2016-03-0815:56.
 */
public class GetGuardStatusResponse extends RequestResponse {

    public GetGuardStatusResult result;

    public class GetGuardStatusResult {


        /**
         * 普通数字密码开关
         */
        public int protect_thief_state;

        /**
         * 普通数字密码
         */
        public String protect_thief_password;


        /**
         * 手势开关状态
         */
        public int protect_thief_signal_state;

        /**
         * 手势开关密码信息
         */
        public String protect_thief_signal_password;


        /**
         * 行驶证图片路径
         */
        public String driving_license_url;

        /**
         * 0:未认证 1：审核中 2：审核通过 3：审核驳回
         */
        public int audit_state;

        /**
         * 驾驶证图片路径
         */
        public String driver_license_url;

        /**
         * 防盗开启时间
         */
        public String thief_open_time;


        /**
         * 审核驳回的说明
         */
        public String audit_desc;

        /**
         * 防盗开关 0关 1开
         */
        public int thief_switch_state;

        @Override
        public String toString() {
            return "GetGuardStatusResult{" +
                    "protect_thief_state=" + protect_thief_state +
                    ", protect_thief_password='" + protect_thief_password + '\'' +
                    ", protect_thief_signal_state=" + protect_thief_signal_state +
                    ", protect_thief_signal_password='" + protect_thief_signal_password + '\'' +
                    ", driving_license_url='" + driving_license_url + '\'' +
                    ", audit_state=" + audit_state +
                    ", driver_license_url='" + driver_license_url + '\'' +
                    ", thief_open_time='" + thief_open_time + '\'' +
                    ", audit_desc='" + audit_desc + '\'' +
                    ", thief_switch_state=" + thief_switch_state +
                    '}';
        }
    }
}


