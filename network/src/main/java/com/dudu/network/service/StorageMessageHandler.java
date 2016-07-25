package com.dudu.network.service;

import com.dudu.network.message.DriveHabitsDataUpload;
import com.dudu.network.message.LocationInfoUpload;
import com.dudu.network.message.ObdRtDataUpload;
import com.dudu.network.message.TirePressureDataUpload;
import com.dudu.network.message.id.BusinessCodeConstant;
import com.dudu.storage.Storage;
import com.dudu.storage.core.IReadCallBack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by dengjun on 2015/12/4.
 * Description :此类处理发送失败保存在文件的中的消息
 *                     发送失败的消息并不是所有的都需要保存，见协议文档
 */
public class StorageMessageHandler {
    private NetworkServiceNew networkServiceNew;
    private Logger log;

    public StorageMessageHandler(NetworkServiceNew networkService) {
        this.networkServiceNew = networkService;
        log = LoggerFactory.getLogger("network");
    }

    public void proStorageMessage(){
        log.info("处理发送失败后持久化的消息---");
        Storage.getInstance().readData(new IReadCallBack() {
            @Override
            public void onReadData(List<String> dataStringList) {
                try {
                    if (dataStringList!=null){
                        for(int i = 0; i < dataStringList.size(); i++){
                            String [] messageArray = dataStringList.get(i).split("@");//把消息的method字段和消息分割开
//                            log.debug("读取持久化消息：{}", dataStringList.get(i));
                            proStorageMessage(messageArray[0], messageArray[1]);
                        }
                        dataStringList.clear();
                    }
                } catch (Exception e) {
                    log.error("异常:{}",e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void proStorageMessage(String businessCode, String messageJsonString){
        try {
            switch (businessCode){
                //主动上传
                case BusinessCodeConstant.GPS_UPLOAD:
                    LocationInfoUpload locationInfoUpload = new LocationInfoUpload();
                    locationInfoUpload.createFromJsonString(messageJsonString);
                    networkServiceNew.sendMessage(locationInfoUpload);
                    break;
                case BusinessCodeConstant.OBD_UPLOAD:
                    ObdRtDataUpload obdRtDataUpload = new ObdRtDataUpload();
                    obdRtDataUpload.createFromJsonString(messageJsonString);
                    networkServiceNew.sendMessage(obdRtDataUpload);
                    break;
                case BusinessCodeConstant.DRIVE_HABIT_UPLOAD:
                    DriveHabitsDataUpload driveHabitsDataUpload = new DriveHabitsDataUpload();
                    driveHabitsDataUpload.createFromJsonString(messageJsonString);
                    networkServiceNew.sendMessage(driveHabitsDataUpload);
                    break;
                case BusinessCodeConstant.TIRE_PRESSURE_UPLOAD:
                    TirePressureDataUpload tirePressureDataUpload = new TirePressureDataUpload();
                    tirePressureDataUpload.createFromJsonString(messageJsonString);
                    networkServiceNew.sendMessage(tirePressureDataUpload);
                    break;
            }
        } catch (Exception e){
            log.error("异常:",e);
        }
    }
}
