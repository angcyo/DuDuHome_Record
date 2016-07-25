package com.record.service;

import com.dudu.drivevideo.spaceguard.StorageSpaceService;

/**
 * Created by Administrator on 2016/6/6.
 */
public class StorageServiceManager {

    private static StorageServiceManager mStorageServiceManager = null;

    StorageServiceManager() {

    }

    public static StorageServiceManager getInstance() {
        if (mStorageServiceManager == null) {
            mStorageServiceManager = new StorageServiceManager();
        }
        return mStorageServiceManager;
    }

    public void startStorageService() {
        StorageSpaceService.getInstance().init();
    }

    public void stopStorageService() {
        StorageSpaceService.getInstance().release();
    }
}
