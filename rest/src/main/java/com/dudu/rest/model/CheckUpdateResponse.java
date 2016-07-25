package com.dudu.rest.model;

import com.dudu.rest.model.common.RequestResponse;

import java.util.List;

/**
 * Created by Administrator on 2016/3/22.
 */
public class CheckUpdateResponse extends RequestResponse {

//    [{"createTime":"2016-03-21 20:22:30","fastdfsGroup":"group1","fastdfsId":"assd","fileName":"ty","fileSize":"20","obeType":"D1","suffix":".exe","upgradeId":11,"upgradeType":"1","userName":"Tom","version":"3"},{"createTime":"2016-03-21 20:17:30","fastdfsGroup":"group1","fastdfsId":"assd","fileName":"uu","fileSize":"20","obeType":"D1","suffix":".exe","upgradeId":4,"upgradeType":"2","userName":"Tom","version":"2"},{"createTime":"2016-03-21 20:22:30","fastdfsGroup":"group1","fastdfsId":"assd","fileName":"DD","fileSize":"20","obeType":"D1","suffix":".exe","upgradeId":10,"upgradeType":"3","userName":"Tom","version":"3"},{"createTime":"2016-03-21 20:21:30","fastdfsGroup":"group1","fastdfsId":"assd","fileName":"CC","fileSize":"20","obeType":"D1","suffix":".exe","upgradeId":9,"upgradeType":"4","userName":"Tom","version":"3"}],"resultCode":0}

    public List<AppUpdateInfo> result;

    public class AppUpdateInfo {
        public String createTime;
        /**
         * 下载group
         */
        public String fastdfsGroup;
        /**
         * 下载url
         */
        public String fastdfsId;
        public String fileName;
        public String fileSize;
        public String obeType;
        public String suffix;
        /**
         * 1.apk升级 2.Launcher升级 3.wifiportal 4.obd固件升级
         */
        public long upgradeId;
        public String upgradeType;
        public String userName;
        /* 服务器累加的版本号，上传一次新的文件，累加一次*/
        public String version;
        /* 软件版本号*/
        public String softwareVersion;
    }
}
