package com.dudu.network.storage;

import com.dudu.network.storage.core.IReadCallBack;
import com.dudu.network.storage.service.StorageService;

/**
 * Created by dengjun on 2015/12/3.
 * Description :
 */
public class Storage {
    private static Storage instance = null;

    private StorageService storageService;

    public static Storage getInstance(){
        if (instance == null){
            synchronized (Storage.class){
                if (instance == null){
                    instance = new Storage();
                }
            }
        }
        return instance;
    }

    public Storage() {
        storageService = new StorageService();
    }

    public void init(){
//        storageService.saveData("monitor");
////        storageService.flush();
//        storageService.readData(null);
    }

    public void release(){
        storageService.flush();
        storageService.release();
    }

    //触发保存线程运行
    public void flush(){
        storageService.flush();
    }

    public void saveData(String dataString){
        storageService.saveData(dataString);
    }

    public void readData(IReadCallBack iReadCallBack){
        storageService.readData(iReadCallBack);
    }
}
