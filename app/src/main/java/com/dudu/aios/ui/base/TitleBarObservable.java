package com.dudu.aios.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.location.GpsSatellite;
import android.location.GpsStatus;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.NetworkUtils;
import com.dudu.android.launcher.utils.StatusBarManager;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.event.Events;
import com.dudu.drivevideo.frontcamera.event.VideoEvent;
import com.dudu.event.DeviceEvent;
import com.dudu.monitor.repo.location.LocationManage;

import java.util.Iterator;

import de.greenrobot.event.EventBus;

/**
 * Created by lxh on 2016/1/20.
 */
public class TitleBarObservable {

    public final ObservableBoolean bluetooth = new ObservableBoolean();

    public final ObservableBoolean gps = new ObservableBoolean();

    public final ObservableBoolean video = new ObservableBoolean();

    public final ObservableField<Drawable> single_drawable = new ObservableField();

    public final ObservableField<String> single = new ObservableField<>();

    public final ObservableBoolean showSingle = new ObservableBoolean();

    public final ObservableBoolean guardOpened = new ObservableBoolean();

    public final ObservableField<String> version = new ObservableField<>();

    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private ConnectivityChangeReceiver mConnectivityReceiver;

    public TitleBarObservable() {
        setDefaultState();
    }

    private int mSatellite = 0;

    private static final int SIM_SIGNAL_IMAGE_IDS[] = {
            R.drawable.signal_0,
            R.drawable.signal_1,
            R.drawable.signal_2,
            R.drawable.signal_3,
            R.drawable.signal_4,
            R.drawable.signal_5
    };

    private void setDefaultState() {
        this.bluetooth.set(false);
        this.gps.set(false);
        this.video.set(false);
        this.showSingle.set(false);
        this.guardOpened.set(false);
        this.single_drawable.set(LauncherApplication.getContext().getResources().getDrawable(R.drawable.signal_0));
        this.single.set(LauncherApplication.getContext().getString(R.string.no_4g_signal));
    }

    public void init() {
        EventBus.getDefault().register(this);
        setSimLevel(StatusBarManager.getInstance().getSignalLevel() + 1);

        mConnectivityReceiver = new ConnectivityChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONNECTIVITY_CHANGE);
        CommonLib.getInstance().getContext().registerReceiver(mConnectivityReceiver, intentFilter);
    }

    private void setSimLevel(int level) {
        if (level < 0 || level >= SIM_SIGNAL_IMAGE_IDS.length) {
            return;
        }
        single_drawable.set(LauncherApplication.getContext().getResources().getDrawable(SIM_SIGNAL_IMAGE_IDS[level]));

    }

    public void onEventMainThread(GpsStatus gpsStatus) {
        int maxSatellites = gpsStatus.getMaxSatellites();
        Iterator<GpsSatellite> iterator = gpsStatus.getSatellites()
                .iterator();
        mSatellite = 0;
        while (iterator.hasNext() && mSatellite <= maxSatellites) {
            mSatellite++;
        }
        if (mSatellite > 0 && (!LocationManage.getInstance().getCurrentLocation().getProvider().equals("lbs"))) {
            gps.set(true);
        } else {
            gps.set(false);
        }

    }


    public void onEventMainThread(DeviceEvent.Video event) {
        StatusBarManager.getInstance().setRecording(event.getState());
        video.set(event.getState() == DeviceEvent.ON);
    }

    public void onEventMainThread(VideoEvent videoEvent) {
        StatusBarManager.getInstance().setRecording(videoEvent.getState());
        video.set(videoEvent.getState() == DeviceEvent.ON);
    }


    public void onEventMainThread(DeviceEvent.SimLevel simLevel) {
        setSimLevel(simLevel.getSimLevel() + 1);
    }

    public void onEventMainThread(DeviceEvent.SimType simType) {
        showSingle(simType.getSimType());
    }

    public void onEventMainThread(Events.GuardSwitchState guardSwitchState) {
        this.guardOpened.set(guardSwitchState.isOpen());
    }

    public void onEventMainThread(DeviceEvent.BluetoothState bluetoothState) {
        switch (bluetoothState.getState()) {
            case DeviceEvent.ON:
                bluetooth.set(true);
                break;
            case DeviceEvent.OFF:
                bluetooth.set(false);
                break;
        }
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {
                String type = NetworkUtils.getCurrentNetworkType(CommonLib.getInstance().getContext());
                setSimLevel(StatusBarManager.getInstance().getSignalLevel());
                showSingle(type);
            }

        }
    }


    private void showSingle(String type) {
        single.set(type);
        if (type.equals("2G") || type.equals("3G") || type.equals("4G")) {
            showSingle.set(true);
        } else {
            showSingle.set(false);
        }
    }
}
