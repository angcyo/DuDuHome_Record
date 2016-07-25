package com.dudu.aios.ui.map;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.dudu.aios.ui.map.observable.AddressResultObservable;
import com.dudu.aios.ui.voice.VoiceEvent;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.databinding.ActivityAddressSearchBinding;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import de.greenrobot.event.EventBus;

public class AddressSearchActivity extends Activity {

    private ActivityAddressSearchBinding activityAddressSearchBinding;

    private AddressResultObservable addressResultObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitiesManager.getInstance().addActivity(this);

        activityAddressSearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_address_search);
        addressResultObservable = new AddressResultObservable(activityAddressSearchBinding);
        activityAddressSearchBinding.setResultPage(addressResultObservable);


        activityAddressSearchBinding.cancelNavi.setOnClickListener(v -> finish());

        activityAddressSearchBinding.buttonVoice.setOnClickListener(v -> finish());

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (addressResultObservable != null) {
            addressResultObservable.setDefault();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addressResultObservable.release();
        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);
        NavigationProxy.getInstance().setShowList(false);
        ActivitiesManager.getInstance().removeActivity(this);
        VoiceManagerProxy.getInstance().stopSpeaking();
        VoiceManagerProxy.getInstance().onStop();
        EventBus.getDefault().unregister(this);

    }

    public void onEvent(VoiceEvent event) {

        switch (event) {
            case THRICE_UNSTUDIED:
                finish();
                break;
        }
    }
}
