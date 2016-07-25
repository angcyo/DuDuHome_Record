package com.dudu.aios.ui.fragment.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.utils.DebugTime;
import com.dudu.android.launcher.R;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

/**
 * Created by angcyo on 15-08-31-031.
 */
public abstract class RBaseFragment extends BaseFragment {

    /*控制log输出*/
    public static boolean DEBUG = false;

    /*延迟加载时间*/
    public static long DELAY_TIME = 10;

    protected MainRecordActivity mBaseActivity;
    protected ViewGroup rootView;
    protected boolean isCreate = false;
    protected RBaseViewHolder mViewHolder;
    protected LayoutInflater mLayoutInflater;
    protected ViewGroup mFragmentLayout;
    protected FrameLayout mContainerLayout;//内容布局

    public static RBaseFragment getInstance(Bundle bundle, Class<? extends RBaseFragment> cls) {
        try {
            RBaseFragment fragment = cls.newInstance();
            fragment.setArguments(bundle);
            return fragment;
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @DebugLog
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData(savedInstanceState);
        if (DEBUG) {
            e("onCreate");
        }
    }

    @DebugLog
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            e("onCreateView");
        }

        mLayoutInflater = inflater;
        DebugTime.init();
        rootView = (ViewGroup) inflater.inflate(R.layout.rsen_base_fragment_layout, container, false);
//        DebugTime.time("inflate");

        int contentView = getContentView();
        if (contentView == 0) {
            View view = createContentView();
            if (view != null) {
                rootView.addView(view,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            mViewHolder = new RBaseViewHolder(rootView);
        } else {
            mViewHolder = new RBaseViewHolder(inflater.inflate(contentView, rootView, true));
        }
//        DebugTime.time("RBaseViewHolder");

        initBaseView();
//        DebugTime.time("initBaseView");

        initView(rootView);
//        DebugTime.time("initView");

        initAfter();
//        DebugTime.time("initAfter");

        isCreate = true;
        initViewData();
//        DebugTime.time("initViewData");

        initEvent();
        return rootView;
    }

    protected void initEvent() {

    }

    protected View createContentView() {
        return null;
    }

    private void initBaseView() {
        mContainerLayout = (FrameLayout) rootView.findViewById(R.id.container);
    }

    protected abstract int getContentView();

    protected abstract void initViewData();

    protected void loadData(Bundle savedInstanceState) {

    }

    protected void initView(View rootView) {

    }

    protected void initAfter() {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            e("onStart");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            e("onResume");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            e("onPause");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) {
            e("onStop");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (DEBUG) {
            e("onViewCreated");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (DEBUG) {
            e("onActivityCreated");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCreate = false;
        if (DEBUG) {
            e("onDestroy");
        }
    }

    @DebugLog
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isCreate) {
            if (isVisibleToUser) {
                onShow();
            } else {
                onHide();
            }
        }
        if (DEBUG) {
            e("setUserVisibleHint-->" + isVisibleToUser);
        }
    }

    @DebugLog
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //延迟加载
            View view = getView();
            if (view != null) {
                view.postDelayed(() -> {
                    if (isVisible()) {
                        onDelayShow();
                    }
                }, DELAY_TIME);
            }
        }
        if (DEBUG) {
            e("onHiddenChanged-->" + hidden);
        }
    }

    protected void onDelayShow() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (DEBUG) {
            e("onDestroyView");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG) {
            e("onDetach");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) {
            e("onSaveInstanceState");
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        if (DEBUG) {
            e("onInflate Context");
        }
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        if (DEBUG) {
            e("onInflate Activity");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBaseActivity = (MainRecordActivity) context;
        if (DEBUG) {
            e("onAttach Context");
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (DEBUG) {
            e("onFragmentResult");
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (DEBUG) {
            e("onViewStateRestored");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (DEBUG) {
            e("onLowMemory");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBaseActivity = (MainRecordActivity) activity;
        if (DEBUG) {
            e("onAttach Activity");
        }
    }

    protected void e(String log) {
        Log.e(new Exception().getStackTrace()[0].getClassName(), log);
    }
}
