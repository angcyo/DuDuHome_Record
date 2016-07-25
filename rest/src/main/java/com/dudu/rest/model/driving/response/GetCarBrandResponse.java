package com.dudu.rest.model.driving.response;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by Administrator on 2016/4/1.
 */
public class GetCarBrandResponse extends RequestResponse {

    public static final String AUDIT_STATE_UNAUDITED = "0";
    public static final String AUDIT_STATE_AUDITING = "1";
    public static final String AUDIT_STATE_AUDITED = "2";
    public static final String AUDIT_STATE_REJECT = "3";

    public GetCarBrandResult result;

    public static class GetCarBrandResult {
        /**
         * 品牌
         */
        public String brand;

        /**
         * 品牌obd代号
         */
        public long obd_car_no;

        /**
         * 型号
         */
        public String model;

        /**
         * 行驶证审核状态  0:未认证 1：审核中 2：审核通过 3：审核驳回
         */
        public String audit_state;

        /**
         * 车型
         * limousine:轿车
         * mvp:mvp
         * roadster:跑车
         * suv:suv
         */
        public String cars_category;

        @Override
        public String toString() {
            return "GetCarBrandResult{" +
                    "brand='" + brand + '\'' +
                    ", obd_car_no=" + obd_car_no +
                    ", model='" + model + '\'' +
                    ", audit_state='" + audit_state + '\'' +
                    ", cars_category='" + cars_category + '\'' +
                    '}';
        }
    }
}
