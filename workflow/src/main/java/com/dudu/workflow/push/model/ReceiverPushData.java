package com.dudu.workflow.push.model;

/**
 * Created by Administrator on 2016/3/29.
 */
public class ReceiverPushData {

    public ReceivedDataResult result;

    public int resultCode;

    public static class ReceivedDataResult {

        public String method;

        //防劫开关的状态
        public String robberySwitchs;

        //设置防劫完成的时间
        public String completeTime;

        //设置防劫的次数
        public String numberOfOperations;


        //设置防劫的转速
        public String revolutions;

        //防盗开关的状态
        public String thiefSwitchState;

        //手势密码
        public String protectThiefSignalPassword;

        //手势密码的开关状态
        public String protectThiefSignalState;

        //数字密码
        public String protectThiefPassword;

        //数字密码的开关状态
        public String protectThiefState;

        /**
         * 审核状态
         */
        public int audit_state;

        /**
         * 审核说明
         */
        public String audit_desc;

        /**
         * 车辆品牌
         */
        public String brand;

        /**
         * 车辆品牌代码
         */
        public int obd_car_no;

        /**
         * 加速测试ID
         */
        public String testFeedId;

        /**
         * 型号
         */
        public String model;

        /**
         * 车型
         * limousine:轿车
         * mvp:mvp
         * roadster:跑车
         * suv:suv
         */
        public String cars_category;

        /**
         * 测速类型
         * 1.100km/h 2.200km/h 3.300km/h
         */
        public String type;

        /**
         * 汽车排量
         */
        public String displacement;

        /**
         * 年份
         */
        public String year;

        /**
         * 请求者手机号码
         */
        public String phone;


        /* log上传Url*/
        public String logUploadUrl;

        /* 剩余流量*/
        public  String remainingFlow;
        /* 月度总流量*/
        public String monthMaxValue;

        @Override
        public String toString() {
            return "ReceivedDataResult{" +
                    "method='" + method + '\'' +
                    ", robberySwitchs='" + robberySwitchs + '\'' +
                    ", thiefSwitchState='" + thiefSwitchState + '\'' +
                    ", protectThiefSignalPassword='" + protectThiefSignalPassword + '\'' +
                    ", protectThiefSignalState='" + protectThiefSignalState + '\'' +
                    ", protectThiefPassword='" + protectThiefPassword + '\'' +
                    ", protectThiefState='" + protectThiefState + '\'' +
                    ", audit_state=" + audit_state +
                    ", audit_desc='" + audit_desc + '\'' +
                    ", brand='" + brand + '\'' +
                    ", obd_car_no=" + obd_car_no +
                    ", testFeedId='" + testFeedId + '\'' +
                    ", model='" + model + '\'' +
                    ", type='" + type + '\'' +
                    ", displacement='" + displacement + '\'' +
                    ", year='" + year + '\'' +
                    ", phone='" + phone + '\'' +
                    ", logUploadUrl='" + logUploadUrl + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReceiverPushData{" +
                "result=" + result +
                ", resultCode=" + resultCode +
                '}';
    }
}
