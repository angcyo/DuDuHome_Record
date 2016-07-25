package com.dudu.rest.model;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by Administrator on 2016/2/15.
 */
public class GetRobberyStatusResponse extends RequestResponse {

    public Result result;

    public class Result {
        /**
         *审批状态
         * */
        public String audit_state;

        /**
         *开启时间
         * */
        public String robbery_open_time;

        /**
         *防劫开关状态
         * */
        public String protect_rob_state;

        /**
         *转速
         * */
        public String revolutions;

        /**
         *操作次数
         * */
        public String numberOfOperations;

        /**
         *完成时间
         * */
        public String completeTime;

        /**
         *证件地址
         * */
        public String insurance_url;

        @Override
        public String toString() {
            return "Result{" +
                    "audit_state='" + audit_state + '\'' +
                    ", robbery_open_time='" + robbery_open_time + '\'' +
                    ", protect_rob_state='" + protect_rob_state + '\'' +
                    ", revolutions='" + revolutions + '\'' +
                    ", numberOfOperations='" + numberOfOperations + '\'' +
                    ", completeTime='" + completeTime + '\'' +
                    ", insurance_url='" + insurance_url + '\'' +
                    '}';
        }
    }


}
