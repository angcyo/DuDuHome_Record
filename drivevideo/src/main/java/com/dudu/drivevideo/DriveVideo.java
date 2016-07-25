package com.dudu.drivevideo;

import android.widget.ImageView;

/**
 * Created by dengjun on 2016/1/26.
 * Description :
 */
public class DriveVideo {
    private static DriveVideo  instance = null;



    public static DriveVideo getInstance(){
        if (instance == null){
            synchronized (DriveVideo.class){
                if (instance == null){
                    instance = new DriveVideo();
                }
            }
        }
        return instance;
    }

    private DriveVideo() {

    }

    public void startDriveVideo(){

    }

    public void stopDriveVideo(){

    }

    public void setImageView(ImageView imageView){

    }



}
