package com.dudu.aios.ui.fragment;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.Contacts;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.workflow.common.DataFlowFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseUploadFragment extends RBaseFragment implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger("LicenseUploadFragment");

    private ImageButton mBackButton;

    private TextView tvPrompt;

    @Override
    protected int getContentView() {
        return R.layout.fragment_license_uplaod;
    }

    @Override
    protected void initViewData() {

        iniView();

        iniData();

        iniLister();

    }

    private void iniLister() {
        mBackButton.setOnClickListener(this);
    }

    private void iniData() {
        int licenseType = (int) SharedPreferencesUtil.getLongValue(getActivity(), Contacts.LICENSE_TYPE, 0);
        logger.debug("获取证件的类型为：" + licenseType);
        switch (licenseType) {
            case Contacts.DRIVING_TYPE:
                queryAuditStateDB();
                break;
            case Contacts.INSURANCE_TYPE:
                queryInsuranceAuditState();
                break;
        }

    }

    private void queryInsuranceAuditState() {
        int auditStatus = (int) SharedPreferencesUtil.getLongValue(getActivity(), Contacts.AUDIT_STATE, 0);
        logger.debug("获取保险证的状态为：" + auditStatus);
        switch (auditStatus) {
            case Contacts.AUDIT_STATE_NOT_APPROVE:
                tvPrompt.setText(getResources().getString(R.string.insurance_license_upload_prompt));
                break;
            case Contacts.AUDIT_STATE_AUDITING:
                tvPrompt.setText(getResources().getString(R.string.insurance_license_auditing_prompt));
                break;
            case Contacts.AUDIT_STATE_REJECT:
                tvPrompt.setText(getResources().getString(R.string.insurance_license_reject_prompt));
                break;
        }
    }

    private void queryAuditStateDB() {
        DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                .map(userMessage -> userMessage.getAudit_state())
                .filter(audit_state -> audit_state != 2)
                .subscribe(auditState -> {
                            logger.debug("查询数据库的审核状态：" + auditState);
                            switch (Integer.parseInt(String.valueOf(auditState))) {
                                case Contacts.AUDIT_STATE_NOT_APPROVE:
                                    tvPrompt.setText(getResources().getString(R.string.driving_license_upload_prompt));
                                    break;
                                case Contacts.AUDIT_STATE_AUDITING:
                                    tvPrompt.setText(getResources().getString(R.string.driving_license_auditing_prompt));
                                    break;
                                case Contacts.AUDIT_STATE_REJECT:
                                    tvPrompt.setText(getResources().getString(R.string.driving_license_reject_prompt));
                                    break;
                            }
                }, throwable -> logger.error("queryAuditStateDB", throwable));

    }

    private void iniView() {
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
        tvPrompt = (TextView) mViewHolder.v(R.id.license_prompt);
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("fragment is onShow()");
        iniData();
    }

    @Override
    public void onHide() {
        super.onHide();
        logger.debug("fragment is onHide()");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                mBaseActivity.showMain();
                break;
        }
    }
}
