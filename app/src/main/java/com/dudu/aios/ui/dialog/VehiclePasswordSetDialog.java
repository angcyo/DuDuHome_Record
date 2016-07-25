package com.dudu.aios.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.UserMessage.UserMessage;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;

import rx.functions.Action1;
/**
 * Created by luo zha on 2016/3/23.
 */
public class VehiclePasswordSetDialog extends Dialog implements View.OnClickListener{

    private Button btnDelete, btnOk;

    private Button btZero, btOne, btTwo, btThree, btFour, btFive, btSix, btSeven, btEight, btNine, btWell, btRice;

    private ImageButton mBackButton;

    private EditText txtPassword, txtRePassword;

    private static final String TAG = "VehiclePasswordReSetDialog";

    private boolean txtPasswordFocus=true;

    private boolean txtRePasswordFocus=false;

    private PasswordSetOnListener passwordSetOnListener;

    public VehiclePasswordSetDialog(Context context) {
        super(context, R.style.PasswordSetDialogStyle);
    }

    public void onPasswordSetOnListener(PasswordSetOnListener listener){
        this.passwordSetOnListener=listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_password_set_dialog);
        initView();
        initListener();
    }

    private void initListener() {
        btnOk.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btZero.setOnClickListener(this);
        btOne.setOnClickListener(this);
        btTwo.setOnClickListener(this);
        btThree.setOnClickListener(this);
        btFour.setOnClickListener(this);
        btFive.setOnClickListener(this);
        btSix.setOnClickListener(this);
        btSeven.setOnClickListener(this);
        btEight.setOnClickListener(this);
        btNine.setOnClickListener(this);
        btWell.setOnClickListener(this);
        btRice.setOnClickListener(this);
        mBackButton.setOnClickListener(this);


        txtPassword.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                txtPassword.setInputType(InputType.TYPE_NULL);
                txtPasswordFocus=true;
                txtRePasswordFocus=false;
                obtainEditFocus(txtPassword);
                return false;
            }
        });

        txtRePassword.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                txtRePassword.setInputType(InputType.TYPE_NULL);
                txtPasswordFocus=false;
                txtRePasswordFocus=true;
                obtainEditFocus(txtRePassword);
                return false;
            }
        });

    }

    private void removeDigit() {
        if (txtPasswordFocus){
            removeSelectedDigit(txtPassword);
        }else if (txtRePasswordFocus){
            removeSelectedDigit(txtRePassword);
        }
    }


    private void removeSelectedDigit(EditText editText) {
        final int length = editText.length();
        final int start = editText.getSelectionStart();
        final int end = editText.getSelectionEnd();
        if (start < end) {
            editText.getEditableText().replace(start, end, "");
        } else {
            if (editText.isCursorVisible()) {
                if (end > 0) {
                    editText.getEditableText().replace(end - 1, end, "");
                }
            } else {
                if (length > 1) {
                    editText.getEditableText().replace(length - 1, length, "");
                } else {
                    editText.getEditableText().clear();
                }
            }
            String digitString = editText.getText().toString();
            if (digitString.length() > 0) {
                if (digitString.substring(digitString.length() - 1, digitString.length()).equals(" ")) {
                    removeSelectedDigit(editText);
                }
            }
        }
    }

    private void initView() {
        btnOk = (Button) findViewById(R.id.button_ok);
        btnDelete = (Button) findViewById(R.id.button_delete);
        mBackButton=(ImageButton)findViewById(R.id.button_back);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        txtRePassword = (EditText) findViewById(R.id.txt_re_password);

        btZero = (Button) findViewById(R.id.button_zero);
        btOne = (Button) findViewById(R.id.button_one);
        btTwo = (Button) findViewById(R.id.button_two);
        btThree = (Button) findViewById(R.id.button_three);
        btFour = (Button) findViewById(R.id.button_four);
        btFive = (Button) findViewById(R.id.button_five);
        btSix = (Button) findViewById(R.id.button_six);
        btSeven = (Button) findViewById(R.id.button_seven);
        btEight = (Button) findViewById(R.id.button_eight);
        btNine = (Button) findViewById(R.id.button_nine);
        btWell = (Button) findViewById(R.id.button_well);
        btRice = (Button) findViewById(R.id.button_rice);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ok:
                actionResetPassword();
                break;
            case R.id.button_delete:
                removeDigit();
                break;

            case R.id.button_back:
                dismiss();
                break;
            case R.id.button_one:
                handleDialButtonClick("1");
                break;
            case R.id.button_two:
                handleDialButtonClick("2");
                break;
            case R.id.button_three:
                handleDialButtonClick("3");
                break;
            case R.id.button_four:
                handleDialButtonClick("4");
                break;
            case R.id.button_five:
                handleDialButtonClick("5");
                break;
            case R.id.button_six:
                handleDialButtonClick("6");
                break;
            case R.id.button_seven:
                handleDialButtonClick("7");
                break;
            case R.id.button_eight:
                handleDialButtonClick("8");
                break;
            case R.id.button_nine:
                handleDialButtonClick("9");
                break;
            case R.id.button_zero:
                handleDialButtonClick("0");
                break;
            case R.id.button_well:
                handleDialButtonClick("#");
                break;
            case R.id.button_rice:
                handleDialButtonClick("*");
                break;
        }
    }

    private void handleDialButtonClick(String digit) {
        if (txtPasswordFocus){

            setEditValue(txtPassword,digit);
        }else if (txtRePasswordFocus){
            setEditValue(txtRePassword,digit);
        }
    }

    private void setEditValue(EditText editText,String digit){
        if (editText.getText().toString().length()==4){
            return;
        }
        final int length = editText.length();
        final int start = editText.getSelectionStart();
        final int end = editText.getSelectionEnd();
        if (length == start && length == end) {
            editText.setCursorVisible(false);
        }

        if (start < end) {
            editText.getEditableText().replace(start, end, digit);
        } else {
            editText.getEditableText().insert(editText.getSelectionEnd(), digit);
        }
    }

    private void obtainEditFocus(EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.setCursorVisible(true);
    }

    private void actionResetPassword() {
        String password=txtPassword.getText().toString().trim();
        String rePassword=txtRePassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)&&TextUtils.isEmpty(rePassword)){
            return;
        }

        if (!password.equals(rePassword)){
            LogUtils.v(TAG,"两次输入的密码不相等。。。");
            return;
        }
        RequestFactory.getGuardRequest().setDigitPassword("1",password).subscribe(new Action1<RequestResponse>() {
            @Override
            public void call(RequestResponse requestResponse) {
                if (requestResponse.resultCode==0){
                    LogUtils.v(TAG,"设置数字密码成功");
                    UserMessage saveMessage = new UserMessage();
                    saveMessage.setObeId(Long.parseLong(CommonLib.getInstance().getObeId()));
                    saveMessage.setDigitPassword(password);
                    DataFlowFactory.getUserMessageFlow().obtainUserMessage().subscribe(new Action1<UserMessage>() {
                        @Override
                        public void call(UserMessage userMessage) {
                            saveMessage.setGesturePassword(userMessage.getGesturePassword());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                          LogUtils.v(TAG,throwable.toString());
                        }
                    });
                    DataFlowFactory.getUserMessageFlow().saveUserMessage(saveMessage);
                    passwordSetOnListener.setPasswordSuccess();
                    dismiss();
                }else {
                    LogUtils.v(TAG,"设置数字密码错误 ---- 错误码："+requestResponse.resultCode+"---错误信息："+requestResponse.resultMsg);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                LogUtils.v(TAG,"设置数字密码失败--"+throwable.toString());
            }
        });
    }

    public interface PasswordSetOnListener{
        void setPasswordSuccess();
    }
}
