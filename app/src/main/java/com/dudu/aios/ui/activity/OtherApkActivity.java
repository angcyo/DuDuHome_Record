package com.dudu.aios.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dudu.aios.ui.bean.AppInfo;
import com.dudu.aios.ui.utils.InstallerUtils;
import com.dudu.android.launcher.R;
import com.dudu.navi.event.NaviEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class OtherApkActivity extends Activity implements View.OnClickListener {

    private ListView mOtherApkListView;

    private ArrayList<AppInfo> mOtherApkData;

    private OtherApkAdapter mOtherApkAdapter;

    private ImageButton mBackButton;

    private Logger logger = LoggerFactory.getLogger("ui.OtherApkActivity");

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_apk);
        iniView();
        initListener();
        initData();
    }

    private void initData() {
        mOtherApkData = new ArrayList<>();
        mOtherApkAdapter = new OtherApkAdapter(this, mOtherApkData);
        mOtherApkListView.setAdapter(mOtherApkAdapter);
        loadOtherApkData()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfos -> {
                    mOtherApkData.addAll(appInfos);
                    mOtherApkAdapter.setData(mOtherApkData);

                }, throwable -> logger.error("initData", throwable));


    }

    private Observable<List<AppInfo>> loadOtherApkData() {
        return Observable.create(new Observable.OnSubscribe<List<AppInfo>>() {
            @Override
            public void call(Subscriber<? super List<AppInfo>> subscriber) {
                List<AppInfo> list = new ArrayList<>();
                List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
                for (int i = 0; i < packages.size(); i++) {
                    PackageInfo packageInfo = packages.get(i);
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
                    appInfo.setPackageName(packageInfo.packageName);
                    appInfo.setVersionName(packageInfo.versionName);
                    appInfo.setVersionCode(String.valueOf(packageInfo.versionCode));
                    appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(getPackageManager()));
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        list.add(appInfo);
                    }
                }
                subscriber.onNext(list);
            }
        });

    }

    private void initListener() {
        mBackButton.setOnClickListener(this);
        mOtherApkListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
                InstallerUtils.openApp(OtherApkActivity.this, mOtherApkData.get(position).getPackageName());
            }
        });
    }

    private void iniView() {
        mOtherApkListView = (ListView) findViewById(R.id.otherApk_listView);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                break;
        }
    }

    private class OtherApkAdapter extends BaseAdapter {

        private ArrayList<AppInfo> data;

        private Context context;

        private LayoutInflater inflater;

        public OtherApkAdapter(Context context, ArrayList<AppInfo> data) {
            this.data = data;
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<AppInfo> data) {
            this.data = (ArrayList<AppInfo>) data.clone();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.other_apk_item, parent, false);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
                holder.appName = (TextView) convertView.findViewById(R.id.appName);
                holder.packageName = (TextView) convertView.findViewById(R.id.packageName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AppInfo info = data.get(position);
            holder.appIcon.setBackground(info.getAppIcon());
            holder.appName.setText(info.getAppName());
            holder.packageName.setText(info.getPackageName());
            return convertView;
        }

        class ViewHolder {
            ImageView appIcon;
            TextView appName;
            TextView packageName;
        }
    }
}
