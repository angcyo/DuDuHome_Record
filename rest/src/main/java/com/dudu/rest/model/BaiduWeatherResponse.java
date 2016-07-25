package com.dudu.rest.model;

/**
 * Created by Administrator on 2016/5/5.
 */
public class BaiduWeatherResponse {
    public int errNum;
    public String errMsg;
    public BaiduWeatherResult retData;

    public class BaiduWeatherResult {
        /**
         * 城市名称
         */
        public String city;
        /**
         * 城市Id
         */
        public String cityid;
        /**
         * 今天的天气
         */
        public TodayResult today;

        /**
         * 空气指数
         */
        public WeatherIndex[] index;

        /**
         * 未来四天的天气预报
         */
        public ForecastResult[] forecast;

        /**
         * 过去七天的历史天气
         */
        public HistoryResult[] history;

        public class TodayResult {
            /**
             * 日期：
             * 2016-05-06
             */
            public String date;
            /**
             * 星期几
             */
            public String week;
            /**
             * 当前温度
             */
            public String curTemp;
            /**
             * 空气质量指数
             */
            public String aqi;
            /**
             * 风向
             */
            public String fengxiang;
            /**
             * 风力
             */
            public String fengli;

            /**
             * 最高温度
             */
            public String hightemp;

            /**
             * 最低温度
             */
            public String lowtemp;

            /**
             * 天气类型：晴、阴、多云等
             */
            public String type;
        }

        public class WeatherIndex {
            /**
             * 指数名称:
             * 感冒指数;
             * 防晒指数;
             * 穿衣指数;
             * 运动指数;
             * 洗车指数;
             * 晾晒指数
             */
            public String name;
            /**
             * 指数代号:
             * gm;
             * fs;
             * ct;
             * yd;
             * xc;
             * ls
             */
            public String code;

            /**
             * 程度：
             * 适宜;
             * 舒适;
             * 较适宜;
             * 较不宜;
             * 中等
             */
            public String index;

            /**
             * 详细情况
             */
            public String details;

            /**
             * 其他名称
             */
            public String otherName;
        }

        public class ForecastResult {
            /**
             * 日期：
             * 2016-05-06
             */
            public String date;
            /**
             * 星期几
             */
            public String week;
            /**
             * 风向
             */
            public String fengxiang;
            /**
             * 风力:
             * 3-4级
             */
            public String fengli;
            /**
             * 最高温度
             */
            public String hightemp;
            /**
             * 最低温度
             */
            public String lowtemp;
            /**
             * 天气类型：
             * 晴、阴、多云等
             */
            public String type;
        }

        public class HistoryResult {
            /**
             * 日期：
             * 2016-05-06
             */
            public String date;
            /**
             * 星期几
             */
            public String week;
            /**
             * 空气质量指数
             */
            public String aqi;
            /**
             * 风向
             */
            public String fengxiang;
            /**
             * 风力：
             * 3-4级
             */
            public String fengli;
            /**
             * 最高温度
             */
            public String hightemp;
            /**
             * 最低温度
             */
            public String lowtemp;
            /**
             * 天气类型：
             * 晴、阴、多云等
             */
            public String type;
        }
    }

}
