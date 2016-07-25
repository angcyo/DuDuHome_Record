package com.dudu.aios.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dudu.aios.ui.utils.KeyboardUtil;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.android.launcher.utils.WifiApBean;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FlowPasswordSetDialog extends Dialog implements View.OnClickListener {

    private Button btnOk, btnCancel;

    private EditText txtWifiName, txtWifiPassword;

    private Activity act;

    private KeyboardView keyboardView;

    private OnCancelOnListener listener;

    Logger logger = LoggerFactory.getLogger("FlowFragment");

    public FlowPasswordSetDialog(Activity context) {
        super(context, R.style.PasswordSetDialogStyle);
        act = context;
    }

    public void setOnCancelOnListener(OnCancelOnListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flow_password_set_dialog);
        // getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        initView();
        initData();
        initClickListener();
    }

    private void initData() {
        WifiApBean wifiApBean = WifiApAdmin.obtainPassword(act);
        logger.debug("ssid:" + wifiApBean.getSsid() + "  password:" + wifiApBean.getPassword());
        if (wifiApBean.getSsid() == null || wifiApBean.getPassword() == null) {
            txtWifiName.setText(SharedPreferencesUtil.getStringValue(act, WifiApAdmin.KEY_WIFI_AP_SSID, WifiApAdmin.DEFAULT_SSID));
            txtWifiPassword.setText(SharedPreferencesUtil.getStringValue(act, WifiApAdmin.KEY_WIFI_AP_PASSWORD, WifiApAdmin.DEFAULT_PASSWORD));
        } else {
            txtWifiName.setText(wifiApBean.getSsid());
            txtWifiPassword.setText(wifiApBean.getPassword());
        }
        txtWifiName.setSelection(txtWifiName.getText().toString().length());
        txtWifiPassword.setSelection(txtWifiPassword.getText().toString().length());
    }

    private void initClickListener() {
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        initKeyboard(txtWifiName);
        txtWifiName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                initKeyboard(txtWifiName);
                return false;
            }
        });

        txtWifiPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                initKeyboard(txtWifiPassword);
                return false;
            }
        });
    }

    private void initKeyboard(EditText editText) {
        hideSoftInputMethod(editText);
        new KeyboardUtil(act, editText, keyboardView).showKeyboard();
        int inputBack = editText.getInputType();
        editText.setInputType(inputBack);
        editText.setSelection(editText.getText().toString().length());
        editText.setCursorVisible(true);
    }

    // 隐藏系统键盘
    public void hideSoftInputMethod(EditText ed) {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            methodName = "setSoftInputShownOnFocus";
        }

        if (methodName == null) {
            ed.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName,
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(ed, false);
            } catch (NoSuchMethodException e) {
                ed.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        btnOk = (Button) findViewById(R.id.button_ok);
        btnCancel = (Button) findViewById(R.id.button_cancel);
        txtWifiName = (EditText) findViewById(R.id.txt_wifi_name);
        txtWifiPassword = (EditText) findViewById(R.id.txt_wifi_password);
        keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ok:
                actionOk();
                break;
            case R.id.button_cancel:
                listener.actionCancel();
                dismiss();
                break;
        }
    }

    private void actionOk() {
        String name = txtWifiName.getText().toString();
        String password = txtWifiPassword.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            //名字不能为空
            logger.debug("密码或名字为空");
            Toast.makeText(act.getBaseContext(), act.getResources().getString(R.string.set_wifi_name_password_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.trim().length() < 8) {
            logger.debug("设置的密码要设置的密码的长度小于8");
            Toast.makeText(act.getBaseContext(), act.getResources().getString(R.string.set_wifi_password_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.trim().length() > 15) {
            logger.debug("设置的wifi热点的名字不能超过15位");
            Toast.makeText(act.getBaseContext(), act.getResources().getString(R.string.wifi_ap_exceed_fifteen_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        dismiss();
        if (listener != null) {
            listener.actionOk(name, password);
        }
        //密码设置成功
        logger.debug("设置热点的名字：" + name + "  密码：" + password);
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.debug("onStart()..");
        initData();
    }

    public interface OnCancelOnListener {
        void actionCancel();

        void actionOk(String ssid, String password);
    }
}
