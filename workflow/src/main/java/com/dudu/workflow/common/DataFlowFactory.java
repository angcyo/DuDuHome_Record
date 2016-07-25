package com.dudu.workflow.common;

import com.dudu.persistence.RobberyMessage.RealRobberyMessageDataService;
import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.persistence.UserMessage.RealUserMessageDataService;
import com.dudu.persistence.app.RealmAppVersionService;
import com.dudu.persistence.driving.RealmFaultCodeService;
import com.dudu.persistence.switchmessage.RealmSwitchMessageService;
import com.dudu.workflow.app.LocalAppVersionFlow;
import com.dudu.workflow.driving.DrivingFlow;
import com.dudu.workflow.robbery.RobberyMessageFlow;
import com.dudu.workflow.switchmessage.SwitchDataFlow;
import com.dudu.workflow.userMessage.UserMessageFlow;

/**
 * Created by Administrator on 2016/2/17.
 */
public class DataFlowFactory {
    private static DataFlowFactory mInstance = new DataFlowFactory();
    private static SwitchDataFlow switchDataFlow;
    private static DrivingFlow drivingFlow;
    private static LocalAppVersionFlow localAppVersionFlow;

    private static UserMessageFlow userMessageFlow;

    private static RobberyMessageFlow robberyMessageFlow;

    public static DataFlowFactory getInstance() {
        return mInstance;
    }

    public void init() {
        userMessageFlow = new UserMessageFlow(new RealUserMessageDataService());
        switchDataFlow = new SwitchDataFlow(new RealmSwitchMessageService());
        drivingFlow = new DrivingFlow();
        drivingFlow.setFaultCodeService(new RealmFaultCodeService());
        localAppVersionFlow = new LocalAppVersionFlow(new RealmAppVersionService());
        robberyMessageFlow = new RobberyMessageFlow(new RealRobberyMessageDataService());
    }

    public static SwitchDataFlow getSwitchDataFlow() {
        return switchDataFlow;
    }

    public static DrivingFlow getDrivingFlow() {
        return drivingFlow;
    }

    public static UserMessageFlow getUserMessageFlow() {
        return userMessageFlow;
    }

    public static LocalAppVersionFlow getLocalAppVersionFlow() {
        return localAppVersionFlow;
    }

    public static RobberyMessageFlow getRobberyMessageFlow() {
        return robberyMessageFlow;
    }
}
