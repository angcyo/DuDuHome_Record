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


public class  VehiclePasswordReSetDialog extends Dialog implements View.OnClickListener {

    private Button btnDelete, btnOk, btnSend;

    private Button btZero, btOne, btTwo, btThree, btFour, btFive, btSix, btSeven, btEight, btNine, btWell, btRice;

    private ImageButton mBackButton;

    private EditText txtVerificationCode, txtNewPassword;

    private static final String TAG = "VehiclePasswordSetDialog";

    private boolean txtVerificationCodeFocus=true;

    private boolean txtNewPasswordFocus=false;

    private PasswordResetListener passwordResetListener;

    public VehiclePasswordReSetDialog(Context context) {
        super(context, R.style.PasswordSetDialogStyle);
    }

    public void onPasswordResetListener(PasswordResetListener listener){
this.passwordResetListener=listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_password_reset_dialog);
        initView();
        initListener();
    }

    private void initListener() {
        btnOk.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnSend.setOnClickListener(this);
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

        txtVerificationCode.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                txtVerificationCode.setInputType(InputType.TYPE_NULL);
                txtVerificationCodeFocus=true;
                txtNewPasswordFocus=false;
                obtainEditFocus(txtVerificationCode);
                return false;
            }
        });

        txtNewPassword.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                txtNewPassword.setInputType(InputType.TYPE_NULL);
                txtVerificationCodeFocus=false;
                txtNewPasswordFocus=true;
                obtainEditFocus(txtNewPassword);
                return false;
            }
        });

    }

    private void obtainEditFocus(EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.setCursorVisible(true);
    }

    private void initView() {
        btnOk = (Button) findViewById(R.id.button_ok);
        btnDelete = (Button) findViewById(R.id.button_delete);
        btnSend = (Button) findViewById(R.id.button_send);
        mBackButton=(ImageButton)findViewById(R.id.button_back);
        txtVerificationCode = (EditText) findViewById(R.id.txt_verification_code);
        txtNewPassword = (EditText) findViewById(R.id.txt_new_password);

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
            case R.id.button_send:
                sendSMS();
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



    private void actionResetPassword() {
        if (TextUtils.isEmpty(txtNewPassword.getText())&&TextUtils.isEmpty(txtVerificationCode.getText())){
            return;
        }

        String codes=txtVerificationCode.getText().toString().trim();
        String password=txtNewPassword.getText().toString().trim();

        RequestFactory.getGuardRequest().verificationVerificationCode(password,codes).subscribe(new Action1<RequestResponse>() {
            @Override
            public void call(RequestResponse requestResponse) {
                if (requestResponse.resultCode==0){
                    LogUtils.v(TAG,"设置密码成功。。");
                    //保存设置的数字密码
                    UserMessage saveUserMessage=new UserMessage();
                    saveUserMessage.setObeId(Long.parseLong(CommonLib.getInstance().getObeId()));
                    saveUserMessage.setDigitPassword(password);

                    DataFlowFactory.getUserMessageFlow().obtainUserMessage().subscribe(new Action1<UserMessage>() {
                        @Override
                        public void call(UserMessage userMessage) {
                            saveUserMessage.setGesturePassword(userMessage.getGesturePassword());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            LogUtils.v(TAG,"错误："+throwable.toString());
                        }
                    });
                    DataFlowFactory.getUserMessageFlow().saveUserMessage(saveUserMessage);
                    if (passwordResetListener!=null){
                        passwordResetListener.resetPasswordSuccess();
                    }
                    dismiss();
                }else {
                    LogUtils.v(TAG,"设置密码失败。。 错误码: "+requestResponse.resultCode+"--错误码信息："+requestResponse.resultMsg);
                }

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                LogUtils.v(TAG,throwable.toString());
            }
        });
    }

    private void removeDigit() {
        if (txtNewPasswordFocus){
            removeSelectedDigit(txtNewPassword);
        }else if (txtVerificationCodeFocus){
            removeSelectedDigit(txtVerificationCode);
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

    private void handleDialButtonClick(String digit) {
        if (txtNewPasswordFocus){
            if (txtNewPassword.getText().toString().length()==4){
                return;
            }
            setEditValue(txtNewPassword,digit);
        }else if (txtVerificationCodeFocus){
            setEditValue(txtVerificationCode,digit);
        }
    }

    private void setEditValue(EditText editText,String digit){
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

    private void sendSMS() {
        RequestFactory.getGuardRequest().obtainVerificationCode().subscribe(new Action1<RequestResponse>() {
            @Override
            public void call(RequestResponse requestResponse) {
                if (requestResponse.resultCode == 0) {
                    LogUtils.v(TAG, "获取验证码成功。。");
                } else {
                    LogUtils.v(TAG, "获取验证码失败" + "---错误码:" + requestResponse.resultCode + "---错误码信息：" + requestResponse.resultMsg);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                LogUtils.v(TAG, throwable.toString());
            }
        });
    }

    public interface PasswordResetListener{
        void  resetPasswordSuccess();
    }
}
